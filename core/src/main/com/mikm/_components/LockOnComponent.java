package com.mikm._components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

/**
 * Component for lock-on targeting system.
 * Tracks the currently locked enemy and related state.
 */
public class LockOnComponent implements Component {
    public static final ComponentMapper<LockOnComponent> MAPPER = ComponentMapper.getFor(LockOnComponent.class);

    /** The currently locked enemy entity, or null if no lock */
    public Entity lockedEnemy;

    /** Distance to locked enemy cached at combo node entry */
    public float cachedDistance;

    /** True if player manually locked, false if auto-locked */
    public boolean hasExplicitLock;

    /** Lock indicator rotation for visual effect */
    public float lockRotation;

    /** Lock indicator pulse scale for visual effect */
    public float lockPulseScale = 1f;

    /** Timer for lock pulse animation */
    public float lockPulseTimer;

    /** Maximum range for auto-targeting */
    public static final float AUTO_LOCK_RANGE = 120f;

    /** Maximum range for manual targeting with mouse */
    public static final float MANUAL_LOCK_RANGE = 120f;

    /**
     * Clears the current lock.
     */
    public void clearLock() {
        lockedEnemy = null;
        cachedDistance = 0f;
        hasExplicitLock = false;
    }

    /**
     * Sets a new lock target.
     *
     * @param enemy The enemy to lock onto
     * @param explicit Whether this is an explicit (manual) lock
     */
    public void setLock(Entity enemy, boolean explicit) {
        lockedEnemy = enemy;
        hasExplicitLock = explicit;
    }

    /**
     * Checks if there is currently a locked enemy.
     *
     * @return true if an enemy is locked
     */
    public boolean hasLock() {
        return lockedEnemy != null;
    }
}
