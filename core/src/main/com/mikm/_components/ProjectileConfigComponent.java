package com.mikm._components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

/**
 * Component for configuring projectile behavior.
 * Stores settings from attack YAML.
 */
public class ProjectileConfigComponent implements Component {
    public static final ComponentMapper<ProjectileConfigComponent> MAPPER = ComponentMapper.getFor(ProjectileConfigComponent.class);

    /** When the projectile was created: PRESS, RELEASE, or HOLD */
    @Copyable
    public String createOn;

    /** Spawn interval for HOLD type projectiles */
    @Copyable
    public float holdInterval;

    /** Damage dealt by this projectile */
    @Copyable
    public int damage;

    /** Lifetime in seconds before despawning */
    @Copyable
    public float lifetime;

    /** Timer tracking current lifetime */
    public float lifetimeTimer;

    /** Movement pattern: STRAIGHT, HOMING, WAVE, etc. */
    @Copyable
    public String movementPattern;

    /** Speed of the projectile */
    @Copyable
    public float speed;

    /** Whether the projectile orbits the player */
    @Copyable
    public boolean orbits;

    /** Animation FPS */
    @Copyable
    public float fps;

    /** Whether this projectile belongs to the player */
    @Copyable
    public boolean isPlayer;

    /** Time in seconds before hitbox becomes active after spawn (converted from STARTUP_FRAMES) */
    @Copyable
    public float startupTime;

    /** Duration in seconds hitbox is active after startup (0 or negative = always active once started, converted from ACTIVE_FRAMES) */
    @Copyable
    public float activeTime;

    /** Radius of the projectile's hitbox circle */
    @Copyable
    public float hitboxRadius;

    /**
     * Checks if the projectile has expired.
     *
     * @return true if lifetime has been exceeded
     */
    public boolean isExpired() {
        return lifetime > 0 && lifetimeTimer >= lifetime;
    }

    /**
     * Checks if the hitbox is currently active based on {@link #startupTime},
     * {@link #activeTime}, and {@link #lifetimeTimer}.
     * Also used by {@link com.mikm._systems.ProjectileHitboxDebugSystem} for debug visualization,
     * so changes here are automatically reflected in the debug drawing.
     *
     * @return true if the hitbox can deal damage
     */
    public boolean isHitboxActive() {
        if (lifetimeTimer < startupTime) {
            return false;
        }
        if (activeTime <= 0) {
            return true; // 0 or negative = infinite (active until projectile expires)
        }
        return lifetimeTimer < startupTime + activeTime;
    }

    /**
     * Updates the lifetime timer.
     *
     * @param deltaTime Time since last frame
     */
    public void updateLifetime(float deltaTime) {
        lifetimeTimer += deltaTime;
    }

    /**
     * Resets the lifetime timer.
     */
    public void resetLifetime() {
        lifetimeTimer = 0f;
    }
}
