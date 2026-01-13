package com.mikm.entities.actions;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.mikm._components.RuntimeDataComponent;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm.utils.ExtraMathUtils;
import com.mikm._components.Transform;
import com.mikm._components.PlayerCombatComponent;
import com.mikm.entities.prefabLoader.Blackboard;
import com.mikm.input.GameInput;

@RuntimeDataComponent
public class PlayerAttackingAction extends AcceleratedMoveAction {
    float sliceWidthMultiplier = 1;
    final float SPEED = 1;

    private static final ComponentMapper<PlayerAttackingActionComponent> MAPPER = ComponentMapper.getFor(PlayerAttackingActionComponent.class);
    class PlayerAttackingActionComponent implements Component {
        // No state needed
    }

    public PlayerAttackingAction() {
    }

    @Override
    public Component createActionComponent() {
        return new PlayerAttackingActionComponent();
    }

    //TODO weapons
    @Override
    public void enter(Entity entity) {
        super.enter(entity);
        Transform transform = Transform.MAPPER.get(entity);
        PlayerCombatComponent playerCombatComponent = PlayerCombatComponent.MAPPER.get(entity);

        Blackboard.getInstance().bind("idleTimer", entity, 0);
        //playerCombatComponent.currentHeldItem.enterAttackState();
        transform.direction = GameInput.getAttackingDirectionInt();
        //MAX_TIME = playerCombatComponent.currentHeldItem.MAX_TIME;
        MAX_TIME = 0f;
    }

    @Override
    public void update(Entity entity) {
        super.update(entity);
        PlayerCombatComponent playerCombatComponent = PlayerCombatComponent.MAPPER.get(entity);
        RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(entity);
        //playerCombatComponent.currentHeldItem.updateDuringAttackState();
        //TODO: implement angleTo and lockedIn
        //float angleToLocked = Application.playerOLD.angleTo(Application.playerOLD.lockedIn);
        float skewLerp = ExtraMathUtils.skewedSinLerp(routineListComponent.timeElapsedInCurrentAction, MAX_TIME, 3, .2f, 100);
        //player.xVel = -MathUtils.cos(angleToLocked) * skewLerp * SPEED;
        //player.yVel = -MathUtils.sin(angleToLocked) * skewLerp * SPEED;
    }

    @Override
    public void onExit(Entity entity) {
        PlayerCombatComponent playerCombatComponent = PlayerCombatComponent.MAPPER.get(entity);
        //playerCombatComponent.currentHeldItem.exitAttackState();
        super.onExit(entity);
    }

}
