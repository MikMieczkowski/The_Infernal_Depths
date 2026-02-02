package com.mikm._systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mikm._components.CombatComponent;
import com.mikm._components.LockOnComponent;
import com.mikm._components.Transform;
import com.mikm.input.GameInput;
import com.mikm.rendering.screens.Application;
import com.mikm.utils.DeltaTime;

/**
 * System that manages lock-on targeting.
 * Handles auto-locking nearest enemy, manual lock switching,
 * and lock indicator visual updates.
 */
public class LockOnSystem extends EntitySystem {

    private Family enemyFamily;
    private Entity player;

    private static final float PULSE_SPEED = 4f;
    private static final float PULSE_MAGNITUDE = 0.15f;
    private static final float ROTATION_SPEED = 90f; // degrees per second

    @Override
    public void addedToEngine(Engine engine) {
        // Define the family for entities that can be locked onto
        enemyFamily = Family.all(CombatComponent.class, Transform.class).get();
    }

    /**
     * Gets all current enemies dynamically (enemies may be spawned/removed at runtime).
     */
    private ImmutableArray<Entity> getEnemies() {
        return getEngine().getEntitiesFor(enemyFamily);
    }

    @Override
    public void update(float deltaTime) {
        if (!Application.getInstance().systemShouldTick()) {
            return;
        }

        player = Application.getInstance().getPlayer();
        if (player == null) {
            return;
        }

        LockOnComponent lockOn = LockOnComponent.MAPPER.get(player);
        if (lockOn == null) {
            return;
        }

        Transform playerTransform = Transform.MAPPER.get(player);

        // Handle manual lock input
        handleLockInput(lockOn, playerTransform);

        // Auto-lock if no current lock
        if (!lockOn.hasLock()) {
            autoLockNearest(lockOn, playerTransform);
        }

        // Validate current lock (check if enemy is still valid)
        validateCurrentLock(lockOn, playerTransform);

        // Update lock indicator visuals
        updateLockIndicator(lockOn, DeltaTime.deltaTimeMultiplier());

        // Update cached distance for combo evaluation
        updateCachedDistance(lockOn, playerTransform);
    }

    /**
     * Handles manual lock-on input.
     */
    private void handleLockInput(LockOnComponent lockOn, Transform playerTransform) {
        // Right click / L2 to cycle lock clockwise
        if (GameInput.isLockButtonJustPressed()) {
            cycleLock(lockOn, playerTransform, true);
        }

        // Controller cycling (R1/R2)
        if (GameInput.isLockCycleNextJustPressed()) {
            cycleLock(lockOn, playerTransform, true);
        }
        if (GameInput.isLockCyclePrevJustPressed()) {
            cycleLock(lockOn, playerTransform, false);
        }
    }

    /**
     * Auto-locks to the nearest enemy within range.
     */
    private void autoLockNearest(LockOnComponent lockOn, Transform playerTransform) {
        Entity nearest = null;
        float nearestDist = LockOnComponent.AUTO_LOCK_RANGE;

        for (Entity enemy : getEnemies()) {
            if (enemy == player) continue;

            Transform enemyTransform = Transform.MAPPER.get(enemy);
            CombatComponent combat = CombatComponent.MAPPER.get(enemy);

            // Skip dead or non-attackable enemies
            if (combat.dead) continue;

            float dist = distance(playerTransform, enemyTransform);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = enemy;
            }
        }

        if (nearest != null) {
            lockOn.setLock(nearest, false);
        }
    }

    /**
     * Finds the enemy closest to the mouse cursor.
     */
    private Entity findEnemyClosestToMouse(Transform playerTransform) {
        Vector2 mousePos = GameInput.mousePosRelativeToPlayer();
        float mouseWorldX = playerTransform.getCenteredX() + mousePos.x;
        float mouseWorldY = playerTransform.getCenteredY() + mousePos.y;

        Entity closest = null;
        float closestDist = LockOnComponent.MANUAL_LOCK_RANGE;

        for (Entity enemy : getEnemies()) {
            if (enemy == player) continue;

            Transform enemyTransform = Transform.MAPPER.get(enemy);
            CombatComponent combat = CombatComponent.MAPPER.get(enemy);

            if (combat.dead) continue;

            float dist = Vector2.dst(mouseWorldX, mouseWorldY, enemyTransform.getCenteredX(), enemyTransform.getCenteredY());
            if (dist < closestDist) {
                closestDist = dist;
                closest = enemy;
            }
        }

        return closest;
    }

    /**
     * Cycles the lock to the next/previous enemy in a clockwise/counterclockwise direction.
     */
    private void cycleLock(LockOnComponent lockOn, Transform playerTransform, boolean clockwise) {
        if (!lockOn.hasLock()) {
            autoLockNearest(lockOn, playerTransform);
            return;
        }

        Transform currentLockTransform = Transform.MAPPER.get(lockOn.lockedEnemy);
        if (currentLockTransform == null) {
            lockOn.clearLock();
            return;
        }

        // Calculate angle to current lock
        float currentAngle = MathUtils.atan2(
                currentLockTransform.getCenteredY() - playerTransform.getCenteredY(),
                currentLockTransform.getCenteredX() - playerTransform.getCenteredX()
        );

        Entity bestNext = null;
        float bestAngleDiff = Float.MAX_VALUE;

        for (Entity enemy : getEnemies()) {
            if (enemy == player || enemy == lockOn.lockedEnemy) continue;

            Transform enemyTransform = Transform.MAPPER.get(enemy);
            CombatComponent combat = CombatComponent.MAPPER.get(enemy);

            if (combat.dead) continue;

            float dist = distance(playerTransform, enemyTransform);
            if (dist > LockOnComponent.AUTO_LOCK_RANGE) continue;

            float angle = MathUtils.atan2(
                    enemyTransform.getCenteredY() - playerTransform.getCenteredY(),
                    enemyTransform.getCenteredX() - playerTransform.getCenteredX()
            );

            float angleDiff;
            if (clockwise) {
                angleDiff = (currentAngle - angle + MathUtils.PI2) % MathUtils.PI2;
            } else {
                angleDiff = (angle - currentAngle + MathUtils.PI2) % MathUtils.PI2;
            }

            if (angleDiff > 0 && angleDiff < bestAngleDiff) {
                bestAngleDiff = angleDiff;
                bestNext = enemy;
            }
        }

        if (bestNext != null) {
            lockOn.setLock(bestNext, true);
        }
    }

    /**
     * Validates that the current lock is still valid (enemy exists and is alive).
     */
    private void validateCurrentLock(LockOnComponent lockOn, Transform playerTransform) {
        if (!lockOn.hasLock()) return;

        CombatComponent combat = CombatComponent.MAPPER.get(lockOn.lockedEnemy);
        if (combat == null || combat.dead) {
            lockOn.clearLock();
            return;
        }

        // Clear lock if enemy is out of auto-lock range
        Transform enemyTransform = Transform.MAPPER.get(lockOn.lockedEnemy);
        if (enemyTransform == null || distance(playerTransform, enemyTransform) > LockOnComponent.AUTO_LOCK_RANGE) {
            lockOn.clearLock();
        }
    }

    /**
     * Updates the lock indicator visual effects.
     */
    private void updateLockIndicator(LockOnComponent lockOn, float deltaTime) {
        // Rotation animation
        lockOn.lockRotation += ROTATION_SPEED * deltaTime;
        if (lockOn.lockRotation >= 360f) {
            lockOn.lockRotation -= 360f;
        }

        // Pulse animation
        lockOn.lockPulseTimer += PULSE_SPEED * deltaTime;
        lockOn.lockPulseScale = 1f + MathUtils.sin(lockOn.lockPulseTimer) * PULSE_MAGNITUDE;
    }

    /**
     * Updates the cached distance to the locked enemy.
     * Called when attack is pressed for combo evaluation.
     */
    private void updateCachedDistance(LockOnComponent lockOn, Transform playerTransform) {
        if (lockOn.hasLock()) {
            Transform enemyTransform = Transform.MAPPER.get(lockOn.lockedEnemy);
            if (enemyTransform != null) {
                lockOn.cachedDistance = distance(playerTransform, enemyTransform);
            }
        }
    }

    /**
     * Caches the current distance to the locked enemy.
     * Should be called when an attack input is detected.
     */
    public void cacheDistanceForCombo(LockOnComponent lockOn, Transform playerTransform) {
        if (lockOn.hasLock()) {
            Transform enemyTransform = Transform.MAPPER.get(lockOn.lockedEnemy);
            if (enemyTransform != null) {
                lockOn.cachedDistance = distance(playerTransform, enemyTransform);
            }
        } else {
            lockOn.cachedDistance = Float.MAX_VALUE; // No lock = infinite distance
        }
    }

    /**
     * Gets the angle from player to locked enemy.
     */
    public static float getAngleToLockedEnemy(Entity player) {
        LockOnComponent lockOn = LockOnComponent.MAPPER.get(player);
        if (lockOn == null || !lockOn.hasLock()) {
            return GameInput.getAttackingAngle(); // Fallback to mouse/stick direction
        }

        Transform playerTransform = Transform.MAPPER.get(player);
        Transform enemyTransform = Transform.MAPPER.get(lockOn.lockedEnemy);

        if (enemyTransform == null) {
            return GameInput.getAttackingAngle();
        }

        return MathUtils.atan2(
                enemyTransform.getCenteredY() - playerTransform.getCenteredY(),
                enemyTransform.getCenteredX() - playerTransform.getCenteredX()
        );
    }

    private float distance(Transform a, Transform b) {
        return Vector2.dst(a.getCenteredX(), a.getCenteredY(), b.getCenteredX(), b.getCenteredY());
    }
}
