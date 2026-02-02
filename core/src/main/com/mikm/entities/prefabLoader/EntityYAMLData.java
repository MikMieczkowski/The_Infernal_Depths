package com.mikm.entities.prefabLoader;

import java.util.List;
import java.util.Map;

public class EntityYAMLData {

    public Config CONFIG;
    public SpawnConfig SPAWN_CONFIG;
    public Map<String, BehaviourData> BEHAVIOURS;
    public Map<String, RoutineData> ROUTINES;

    public static class Config {
        public String NAME;
        public int KNOCKBACK;
        public int MAX_HP;
        public int DAMAGE;
        public Float SPEED;
        public int ORIGIN_X;
        public int ORIGIN_Y;
        public boolean INVINCIBLE;
        public BehaviourData HURT_ANIMATION;
        public String HURT_SOUND_EFFECT;
        public boolean CHECK_TRANSITIONS_EVERY_FRAME;
        public String POST_HIT_ROUTINE;

        public BoundsData BOUNDS;

        public static class BoundsData {
            public int IMAGE_WIDTH;
            public int IMAGE_HEIGHT;
            public Boolean HAS_SHADOW;
            public int SHADOW_OFFSET_X;
            public int SHADOW_OFFSET_Y;
            public int SHADOW_WIDTH;
            public int SHADOW_HEIGHT;
            public int HITBOX_OFFSET_X;
            public int HITBOX_OFFSET_Y;
            public int HITBOX_RADIUS;
        }
    }

    public static class SpawnConfig {
        public float LEVEL_1_SPAWN_PERCENT;
        public float LEVEL_2_SPAWN_PERCENT;
        public float LEVEL_3_SPAWN_PERCENT;
        public float LEVEL_4_SPAWN_PERCENT;
    }

    public static class BehaviourData {
        public String ACTION;
        public AnimData ANIMATION;
        //public AnimData DIRECTIONAL_ANIMATION;
        //public AnimData SINGLE_ANIMATION;
        //public AnimData SINGLE_FRAME;
        //public String COPY_ANIMATION;
        public String ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO;
        // Per-behaviour post-hit routine, defined alongside ACTION/ANIMATION in YAML
        public String POST_HIT_ROUTINE;
        //public String COPY_CONFIG;
        public Map<String, Object> CONFIG;


        public static class AnimData {
            public String TYPE;
            public String FROM_FIRST_FRAME_OF_EACH_DIR;
            public String STARTS_WITH;
            public String IMAGE_NAME;
            public float FPS;
            public boolean LOOP;
        }
    }

    public static class RoutineData {
        public List<Map<String, Object>> CYCLE;
        public List<TransitionData> TRANSITIONS;


        public static class TransitionData {
            public String ON_CONDITION;
            public Object GO_TO;
            public String NO_REPEAT;
        }
    }
}
