package com.mikm._components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

/**
 * Component for tracking attack input state.
 * Handles hold time for determining attack duration (light/heavy).
 */
public class AttackInputComponent implements Component {
    public static final ComponentMapper<AttackInputComponent> MAPPER = ComponentMapper.getFor(AttackInputComponent.class);

    /** Hold time threshold for heavy attacks (seconds) */
    public static final float HEAVY_THRESHOLD = 0.2f;

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

    /** Saved hold time from a queued attack press/release. Negative means no saved time. */
    public float queuedHoldTime = -1f;

    /**
     * Resets the input state after an attack is executed or cancelled.
     */
    public void reset() {
        isHolding = false;
        holdTimer = 0f;
        attackQueued = false;
        holdProjectileTimer = 0f;
        queuedHoldTime = -1f;
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
