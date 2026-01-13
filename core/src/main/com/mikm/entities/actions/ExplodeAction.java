package com.mikm.entities.actions;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.mikm._components.Copyable;
import com.mikm._components.Transform;
import com.mikm._components.WorldColliderComponent;
import com.mikm._components.routine.RoutineListComponent;

public class ExplodeAction extends Action {
    @Copyable private float HITBOX_MULTIPLIER;
    @Copyable private int MULTIPLY_ACTIVATE_FRAME;

    private static final ComponentMapper<ExplodeActionComponent> MAPPER = ComponentMapper.getFor(ExplodeActionComponent.class);
    class ExplodeActionComponent implements Component {
        float activateTime;
        boolean activatedHitboxMultiplier = false;
    }

    public ExplodeAction(){}

    @Override
    public Component createActionComponent() {
        return new ExplodeActionComponent();
    }

    @Override
    public void postConfigRead(Entity entity) {
        super.postConfigRead(entity);
        ExplodeActionComponent data = MAPPER.get(entity);
        data.activateTime = (MULTIPLY_ACTIVATE_FRAME - 1) * animation.getFrameDuration();
    }

    @Override
    public void enter(Entity entity) {
        super.enter(entity);
        Transform transform = Transform.MAPPER.get(entity);
        transform.xVel = 0;
        transform.yVel = 0;
    }

    @Override
    public void update(Entity entity) {
        super.update(entity);
        Transform transform = Transform.MAPPER.get(entity);
        ExplodeActionComponent data = MAPPER.get(entity);
        WorldColliderComponent collider = WorldColliderComponent.MAPPER.get(entity);
        RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(entity);
        
        // Keep velocity clamped to zero during explode to avoid residual motion from collisions
        transform.xVel = 0;
        transform.yVel = 0;
        if (routineListComponent.timeElapsedInCurrentAction > data.activateTime && !data.activatedHitboxMultiplier) {
            collider.RADIUS *= HITBOX_MULTIPLIER;
            data.activatedHitboxMultiplier = true;
        }
    }

    @Override
    public void onExit(Entity entity) {
        ExplodeActionComponent data = MAPPER.get(entity);
        WorldColliderComponent collider = WorldColliderComponent.MAPPER.get(entity);
        collider.RADIUS /= HITBOX_MULTIPLIER;
        data.activatedHitboxMultiplier = false;
        super.onExit(entity);
    }
}
