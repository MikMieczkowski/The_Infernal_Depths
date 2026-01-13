package com.mikm.entities.prefabLoader;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.mikm.YamlCopyResolver;
import com.mikm._components.*;
import com.mikm._components.routine.Routine;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm.entities.actions.Action;

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
        for (FileHandle file : dir.list()) {
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
