package com.mikm._components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

/**
 * Component for tracking aerial state.
 * Used for enemies that have been launched and for combo tree switching.
 */
public class AerialStateComponent implements Component {
    public static final ComponentMapper<AerialStateComponent> MAPPER = ComponentMapper.getFor(AerialStateComponent.class);

    /** Whether this entity is currently in an aerial state */
    public boolean isAerial;

    /** Whether this entity was launched by the player (for combo tree switching) */
    public boolean wasLaunchedByPlayer;

    /** Vertical velocity for aerial physics (if needed) */
    public float aerialVelocityY;

    /** Time spent in aerial state */
    public float aerialTimer;

    /**
     * Launches the entity into aerial state.
     *
     * @param byPlayer Whether this was caused by the player
     */
    public void launch(boolean byPlayer) {
        isAerial = true;
        wasLaunchedByPlayer = byPlayer;
        aerialTimer = 0f;
    }

    /**
     * Lands the entity, ending aerial state.
     */
    public void land() {
        isAerial = false;
        wasLaunchedByPlayer = false;
        aerialVelocityY = 0f;
        aerialTimer = 0f;
    }

    /**
     * Updates the aerial timer.
     *
     * @param deltaTime Time since last frame
     */
    public void update(float deltaTime) {
        if (isAerial) {
            aerialTimer += deltaTime;
        }
    }
}
