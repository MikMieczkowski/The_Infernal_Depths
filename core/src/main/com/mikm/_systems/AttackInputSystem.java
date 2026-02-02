package com.mikm._systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mikm._components.AttackInputComponent;
import com.mikm._components.ComboStateComponent;
import com.mikm._components.LockOnComponent;
import com.mikm._components.Transform;
import com.mikm.entities.prefabLoader.attack.AttackFormattedData;
import com.mikm.entities.prefabLoader.weapon.AttackDuration;
import com.mikm.input.GameInput;
import com.mikm.rendering.screens.Application;
import com.mikm.utils.DeltaTime;

/**
 * System that handles attack input.
 * Tracks hold time, determines attack duration, and triggers combo traversal.
 */
public class AttackInputSystem extends IteratingSystem {

    public AttackInputSystem() {
        super(Family.all(AttackInputComponent.class, ComboStateComponent.class, Transform.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        if (!Application.getInstance().systemShouldTick()) {
            return;
        }

        AttackInputComponent input = AttackInputComponent.MAPPER.get(entity);
        ComboStateComponent combo = ComboStateComponent.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);

        // Update attack button state tracking
        GameInput.updateAttackButtonState();

        float dt = DeltaTime.deltaTimeMultiplier();

        // Handle attack button press
        if (GameInput.isAttackButtonJustPressed()) {
            onAttackButtonPressed(entity, input, combo);
        }

        // Update hold timer while holding
        if (input.isHolding && GameInput.isAttackButtonPressed()) {
            input.updateHold(dt);

            // Handle HOLD projectile spawning
            handleHoldProjectiles(entity, input, combo);
        }

        // Handle attack button release
        if (input.isHolding && !GameInput.isAttackButtonPressed()) {
            onAttackButtonReleased(entity, input, combo);
        }

        // Update combo timer
        combo.updateComboTimer(dt);

        // Update attack timer
        if (combo.updateAttackTimer(dt)) {
            onAttackEnded(entity, combo);
        }
    }

    /**
     * Called when attack button is first pressed.
     */
    private void onAttackButtonPressed(Entity entity, AttackInputComponent input, ComboStateComponent combo) {
        // Don't start new attack if already attacking (queue instead)
        if (combo.isAttacking) {
            input.attackQueued = true;
            return;
        }

        input.startHold();

        // Cache distance for combo evaluation
        LockOnComponent lockOn = LockOnComponent.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);
        if (lockOn != null) {
            LockOnSystem lockOnSystem = getEngine().getSystem(LockOnSystem.class);
            if (lockOnSystem != null) {
                lockOnSystem.cacheDistanceForCombo(lockOn, transform);
            }
        }
        // Note: PRESS projectiles are spawned in onAttackButtonReleased after attack selection
    }

    /**
     * Called when attack button is released.
     */
    private void onAttackButtonReleased(Entity entity, AttackInputComponent input, ComboStateComponent combo) {
        // Determine attack duration based on hold time
        AttackDuration duration = getAttackDuration(input.holdTimer);
        input.wasLightAttack = (duration == AttackDuration.LIGHT);

        // Get cached distance
        float distance = getCachedDistance(entity);

        // Trigger combo system to execute attack
        ComboSystem comboSystem = getEngine().getSystem(ComboSystem.class);
        if (comboSystem != null) {
            comboSystem.executeAttack(entity, duration, distance);
        }

        // Now that attack is selected, spawn PRESS projectiles (spawn at attack start)
        handlePressProjectiles(entity, combo);

        // Handle RELEASE projectiles
        handleReleaseProjectiles(entity, combo);

        input.reset();
    }

    /**
     * Called when an attack animation ends.
     */
    private void onAttackEnded(Entity entity, ComboStateComponent combo) {
        AttackInputComponent input = AttackInputComponent.MAPPER.get(entity);

        // If attack was queued, process it now
        if (input != null && input.attackQueued) {
            input.attackQueued = false;
            input.startHold();
        }
    }

    /**
     * Determines attack duration based on hold time.
     */
    private AttackDuration getAttackDuration(float holdTime) {
        if (holdTime >= AttackInputComponent.MEDIUM_THRESHOLD) {
            return AttackDuration.HEAVY;
        } else if (holdTime >= AttackInputComponent.LIGHT_THRESHOLD) {
            return AttackDuration.MEDIUM;
        } else {
            return AttackDuration.LIGHT;
        }
    }

    /**
     * Gets the cached distance from the lock-on component.
     */
    private float getCachedDistance(Entity entity) {
        LockOnComponent lockOn = LockOnComponent.MAPPER.get(entity);
        if (lockOn != null) {
            return lockOn.cachedDistance;
        }
        return Float.MAX_VALUE; // No lock = treat as infinite distance
    }

    /**
     * Handles projectile spawning on PRESS.
     */
    private void handlePressProjectiles(Entity entity, ComboStateComponent combo) {
        ProjectileSpawnSystem spawnSystem = getEngine().getSystem(ProjectileSpawnSystem.class);
        if (spawnSystem != null && combo.currentAttackData != null) {
            spawnSystem.spawnProjectiles(entity, combo.currentAttackData, "PRESS");
        }
    }

    /**
     * Handles projectile spawning on RELEASE.
     */
    private void handleReleaseProjectiles(Entity entity, ComboStateComponent combo) {
        ProjectileSpawnSystem spawnSystem = getEngine().getSystem(ProjectileSpawnSystem.class);
        if (spawnSystem != null && combo.currentAttackData != null) {
            spawnSystem.spawnProjectiles(entity, combo.currentAttackData, "RELEASE");
        }
    }

    /**
     * Handles projectile spawning during HOLD at intervals.
     */
    private void handleHoldProjectiles(Entity entity, AttackInputComponent input, ComboStateComponent combo) {
        if (combo.currentAttackData == null || combo.currentAttackData.PROJECTILES == null) {
            return;
        }

        for (AttackFormattedData.ProjectileData projectile : combo.currentAttackData.PROJECTILES) {
            if ("HOLD".equals(projectile.CREATE_ON)) {
                float interval = projectile.CREATE_ON_HOLD_INTERVAL != null
                        ? projectile.CREATE_ON_HOLD_INTERVAL : 1f;

                if (input.shouldSpawnHoldProjectile(interval)) {
                    ProjectileSpawnSystem spawnSystem = getEngine().getSystem(ProjectileSpawnSystem.class);
                    if (spawnSystem != null) {
                        spawnSystem.spawnSingleProjectile(entity, projectile);
                    }
                }
            }
        }
    }
}
