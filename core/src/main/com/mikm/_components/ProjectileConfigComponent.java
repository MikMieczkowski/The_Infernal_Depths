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

    /** Duration hitbox is active (0 = always active, >0 = only active for this duration after spawn) */
    @Copyable
    public float hitboxActiveDuration;

    /**
     * Checks if the projectile has expired.
     *
     * @return true if lifetime has been exceeded
     */
    public boolean isExpired() {
        return lifetimeTimer >= lifetime;
    }

    /**
     * Checks if the hitbox is currently active.
     * If hitboxActiveDuration is 0, the hitbox is always active.
     * Otherwise, hitbox is only active for the first hitboxActiveDuration seconds.
     *
     * @return true if the hitbox can deal damage
     */
    public boolean isHitboxActive() {
        return hitboxActiveDuration <= 0 || lifetimeTimer < hitboxActiveDuration;
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
