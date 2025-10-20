package com.mikm.entityLoader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.mikm.entities.Entity;
import com.mikm.entities.actions.Action;
import com.mikm.entities.player.Player;
import com.mikm.entities.routineHandler.*;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;

//Add checks whenever there is only supposed to be one entry but there is multiple
public class EntityLoader {

    //"slime" -> Created Slime instance
    private static Map<String, Entity> entities = new HashMap<>();

    private static EntityConfigLoader configLoader;
    private static EntityActionLoader actionLoader;
    private static EntityRoutineLoader routineLoader;

    //ANY CHANGES MADE HERE MUST ALSO BE MADE TO COPY ENTITY

    public static Entity create(String entityName) {
        //if already read this yaml file, return a copy of the entity, otherwise read
        if (entities.containsKey(entityName)) {
            if (entityName.equals("player")) {
                throw new RuntimeException("Already created player");
            }
            return copyEntity(entities.get(entityName));
        }

        FileHandle file = Gdx.files.internal("yaml/" + entityName + ".yaml");
        String fileName = entityName + ".yaml";
        InputStream input = file.read();
        Yaml yaml = new Yaml();
        EntityData data = yaml.loadAs(input, EntityData.class);
        Entity entity = createEntityFromEntityData(data, fileName);
        if (entityName.equals("bat")) {
            entity.collider.isBat = true;
        }
        entities.put(entityName, entity);
        return entity;
    }

    private static Entity createEntityFromEntityData(EntityData data, String fileName) {
        Entity entity;
        if (data.CONFIG.NAME.equals("player")) {
            entity = new Player(0, 0);
        } else {
            entity = new Entity(0, 0);
        }

        configLoader = new EntityConfigLoader(entity, data);
        actionLoader = new EntityActionLoader(fileName, entity, data.BEHAVIOURS);
        routineLoader = new EntityRoutineLoader(fileName, entity, data.ROUTINES);

        //Must load in this order: needed config/spawnconfig, BEHAVIOURS, ROUTINES, then CONFIG that requires behaviours or routines to be loaded first

        configLoader.loadConfigPreRead();

        Map<String, Action> nameToAction = actionLoader.loadActions(data.CONFIG.HURT_ANIMATION, data.CONFIG.POST_HIT_ROUTINE);
        Map<String, String> behaviourNameToInterruptRoutine = loadBehaviourNameToInterruptRoutine(data.BEHAVIOURS);
        Map<String, Routine> nameToRoutine = routineLoader.loadRoutines(behaviourNameToInterruptRoutine, nameToAction);

        configLoader.loadConfigPostRead(nameToRoutine);
        return entity;
    }


    private static Map<String, String> loadBehaviourNameToInterruptRoutine(Map<String, EntityData.BehaviourData> behaviourDataMap) {
        Map<String, String> output = new HashMap<>();
        for (Map.Entry<String, EntityData.BehaviourData> entry : behaviourDataMap.entrySet()) {
            if (entry.getValue().ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO != null) {
                output.put(entry.getKey(), entry.getValue().ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO);
            }
        }
        return output;
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

    private static Entity copyEntity(Entity entity) {
        Entity output = new Entity(entity.x, entity.y);
        output.damagesPlayer = entity.damagesPlayer;
        output.NAME = entity.NAME;
        output.DAMAGE = entity.DAMAGE;
        output.KNOCKBACK = entity.KNOCKBACK;
        output.MAX_HP = entity.MAX_HP;
        output.hp = output.MAX_HP;
        output.SPEED = entity.SPEED;
        output.ORIGIN_X = entity.ORIGIN_X;
        output.ORIGIN_Y = entity.ORIGIN_Y;
        output.isAttackable = entity.isAttackable;
        output.HURT_SOUND_EFFECT = entity.HURT_SOUND_EFFECT;
        output.spawnProbability = entity.spawnProbability;
        output.HAS_SHADOW = entity.HAS_SHADOW;
        output.SHADOW_BOUNDS_OFFSETS = entity.SHADOW_BOUNDS_OFFSETS;
        output.FULL_BOUNDS_DIMENSIONS = entity.FULL_BOUNDS_DIMENSIONS;
        output.HITBOX_OFFSETS = entity.HITBOX_OFFSETS;
        output.HITBOX_RADIUS = entity.HITBOX_RADIUS;
        //deep copy routines/cycles/actions
        output.routineHandler.init(entity.routineHandler);
        output.damagedAction.name = "damaged";
        output.damagedAction.animation = entity.damagedAction.animation.copy();
        // Copy used action classes and (re)bind per-entity timeSince<Action> vars for this new entity
        output.usedActionClasses.addAll(entity.usedActionClasses);

        for (String actionName : output.usedActionClasses) {
            Blackboard.getInstance().bind("timeSince" + actionName, output, 0f);
        }
        if (output.NAME.equals("bat")) {
            output.collider.isBat = true;
        }
        output.isCopied = true;
        return output;
    }
}
