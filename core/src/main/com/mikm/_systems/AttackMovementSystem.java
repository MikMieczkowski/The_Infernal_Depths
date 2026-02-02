package com.mikm._systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mikm._components.ComboStateComponent;
import com.mikm._components.LockOnComponent;
import com.mikm._components.Transform;
import com.mikm.entities.prefabLoader.attack.AttackFormattedData;
import com.mikm.rendering.screens.Application;
import com.mikm.utils.DeltaTime;

/**
 * System that handles attack-related movement.
 * Applies MOVEMENT_CONFIG settings during attack execution.
 * This provides additional movement control beyond what PlayerAttackingAction handles.
 */
public class AttackMovementSystem extends IteratingSystem {

    public AttackMovementSystem() {
        super(Family.all(ComboStateComponent.class, Transform.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        if (!Application.getInstance().systemShouldTick()) {
            return;
        }

        ComboStateComponent combo = ComboStateComponent.MAPPER.get(entity);

        // Only process during attacks
        if (!combo.isAttacking || combo.currentAttackData == null) {
            return;
        }

        // Movement during attacks is primarily handled by PlayerAttackingAction
        // This system can be extended for additional attack movement effects:
        // - Homing movement adjustments
        // - Attack cancels
        // - Movement interrupts
        // - etc.
    }

    /**
     * Calculates the direction from attacker to locked enemy.
     *
     * @param entity The attacking entity
     * @return The direction vector (normalized), or null if no lock
     */
    public static Vector2 getDirectionToLockedEnemy(Entity entity) {
        LockOnComponent lockOn = LockOnComponent.MAPPER.get(entity);
        if (lockOn == null || !lockOn.hasLock()) {
            return null;
        }

        Transform attackerTransform = Transform.MAPPER.get(entity);
        Transform enemyTransform = Transform.MAPPER.get(lockOn.lockedEnemy);

        if (attackerTransform == null || enemyTransform == null) {
            return null;
        }

        float dx = enemyTransform.getCenteredX() - attackerTransform.getCenteredX();
        float dy = enemyTransform.getCenteredY() - attackerTransform.getCenteredY();
        float length = (float) Math.sqrt(dx * dx + dy * dy);

        if (length == 0) {
            return new Vector2(1, 0);
        }

        return new Vector2(dx / length, dy / length);
    }

    /**
     * Applies a dash movement toward or away from the locked enemy.
     *
     * @param entity The entity to move
     * @param speed The movement speed (positive = toward, negative = away)
     */
    public static void applyDashMovement(Entity entity, float speed) {
        Transform transform = Transform.MAPPER.get(entity);
        Vector2 direction = getDirectionToLockedEnemy(entity);

        if (direction != null) {
            transform.xVel = direction.x * speed;
            transform.yVel = direction.y * speed;
        }
    }

    /**
     * Gets the angle from the entity to its locked enemy.
     *
     * @param entity The entity
     * @return The angle in radians, or the attacking angle if no lock
     */
    public static float getAngleToLockedEnemy(Entity entity) {
        return LockOnSystem.getAngleToLockedEnemy(entity);
    }
}
