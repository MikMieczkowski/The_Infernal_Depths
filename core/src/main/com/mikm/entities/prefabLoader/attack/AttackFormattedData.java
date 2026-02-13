package com.mikm.entities.prefabLoader.attack;

import java.util.List;

/**
 * POJO for attack YAML data.
 * Fields match the YAML structure directly.
 */
public class AttackFormattedData {
    public String NAME;
    public Integer ATTACK_FRAMES;
    public Float COMBO_TIME;
    public Boolean CAN_BREAK_ROCKS;
    public Integer HITSTUN_FRAMES;
    public Boolean IS_LAUNCHER;
    public Float DAMAGE_MULTIPLIER;

    public String ENTER_SOUND_EFFECT;
    public String END_SOUND_EFFECT;

    public MovementConfigData MOVEMENT_CONFIG;
    public String PLAYER_ANIMATION;
    public String WEAPON_ANIMATION;
    public Float WEAPON_ANIMATION_FPS;
    public Boolean WEAPON_ANIMATION_LOOP;
    public List<ProjectileData> PROJECTILES;

    public static class MovementConfigData {
        public Float PEAK_SPEED;
        public Float ACCELERATION_PROPORTION;
        public Float DECELERATION_SPEED;
        public Float DISTANCE_SCALE_FAR;
        public Float DISTANCE_SCALE_NEAR;
        public Float DISTANCE_SCALE_MULTIPLIER;
    }

    public static class ProjectileData {
        public String CREATE_ON;
        public Float CREATE_ON_HOLD_INTERVAL;
        public String ANIMATION_NAME;
        public Float FPS;
        public Boolean ORBITS;
        public Float SPEED;
        public Integer DAMAGE;
        public Integer LIFETIME_FRAMES;
        public String MOVEMENT_PATTERN;
        public Integer STARTUP_FRAMES;
        public Integer ACTIVE_FRAMES;
        public Float HITBOX_RADIUS;
        public Float WIDTH_MULTIPLIER;
        // TOWARDS_LOCKED: spawns at SPAWN_DISTANCE from player center toward locked enemy/mouse
        public String SPAWN_POSITION;
        public Float SPAWN_DISTANCE;
        // If true, projectiles at index 1+ inherit DAMAGE from index 0 projectile. No-op for index 0.
        public Boolean INHERITS_DAMAGE_FROM_ZERO;
    }

    /** Converts ATTACK_FRAMES to seconds. */
    public float getAttackMaxTime() {
        return ATTACK_FRAMES / 60.0f;
    }
}
