package com.mikm.entities.prefabLoader.attack;

import java.util.List;

/**
 * POJO for attack YAML data.
 * Fields match the YAML structure directly.
 */
public class AttackFormattedData {
    public Float ATTACK_MAX_TIME;
    public Float COOLDOWN_TIME;
    public Float COMBO_TIME;
    public Boolean CAN_BREAK_ROCKS;
    public Integer HIT_STUN;
    public Boolean IS_LAUNCHER;
    public Float DAMAGE_MULTIPLIER;

    public String ENTER_SOUND_EFFECT;
    public String END_SOUND_EFFECT;

    public MovementConfigData MOVEMENT_CONFIG;
    public String PLAYER_ANIMATION;
    public List<ProjectileData> PROJECTILES;

    public static class MovementConfigData {
        public Float PEAK_SPEED;
        public Float ACCELERATION_PROPORTION;
        public Float DECELERATION_SPEED;
    }

    public static class ProjectileData {
        public String CREATE_ON;
        public Float CREATE_ON_HOLD_INTERVAL;
        public String ANIMATION_NAME;
        public Float FPS;
        public Boolean ORBITS;
        public Float SPEED;
        public Integer DAMAGE;
        public Float LIFETIME;
        public String MOVEMENT_PATTERN;
        /** Duration hitbox is active (0 = always active, >0 = only active for this duration after spawn) */
        public Float HITBOX_ACTIVE_DURATION;
    }

    /**
     * Gets the default values with null-safety.
     */
    public float getAttackMaxTime() {
        return ATTACK_MAX_TIME != null ? ATTACK_MAX_TIME : 0.5f;
    }

    public float getCooldownTime() {
        return COOLDOWN_TIME != null ? COOLDOWN_TIME : 0f;
    }

    public float getComboTime() {
        return COMBO_TIME != null ? COMBO_TIME : 0.6f;
    }

    public boolean canBreakRocks() {
        return CAN_BREAK_ROCKS != null && CAN_BREAK_ROCKS;
    }

    public int getHitStun() {
        return HIT_STUN != null ? HIT_STUN : 10;
    }

    public boolean isLauncher() {
        return IS_LAUNCHER != null && IS_LAUNCHER;
    }

    public float getDamageMultiplier() {
        return DAMAGE_MULTIPLIER != null ? DAMAGE_MULTIPLIER : 1.0f;
    }

    public float getPeakSpeed() {
        return MOVEMENT_CONFIG != null && MOVEMENT_CONFIG.PEAK_SPEED != null
                ? MOVEMENT_CONFIG.PEAK_SPEED : 0f;
    }

    public float getAccelerationProportion() {
        return MOVEMENT_CONFIG != null && MOVEMENT_CONFIG.ACCELERATION_PROPORTION != null
                ? MOVEMENT_CONFIG.ACCELERATION_PROPORTION : 0f;
    }

    public float getDecelerationSpeed() {
        return MOVEMENT_CONFIG != null && MOVEMENT_CONFIG.DECELERATION_SPEED != null
                ? MOVEMENT_CONFIG.DECELERATION_SPEED : 0f;
    }
}
