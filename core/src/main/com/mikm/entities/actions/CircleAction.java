package com.mikm.entities.actions;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm.utils.DeltaTime;
import com.mikm._components.Copyable;
import com.mikm._components.Transform;
import com.mikm._components.WorldColliderComponent;
import com.mikm.rendering.screens.Application;

public class CircleAction extends Action {
    @Copyable private float ANGULAR_SPEED;
    @Copyable private float SPEED = 0;

    private static final ComponentMapper<CircleActionComponent> MAPPER = ComponentMapper.getFor(CircleActionComponent.class);
    class CircleActionComponent implements Component {
        float angle;
        float distanceTraveledSinceLastProjectile = 0;
    }

    @Override
    public Component createActionComponent() {
        return new CircleActionComponent();
    }

    public CircleAction(){}

    @Override
    public void postConfigRead(Entity entity) {
        super.postConfigRead(entity);
        if (SPEED == 0) {
            SPEED = 1f;
        }
    }

    @Override
    public void enter(Entity entity) {
        super.enter(entity);
        Transform transform = Transform.MAPPER.get(entity);
        CircleActionComponent data = MAPPER.get(entity);
        WorldColliderComponent collider = WorldColliderComponent.MAPPER.get(entity);
        
        // Seed starting angle relative to player so initial frame is not visually idle
        com.badlogic.gdx.math.Circle playerHitbox = Application.getInstance().getPlayerHitbox();
        com.badlogic.gdx.math.Circle entityHitbox = collider.getHitbox(transform);
        float angleToPlayer = MathUtils.atan2(
                playerHitbox.y - entityHitbox.y,
                playerHitbox.x - entityHitbox.x);
        // Start perpendicular for a natural circular path around the player
        data.angle = angleToPlayer + MathUtils.PI / 2f;
        float globalSpeed = transform.SPEED;
        transform.xVel = SPEED * globalSpeed * MathUtils.cos(data.angle);
        transform.yVel = SPEED * globalSpeed * MathUtils.sin(data.angle);
    }

    @Override
    public void update(Entity entity) {
        super.update(entity);
        Transform transform = Transform.MAPPER.get(entity);
        CircleActionComponent data = MAPPER.get(entity);
        RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(entity);
        
        data.angle += ANGULAR_SPEED * DeltaTime.deltaTimeMultiplier();
        transform.height = 3 + MathUtils.sin(routineListComponent.timeElapsedInCurrentAction * 3) * 3;
        float globalSpeed = transform.SPEED;
        transform.xVel = SPEED * globalSpeed * MathUtils.cos(data.angle);
        transform.yVel = SPEED * globalSpeed * MathUtils.sin(data.angle);
        data.distanceTraveledSinceLastProjectile += SPEED * globalSpeed;
        if (data.distanceTraveledSinceLastProjectile > 10) {
            //TODO particle usage
            //new ParticleEffect(ParticleTypes.getLightningParameters(), transform.x, transform.y);
            data.distanceTraveledSinceLastProjectile = 0;
        }
    }
}
