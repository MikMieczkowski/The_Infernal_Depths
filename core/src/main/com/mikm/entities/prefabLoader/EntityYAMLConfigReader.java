package com.mikm.entities.prefabLoader;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Rectangle;
import com.mikm.Vector2Int;
import com.mikm._components.*;
import com.mikm._components.routine.Routine;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm.rendering.cave.SpawnProbability;
import com.mikm.rendering.screens.Application;

import java.util.Map;

public class EntityYAMLConfigReader {
    private EntityYAMLData data;

    private Transform transform;
    private CombatComponent combatComponent;
    private SpriteComponent spriteComponent;
    private RoutineListComponent routineListComponent;
    private WorldColliderComponent worldColliderComponent;
    private SpawnComponent spawnComponent;
    private ShadowComponent shadowComponent;


    EntityYAMLConfigReader(Map<Class<? extends Component>, Component> components, EntityYAMLData data) {
        this.data = data;

        transform = (Transform) components.get(Transform.class);
        combatComponent = (CombatComponent) components.get(CombatComponent.class);
        spriteComponent = (SpriteComponent) components.get(SpriteComponent.class);
        routineListComponent = (RoutineListComponent) components.get(RoutineListComponent.class);
        worldColliderComponent = (WorldColliderComponent) components.get(WorldColliderComponent.class);
        spawnComponent = (SpawnComponent) components.get(SpawnComponent.class);
        shadowComponent = (ShadowComponent) components.get(ShadowComponent.class);
    }

    void loadConfigPreRead() {
        loadConfigVariables();
        loadSpawnConfig();
        loadConfigBounds();
        loadConfigHurtAnim();
    }

    private void loadConfigVariables() {
        EntityYAMLData.Config config = data.CONFIG;

        transform.ENTITY_NAME = config.NAME;
        combatComponent.MAX_HP = varOrDef(config.MAX_HP, 3);
        combatComponent.hp = combatComponent.MAX_HP;
        combatComponent.DAMAGE = varOrDef(config.DAMAGE, 10);
        combatComponent.KNOCKBACK = varOrDef(config.KNOCKBACK, 1);
        transform.SPEED = config.SPEED != null ? config.SPEED : 1;
        combatComponent.setAttackable(!config.INVINCIBLE);

        combatComponent.HURT_SOUND_EFFECT = config.HURT_SOUND_EFFECT;
        routineListComponent.CHECK_TRANSITIONS_EVERY_FRAME = config.CHECK_TRANSITIONS_EVERY_FRAME;

        if (config.NAME.equals("bat")) {
            worldColliderComponent.IS_BAT = true;
        }
    }

    private void loadSpawnConfig() {
        EntityYAMLData.SpawnConfig spawnConfig = data.SPAWN_CONFIG;
        if (spawnConfig != null) {
            spawnConfig.LEVEL_4_SPAWN_PERCENT = spawnConfig.LEVEL_3_SPAWN_PERCENT;
            spawnComponent.spawnProbability = new SpawnProbability(spawnConfig.LEVEL_1_SPAWN_PERCENT, spawnConfig.LEVEL_2_SPAWN_PERCENT, spawnConfig.LEVEL_3_SPAWN_PERCENT, spawnConfig.LEVEL_4_SPAWN_PERCENT);
        }
    }

    private void loadConfigBounds() {
        EntityYAMLData.Config config = data.CONFIG;
        if (config.BOUNDS == null) {
            config.BOUNDS = new EntityYAMLData.Config.BoundsData();
        }
        int w = varOrDef(config.BOUNDS.IMAGE_WIDTH, Application.TILE_WIDTH);
        int h = varOrDef(config.BOUNDS.IMAGE_HEIGHT, Application.TILE_HEIGHT);
        transform.FULL_BOUNDS_DIMENSIONS = new Vector2Int(w, h);

        transform.ORIGIN_X = varOrDef(config.ORIGIN_X, transform.FULL_BOUNDS_DIMENSIONS.x/2);

        //BOUNDS
        //DEFAULTS

        boolean shadow = config.BOUNDS.HAS_SHADOW == null || config.BOUNDS.HAS_SHADOW;

        int sw = varOrDef(config.BOUNDS.SHADOW_WIDTH, Application.TILE_WIDTH);
        int sh = varOrDef(config.BOUNDS.SHADOW_HEIGHT, Application.TILE_HEIGHT);

        int hr = (config.BOUNDS.HITBOX_RADIUS == 0) ? 8 : config.BOUNDS.HITBOX_RADIUS;
        transform.HALF_BOUNDS_WIDTH = hr;

        shadowComponent.BOUNDS_OFFSETS = new Rectangle(
                config.BOUNDS.SHADOW_OFFSET_X,
                config.BOUNDS.SHADOW_OFFSET_Y,
                sw,
                sh
        );
        worldColliderComponent.HITBOX_OFFSETS = new Vector2Int(config.BOUNDS.HITBOX_OFFSET_X, config.BOUNDS.HITBOX_OFFSET_Y);
        worldColliderComponent.RADIUS = hr;
    }

    private void loadConfigHurtAnim() {
        EntityYAMLData.Config config = data.CONFIG;
        if (config.HURT_ANIMATION == null) {
            // Do not use COPY_ANIMATION by default; synthesize a concrete ANIMATION block.
            config.HURT_ANIMATION = new EntityYAMLData.BehaviourData();

            // Try to find a directional source for first-frame idle:
            // 1) If Idle references another behaviour via FROM_FIRST_FRAME_OF_EACH_DIR, use that target.
            // 2) Otherwise pick the first behaviour that looks directional (TYPE == DIRECTIONAL_ANIMATION or STARTS_WITH present).
            // 3) Otherwise fall back to a concrete SINGLE_* from the first behaviour.
            String directionalSource = null;
            if (data.BEHAVIOURS.containsKey("Idle")) {
                EntityYAMLData.BehaviourData idle = data.BEHAVIOURS.get("Idle");
                if (idle != null && idle.ANIMATION != null && idle.ANIMATION.FROM_FIRST_FRAME_OF_EACH_DIR != null) {
                    directionalSource = idle.ANIMATION.FROM_FIRST_FRAME_OF_EACH_DIR;
                }
            }
            if (directionalSource == null) {
                for (Map.Entry<String, EntityYAMLData.BehaviourData> e : data.BEHAVIOURS.entrySet()) {
                    EntityYAMLData.BehaviourData b = e.getValue();
                    if (b != null && b.ANIMATION != null &&
                            ("DIRECTIONAL_ANIMATION".equals(b.ANIMATION.TYPE) || b.ANIMATION.STARTS_WITH != null)) {
                        directionalSource = e.getKey();
                        break;
                    }
                }
            }

            config.HURT_ANIMATION.ANIMATION = new EntityYAMLData.BehaviourData.AnimData();
            if (directionalSource != null) {
                config.HURT_ANIMATION.ANIMATION.TYPE = "FROM_FIRST_FRAME_OF_EACH_DIR";
                config.HURT_ANIMATION.ANIMATION.FROM_FIRST_FRAME_OF_EACH_DIR = directionalSource;
            } else {
                // Fall back: mirror the first behaviour's concrete single animation if possible
                Map.Entry<String, EntityYAMLData.BehaviourData> first = data.BEHAVIOURS.entrySet().iterator().next();
                EntityYAMLData.BehaviourData base = first.getValue();
                if (base != null && base.ANIMATION != null) {
                    boolean looksSingleAnim = base.ANIMATION.IMAGE_NAME != null && base.ANIMATION.FPS > 0;
                    boolean looksSingleFrame = base.ANIMATION.IMAGE_NAME != null && base.ANIMATION.FPS == 0;
                    if ("SINGLE_ANIMATION".equals(base.ANIMATION.TYPE) || looksSingleAnim) {
                        config.HURT_ANIMATION.ANIMATION.TYPE = "SINGLE_ANIMATION";
                        config.HURT_ANIMATION.ANIMATION.IMAGE_NAME = base.ANIMATION.IMAGE_NAME;
                        config.HURT_ANIMATION.ANIMATION.FPS = base.ANIMATION.FPS;
                        config.HURT_ANIMATION.ANIMATION.LOOP = base.ANIMATION.LOOP;
                    } else if ("SINGLE_FRAME".equals(base.ANIMATION.TYPE) || looksSingleFrame) {
                        config.HURT_ANIMATION.ANIMATION.TYPE = "SINGLE_FRAME";
                        config.HURT_ANIMATION.ANIMATION.IMAGE_NAME = base.ANIMATION.IMAGE_NAME;
                    } else {
                        // Final resort: a harmless single frame with no image (will be validated later if needed)
                        config.HURT_ANIMATION.ANIMATION.TYPE = "SINGLE_FRAME";
                        config.HURT_ANIMATION.ANIMATION.IMAGE_NAME = base.ANIMATION.IMAGE_NAME;
                    }
                } else {
                    // No behaviours? Extremely unlikely, but guard anyway
                    config.HURT_ANIMATION.ANIMATION.TYPE = "SINGLE_FRAME";
                }
            }
        }
    }


    //These are the config items which require behaviours or routines to be read before them.
    void loadConfigPostRead(Map<String, Routine> nameToRoutine) {
        EntityYAMLData.Config config = data.CONFIG;
        transform.ENTITY_NAME = config.NAME;
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
