package com.mikm._components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

/**
 * Component for tracking attack input state.
 * Handles hold time for determining attack duration (light/medium/heavy).
 */
public class AttackInputComponent implements Component {
    public static final ComponentMapper<AttackInputComponent> MAPPER = ComponentMapper.getFor(AttackInputComponent.class);

    /** Hold time threshold for light attacks */
    public static final float LIGHT_THRESHOLD = 0.3f;

    /** Hold time threshold for medium attacks */
    public static final float MEDIUM_THRESHOLD = 0.6f;

    /** Whether the attack button is currently being held */
    public boolean isHolding;

    /** How long the attack button has been held */
    public float holdTimer;

    /** Whether an attack is queued to execute */
    public boolean attackQueued;

    /** Timer for HOLD projectile spawning intervals */
    public float holdProjectileTimer;

    /** Whether the last attack was a light attack (for quick taps) */
    public boolean wasLightAttack;

    /**
     * Resets the input state after an attack is executed or cancelled.
     */
    public void reset() {
        isHolding = false;
        holdTimer = 0f;
        attackQueued = false;
        holdProjectileTimer = 0f;
    }

    /**
     * Starts tracking a new hold.
     */
    public void startHold() {
        isHolding = true;
        holdTimer = 0f;
        holdProjectileTimer = 0f;
    }

    /**
     * Updates the hold timer.
     *
     * @param deltaTime Time since last frame
     */
    public void updateHold(float deltaTime) {
        if (isHolding) {
            holdTimer += deltaTime;
            holdProjectileTimer += deltaTime;
        }
    }

    /**
     * Checks if the hold projectile timer should trigger a spawn.
     *
     * @param interval The spawn interval
     * @return true if a projectile should spawn
     */
    public boolean shouldSpawnHoldProjectile(float interval) {
        if (holdProjectileTimer >= interval) {
            holdProjectileTimer -= interval;
            return true;
        }
        return false;
    }
}
