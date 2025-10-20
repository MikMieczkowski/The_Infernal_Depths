package com.mikm.entityLoader;

import com.badlogic.gdx.math.Rectangle;
import com.mikm.Vector2Int;
import com.mikm.entities.Entity;
import com.mikm.entities.routineHandler.Routine;
import com.mikm.rendering.cave.SpawnProbability;
import com.mikm.rendering.screens.Application;

import java.util.Map;

public class EntityConfigLoader {
    Entity entity;
    EntityData data;

    EntityConfigLoader(Entity entity, EntityData data) {
        this.entity = entity;
        this.data = data;
    }

    void loadConfigPreRead() {
        loadConfigVariables();
        loadSpawnConfig();
        loadConfigBounds();
        loadConfigHurtAnim();
    }

    private void loadConfigVariables() {
        EntityData.Config config = data.CONFIG;
        entity.NAME = config.NAME;
        entity.MAX_HP = varOrDef(config.MAX_HP, 3);
        entity.hp = entity.MAX_HP;
        entity.DAMAGE = varOrDef(config.DAMAGE, 1);
        entity.KNOCKBACK = varOrDef(config.KNOCKBACK, 1);
        entity.SPEED = varOrDef(config.SPEED, 1);
        entity.isAttackable = !config.INVINCIBLE;

        entity.HURT_SOUND_EFFECT = config.HURT_SOUND_EFFECT;
        entity.routineHandler.CHECK_TRANSITIONS_EVERY_FRAME = config.CHECK_TRANSITIONS_EVERY_FRAME;
    }

    private void loadSpawnConfig() {
        EntityData.SpawnConfig spawnConfig = data.SPAWN_CONFIG;
        if (spawnConfig != null) {
            spawnConfig.LEVEL_4_SPAWN_PERCENT = spawnConfig.LEVEL_3_SPAWN_PERCENT;
            entity.spawnProbability = new SpawnProbability(spawnConfig.LEVEL_1_SPAWN_PERCENT, spawnConfig.LEVEL_2_SPAWN_PERCENT, spawnConfig.LEVEL_3_SPAWN_PERCENT, spawnConfig.LEVEL_4_SPAWN_PERCENT);
        }
    }

    private void loadConfigBounds() {
        EntityData.Config config = data.CONFIG;
        if (config.BOUNDS == null) {
            config.BOUNDS = new EntityData.Config.BoundsData();
        }
        int w = varOrDef(config.BOUNDS.IMAGE_WIDTH, Application.TILE_WIDTH);
        int h = varOrDef(config.BOUNDS.IMAGE_HEIGHT, Application.TILE_HEIGHT);
        entity.FULL_BOUNDS_DIMENSIONS = new Vector2Int(w, h);

        entity.ORIGIN_X = varOrDef(config.ORIGIN_X, entity.FULL_BOUNDS_DIMENSIONS.x/2);

        //BOUNDS
        //DEFAULTS

        boolean shadow = config.BOUNDS.HAS_SHADOW == null || config.BOUNDS.HAS_SHADOW;

        int sw = varOrDef(config.BOUNDS.SHADOW_WIDTH, Application.TILE_WIDTH);
        int sh = varOrDef(config.BOUNDS.SHADOW_HEIGHT, Application.TILE_HEIGHT);

        int hr = (config.BOUNDS.HITBOX_RADIUS == 0) ? 8 : config.BOUNDS.HITBOX_RADIUS;

        entity.HAS_SHADOW = shadow;
        entity.SHADOW_BOUNDS_OFFSETS = new Rectangle(
                config.BOUNDS.SHADOW_OFFSET_X,
                config.BOUNDS.SHADOW_OFFSET_Y,
                sw,
                sh
        );
        entity.HITBOX_OFFSETS = new Vector2Int(config.BOUNDS.HITBOX_OFFSET_X, config.BOUNDS.HITBOX_OFFSET_Y);
        entity.HITBOX_RADIUS = hr;
    }

    private void loadConfigHurtAnim() {
        EntityData.Config config = data.CONFIG;
        if (config.HURT_ANIMATION == null) {
            config.HURT_ANIMATION = new EntityData.BehaviourData();
            if (data.BEHAVIOURS.containsKey("Idle")) {
                config.HURT_ANIMATION.COPY_ANIMATION = "Idle";
            } else {
                //grab random animation
                config.HURT_ANIMATION.COPY_ANIMATION = data.BEHAVIOURS.entrySet().iterator().next().getKey();
            }
        }
    }


    //These are the config items which require behaviours or routines to be read before them.
    void loadConfigPostRead(Map<String, Routine> nameToRoutine) {
        EntityData.Config config = data.CONFIG;
        entity.NAME = config.NAME;
    }

    private int varOrDef(int var, int def) {
        if (var == 0) {
            return def;
        }
        return var;
    }

    private float varOrDef(float var, float def) {
        if (var == 0) {
            return def;
        }
        return var;
    }
}
