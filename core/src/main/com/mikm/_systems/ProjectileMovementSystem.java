package com.mikm._systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mikm._components.MiningProjectileComponent;
import com.mikm._components.ParticleComponent;
import com.mikm._components.ProjectileComponent;
import com.mikm._components.ProjectileConfigComponent;
import com.mikm._components.Transform;
import com.mikm.rendering.screens.Application;
import com.mikm.utils.DeltaTime;

/**
 * System that handles projectile and particle movement.
 * Also handles lifetime tracking for projectiles.
 */
public class ProjectileMovementSystem extends IteratingSystem {

    public ProjectileMovementSystem() {
        super(Family.one(ProjectileComponent.class, MiningProjectileComponent.class, ParticleComponent.class).all(Transform.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        boolean isParticle = ParticleComponent.MAPPER.get(entity) != null;
        if (!Application.getInstance().systemShouldTick() && !isParticle) {
            return;
        }

        Transform transform = Transform.MAPPER.get(entity);
        float dt = DeltaTime.deltaTimeMultiplier();

        // Apply velocity
        transform.x += transform.xVel * dt;
        transform.y += transform.yVel * dt;

        // Handle lifetime for projectiles with config
        ProjectileConfigComponent config = ProjectileConfigComponent.MAPPER.get(entity);
        if (config != null) {
            // Use real delta time (seconds) since lifetime is specified in seconds
            config.updateLifetime(deltaTime);

            // Check for expiration
            if (config.isExpired()) {
                // Remove the entity
                getEngine().removeEntity(entity);
                return;
            }

            // Apply movement pattern
            applyMovementPattern(entity, transform, config, dt);
        }
    }

    /**
     * Applies the movement pattern to the projectile.
     */
    private void applyMovementPattern(Entity entity, Transform transform, ProjectileConfigComponent config, float dt) {
        if (config.movementPattern == null) {
            return;
        }

        switch (config.movementPattern.toUpperCase()) {
            case "STRAIGHT":
                // Default behavior - no additional processing needed
                break;

            case "HOMING":
                // Future: implement homing toward locked enemy
                applyHomingMovement(entity, transform, config, dt);
                break;

            case "WAVE":
                // Future: implement wave/sine movement
                applyWaveMovement(entity, transform, config, dt);
                break;

            case "BOOMERANG":
                // Future: implement boomerang return movement
                applyBoomerangMovement(entity, transform, config, dt);
                break;

            default:
                // Unknown pattern, use straight movement
                break;
        }
    }

    /**
     * Future: Homing movement toward target.
     */
    private void applyHomingMovement(Entity entity, Transform transform, ProjectileConfigComponent config, float dt) {
        // TODO: Get target from LockOnComponent or nearest enemy
        // Adjust velocity to track toward target
        // float turnRate = 180f * dt; // degrees per second
    }

    /**
     * Future: Wave/sine movement perpendicular to travel direction.
     */
    private void applyWaveMovement(Entity entity, Transform transform, ProjectileConfigComponent config, float dt) {
        // TODO: Apply sine wave offset perpendicular to velocity
        // float amplitude = 20f;
        // float frequency = 5f;
    }

    /**
     * Future: Boomerang movement that returns to sender.
     */
    private void applyBoomerangMovement(Entity entity, Transform transform, ProjectileConfigComponent config, float dt) {
        // TODO: Reverse direction after reaching max distance or time
        // Track outgoing/returning state
    }
}
