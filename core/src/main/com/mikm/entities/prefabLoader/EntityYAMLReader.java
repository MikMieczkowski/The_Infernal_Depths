package com.mikm.entities.prefabLoader;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.mikm.YamlCopyResolver;
import com.mikm._components.*;
import com.mikm._components.routine.Routine;
import com.mikm._components.AerialStateComponent;
import com.mikm._components.AttackInputComponent;
import com.mikm._components.ComboStateComponent;
import com.mikm._components.LockOnComponent;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm.entities.actions.Action;
import com.mikm.entities.prefabLoader.weapon.WeaponFormattedData;
import com.mikm.entities.prefabLoader.weapon.WeaponRawData;
import com.mikm.entities.prefabLoader.weapon.WeaponTransformers;
import com.mikm.rendering.screens.Application;

import java.util.*;

public class EntityYAMLReader {

    public static void addPrefab(String entityName, Map<String, PrefabInstantiator.EntityInitFunction> entityNameToActionsInit) {
        //Create prefab from yaml file if not already created, then create entity from prefab
        if (!PrefabInstantiator.prefabExists(entityName)) {
            String fileName = "yaml/" + entityName + ".yaml";
            EntityYAMLData data = YamlCopyResolver.loadAndResolve(fileName, EntityYAMLData.class);
            //loadComponents sets actionsInit
            PrefabInstantiator.addPrefab(entityName, loadComponents(data, fileName, entityNameToActionsInit));
        }
    }

    public static java.util.List<String> getEntitiesInYamlFolder() {
        java.util.List<String> names = new java.util.ArrayList<>();

        FileHandle dir = Gdx.files.internal("yaml"); // relative to assets/
        FileHandle[] files = dir.list();

        // dir.list() returns empty from JAR classpath; fall back to manifest
        if (files == null || files.length == 0) {
            FileHandle manifest = Gdx.files.internal("yaml/entities.txt");
            if (manifest.exists()) {
                for (String line : manifest.readString().split("\r?\n")) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        names.add(line);
                    }
                }
                return names;
            }
            // No manifest either — return empty
            return names;
        }

        for (FileHandle file : files) {
            if (!file.extension().equals("yaml") && !file.extension().equals("yml")) continue;

            boolean inConfig = false;
            for (String line : file.readString().split("\r?\n")) {
                line = line.trim();

                if (line.equals("CONFIG:")) {
                    inConfig = true;
                    continue;
                }

                if (inConfig && line.startsWith("NAME:")) {
                    // extract value after "NAME:"
                    String value = line.substring("NAME:".length()).trim();
                    names.add(value);
                    inConfig = false; // stop after first NAME in CONFIG
                }
            }
        }

        return names;
    }

    private static Set<Component> loadComponents(EntityYAMLData data, String fileName, Map<String, PrefabInstantiator.EntityInitFunction> entityNameToActionsInit) {
        Map<Class<? extends Component>, Component> components = new HashMap<>();
        components.put(WorldColliderComponent.class, new WorldColliderComponent());
        components.put(Transform.class, new Transform(0, 0));
        components.put(RoutineListComponent.class, new RoutineListComponent());
        components.put(SpriteComponent.class, new SpriteComponent());
        components.put(CombatComponent.class, new CombatComponent(data.CONFIG.NAME.equals("player")));
        components.put(SpawnComponent.class, new SpawnComponent());
        components.put(ShadowComponent.class, new ShadowComponent());
        components.put(EffectsComponent.class, new EffectsComponent());
        if (data.CONFIG.NAME.equals("player")) {
            //load playerCombatComponent
            PlayerCombatComponent playerCombatComponent = new PlayerCombatComponent();
            components.put(PlayerCombatComponent.class, playerCombatComponent);

            // Add combat system components for player
            components.put(LockOnComponent.class, new LockOnComponent());
            components.put(AttackInputComponent.class, new AttackInputComponent());

            // Load weapon data and populate combo trees
            ComboStateComponent comboState = new ComboStateComponent();
            try {
                WeaponTransformers.register();
                WeaponFormattedData weaponData = YAMLLoader.load(
                    "weapons/copperSword.yaml",
                    "weapons/weapon.yaml",
                    WeaponRawData.class,
                    WeaponFormattedData.class
                );
                if (weaponData != null) {
                    comboState.groundedRoot = weaponData.COMBO_TREE;
                    comboState.aerialRoot = weaponData.AERIAL_COMBO_TREE;
                    comboState.weaponConfig = weaponData.CONFIG;
                    if (weaponData.ORBIT != null && weaponData.ORBIT.POINTS_TOWARDS_LOCKED != null) {
                        comboState.pointsTowardsLocked = weaponData.ORBIT.POINTS_TOWARDS_LOCKED;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to load weapon data: " + e.getMessage());
            }
            components.put(ComboStateComponent.class, comboState);
        } else {
            // Add AerialStateComponent to enemies for launcher attacks
            components.put(AerialStateComponent.class, new AerialStateComponent());
        }


        EntityYAMLConfigReader configLoader = new EntityYAMLConfigReader(components, data);
        EntityYAMLActionReader actionLoader = new EntityYAMLActionReader(fileName, components, data.BEHAVIOURS);
        EntityYAMLRoutineReader routineLoader = new EntityYAMLRoutineReader(fileName, components, data.ROUTINES);

        boolean noSpawnComponent = data.SPAWN_CONFIG == null;
        if (noSpawnComponent) {
            components.remove(SpawnComponent.class);
        }
        boolean noShadow = data.CONFIG.BOUNDS != null && data.CONFIG.BOUNDS.HAS_SHADOW != null && !data.CONFIG.BOUNDS.HAS_SHADOW;
        if (noShadow) {
            components.remove(ShadowComponent.class);
        }

        //Must load in this order: needed config/spawnconfig, BEHAVIOURS, ROUTINES, then CONFIG that requires behaviours or routines to be loaded first

        configLoader.loadConfigPreRead();

        Map<String, String> behaviourNameToInterruptRoutine = loadBehaviourNameToInterruptRoutine(data.BEHAVIOURS);
        Map<String, Action> nameToAction = actionLoader.loadActions(data.CONFIG.HURT_ANIMATION, data.CONFIG.POST_HIT_ROUTINE);
        Map<String, Routine> nameToRoutine = routineLoader.loadRoutines(behaviourNameToInterruptRoutine, nameToAction);
        entityNameToActionsInit.put(data.CONFIG.NAME, (Entity e) -> {
            for (Action a : nameToAction.values()) {
                Component c = a.createActionComponent();
                if (c != null) {
                    e.add(c);
                }
                a.postConfigRead(e);
            }
            RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(e);
            e.add(routineListComponent.damagedAction.createActionComponent());
            
            // Add trigger component to enemies (non-player entities with CombatComponent)
            if (!data.CONFIG.NAME.equals("player")) {
                CombatComponent combatComponent = CombatComponent.MAPPER.get(e);
                if (combatComponent != null) {
                    Transform transform = Transform.MAPPER.get(e);
                    // Use entity width as trigger diameter, or default to TILE_WIDTH
                    int triggerDiameter = transform.FULL_BOUNDS_DIMENSIONS != null ?
                        transform.FULL_BOUNDS_DIMENSIONS.x : Application.TILE_WIDTH;
                    if (combatComponent.DAMAGE > 0) {
                        // Entity deals contact damage and receives projectile/mining damage
                        e.add(new TriggerComponent(
                            triggerDiameter,
                            TriggerEntityType.ENEMY,
                            Event.ON_STAY, TriggerAction.playerHit(),
                            Event.ON_PLAYER_PROJECTILE_STAY, TriggerAction.enemyHit(),
                            Event.ON_PLAYER_MINING_PROJECTILE_STAY, TriggerAction.bump()
                        ));
                    } else {
                        // Entity only receives projectile/mining damage (no contact damage)
                        e.add(new TriggerComponent(
                            triggerDiameter,
                            TriggerEntityType.ENEMY,
                            Event.ON_PLAYER_PROJECTILE_STAY, TriggerAction.enemyHit(),
                            Event.ON_PLAYER_MINING_PROJECTILE_STAY, TriggerAction.bump()
                        ));
                    }
                }
            }
        });

        configLoader.loadConfigPostRead(nameToRoutine);

        return new HashSet<>(components.values());
    }

    private static Map<String, String> loadBehaviourNameToInterruptRoutine(Map<String, EntityYAMLData.BehaviourData> behaviourDataMap) {
        Map<String, String> output = new HashMap<>();
        for (Map.Entry<String, EntityYAMLData.BehaviourData> entry : behaviourDataMap.entrySet()) {
            if (entry.getValue().ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO != null) {
                output.put(entry.getKey(), entry.getValue().ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO);
            }
        }
        return output;
    }

}
