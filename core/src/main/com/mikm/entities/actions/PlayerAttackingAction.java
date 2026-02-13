package com.mikm.entities.actions;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.mikm._components.ComboStateComponent;
import com.mikm._components.LockOnComponent;
import com.mikm._components.RuntimeDataComponent;
import com.mikm._components.Transform;
import com.mikm._components.PlayerCombatComponent;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm._systems.LockOnSystem;
import com.mikm.entities.prefabLoader.Blackboard;
import com.mikm.entities.prefabLoader.attack.AttackFormattedData;
import com.mikm.utils.ExtraMathUtils;

@RuntimeDataComponent
public class PlayerAttackingAction extends AcceleratedMoveAction {
    float sliceWidthMulAdtiplier = 1;
    final float SPEED = 1;

    private static final ComponentMapper<PlayerAttackingActionComponent> MAPPER = ComponentMapper.getFor(PlayerAttackingActionComponent.class);

    static class PlayerAttackingActionComponent implements Component {
        float peakSpeed;
        float accelerationProportion;
        float decelerationSpeed;
        float currentSpeed;
        float angleToEnemy;
        float distanceScale;
    }

    public PlayerAttackingAction() {
    }

    @Override
    public Component createActionComponent() {
        return new PlayerAttackingActionComponent();
    }

    @Override
    public void enter(Entity entity) {
        super.enter(entity);
        Transform transform = Transform.MAPPER.get(entity);
        PlayerCombatComponent playerCombatComponent = PlayerCombatComponent.MAPPER.get(entity);
        ComboStateComponent comboState = ComboStateComponent.MAPPER.get(entity);
        PlayerAttackingActionComponent actionComp = MAPPER.get(entity);

        Blackboard.getInstance().bind("idleTimer", entity, 0);

        // Get attack direction from lock-on system
        float angleToEnemy = LockOnSystem.getAngleToLockedEnemy(entity);
        actionComp.angleToEnemy = angleToEnemy;
        transform.direction = com.mikm.utils.ExtraMathUtils.angleToVector2Int(angleToEnemy);

        // Load movement config from current attack data
        if (comboState != null && comboState.currentAttackData != null) {
            System.out.println("WM " + comboState.currentAttackData.NAME);

            AttackFormattedData attackData = comboState.currentAttackData;
            MAX_TIME = attackData.getAttackMaxTime();
            actionComp.peakSpeed = attackData.MOVEMENT_CONFIG.PEAK_SPEED;
            actionComp.accelerationProportion = attackData.MOVEMENT_CONFIG.ACCELERATION_PROPORTION;
            actionComp.decelerationSpeed = attackData.MOVEMENT_CONFIG.DECELERATION_SPEED;

            // Distance scaling: three zones based on distance to locked enemy.
            //   [0, NEAR)     — scale down (power curve), player doesn't overshoot
            //   [NEAR, FAR]   — unaffected, full PEAK_SPEED
            //   (FAR, inf)    — scale up, player closes the gap
            // MULTIPLIER controls fraction of distance covered in scaled zones.
            float NEAR = attackData.MOVEMENT_CONFIG.DISTANCE_SCALE_NEAR;
            float FAR = attackData.MOVEMENT_CONFIG.DISTANCE_SCALE_FAR;
            float MULT = attackData.MOVEMENT_CONFIG.DISTANCE_SCALE_MULTIPLIER;

            LockOnComponent lockOn = LockOnComponent.MAPPER.get(entity);
            float distance = -1;
            if (lockOn != null && lockOn.hasLock()) {
                Transform enemyTransform = Transform.MAPPER.get(lockOn.lockedEnemy);
                if (enemyTransform != null) {
                    distance = ExtraMathUtils.distance(
                            transform.getCenteredX(), transform.getCenteredY(),
                            enemyTransform.getCenteredX(), enemyTransform.getCenteredY());
                    //Account for enemy hitbox
                    distance = Math.max(0, distance-10);
                }
            }

            if (distance >= 0 && NEAR > 0 && distance < NEAR) {
                // NEAR zone: power curve (more aggressive than linear near enemy)
                float t = distance / NEAR;
                actionComp.distanceScale = (float) Math.pow(t, 1.5) * MULT;
            } else if (distance >= 0 && FAR > 0 && distance > FAR) {
                // FAR zone: linear scale up
                actionComp.distanceScale = (distance / FAR) * MULT;
            } else {
                // Unaffected zone (or no lock-on)
                actionComp.distanceScale = 1f;
            }
        } else {
            // Default values if no combo state
            MAX_TIME = 0.5f;
            actionComp.peakSpeed = 0f;
            actionComp.accelerationProportion = 0f;
            actionComp.decelerationSpeed = 0f;
            actionComp.distanceScale = 1f;
        }

        actionComp.currentSpeed = 0f;
    }

    @Override
    public void update(Entity entity) {
        super.update(entity);
        Transform transform = Transform.MAPPER.get(entity);
        PlayerAttackingActionComponent actionComp = MAPPER.get(entity);
        RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(entity);

        // Update angle to locked enemy if lock-on exists
        LockOnComponent lockOn = LockOnComponent.MAPPER.get(entity);
        if (lockOn != null && lockOn.hasLock()) {
            actionComp.angleToEnemy = LockOnSystem.getAngleToLockedEnemy(entity);
        }

        // Apply movement toward/away from locked enemy based on MOVEMENT_CONFIG
        actionComp.currentSpeed = ExtraMathUtils.accDecLerp(routineListComponent.timeElapsedInCurrentAction, MAX_TIME,
                actionComp.peakSpeed * actionComp.distanceScale, actionComp.accelerationProportion, actionComp.decelerationSpeed);

        // Apply velocity toward locked enemy, scaled by global entity speed
        float globalSpeed = transform.SPEED;
        transform.xVel = MathUtils.cos(actionComp.angleToEnemy) * actionComp.currentSpeed * globalSpeed;
        transform.yVel = MathUtils.sin(actionComp.angleToEnemy) * actionComp.currentSpeed * globalSpeed;
    }

    @Override
    public void onExit(Entity entity) {
        PlayerCombatComponent playerCombatComponent = PlayerCombatComponent.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);

        // Reset velocity
        transform.xVel = 0;
        transform.yVel = 0;

        super.onExit(entity);
    }
}
