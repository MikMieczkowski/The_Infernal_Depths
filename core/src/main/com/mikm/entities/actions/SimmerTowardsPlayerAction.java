package com.mikm.entities.actions;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm.utils.ExtraMathUtils;
import com.mikm._components.Copyable;
import com.mikm._components.Transform;
import com.mikm._components.CombatComponent;
import com.mikm._components.WorldColliderComponent;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.sound.SoundEffects;

public class SimmerTowardsPlayerAction extends Action {
    @Copyable private float SPEED_MIN;
    @Copyable private float SPEED_MAX;
    @Copyable private Float SLIME_TRAIL_KNOCKBACK;
    @Copyable private Float SLIME_TRAIL_DAMAGE;
    @Copyable private String SLIME_TRAIL_SOUND_EFFECT;

    private static final ComponentMapper<SimmerTowardsPlayerActionComponent> MAPPER = ComponentMapper.getFor(SimmerTowardsPlayerActionComponent.class);
    class SimmerTowardsPlayerActionComponent implements Component {
        float distanceTraveledSinceLastProjectile;
        float angle;
    }

    public SimmerTowardsPlayerAction(){}

    @Override
    public Component createActionComponent() {
        return new SimmerTowardsPlayerActionComponent();
    }

    @Override
    public void postConfigRead(Entity entity) {
        super.postConfigRead(entity);
        if (SLIME_TRAIL_KNOCKBACK == null) {
            SLIME_TRAIL_KNOCKBACK = 1F;
        }
        if (SLIME_TRAIL_DAMAGE == null) {
            SLIME_TRAIL_DAMAGE = 1F;
        }
    }

    @Override
    public void enter(Entity entity) {
        super.enter(entity);
        Transform transform = Transform.MAPPER.get(entity);
        SimmerTowardsPlayerActionComponent data = MAPPER.get(entity);
        Transform playerTransform = Application.getInstance().getPlayerTransform();
        
        data.angle = MathUtils.atan2(playerTransform.y - transform.y, playerTransform.x - transform.x);
    }

    @Override
    public void update(Entity entity) {
        super.update(entity);
        SimmerTowardsPlayerActionComponent data = MAPPER.get(entity);
        RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(entity);

        float moveSpeed = ExtraMathUtils.lerp(routineListComponent.timeElapsedInCurrentAction, MAX_TIME, .3f, 1, SPEED_MIN, SPEED_MAX);
        moveTowardsPlayer(entity, moveSpeed);
        handleSlimeTrail(entity, moveSpeed);
    }

    private void moveTowardsPlayer(Entity entity, float moveSpeed) {
        Transform transform = Transform.MAPPER.get(entity);
        SimmerTowardsPlayerActionComponent data = MAPPER.get(entity);
        Transform playerTransform = Application.getInstance().getPlayerTransform();
        
        float angleToPlayer = MathUtils.atan2(playerTransform.y - transform.y, playerTransform.x - transform.x);
        data.angle = ExtraMathUtils.lerpAngle(.5f, MAX_TIME, data.angle, angleToPlayer);
        transform.xVel = MathUtils.cos(data.angle) * moveSpeed;
        transform.yVel =  MathUtils.sin(data.angle) * moveSpeed;
    }

    private void handleSlimeTrail(Entity entity, float moveSpeed) {
        Transform transform = Transform.MAPPER.get(entity);
        WorldColliderComponent collider = WorldColliderComponent.MAPPER.get(entity);
        CombatComponent combatComponent = CombatComponent.MAPPER.get(entity);
        SimmerTowardsPlayerActionComponent data = MAPPER.get(entity);
        
        data.distanceTraveledSinceLastProjectile += moveSpeed;
        if (data.distanceTraveledSinceLastProjectile > 20) {
            SoundEffects.play(SLIME_TRAIL_SOUND_EFFECT);
            data.distanceTraveledSinceLastProjectile -= 20;
            com.badlogic.gdx.math.Circle hitbox = collider.getHitbox(transform);
            //TODO projectiles/combat, particles
            //Application.getInstance().currentScreen.addInanimateEntity(new StaticProjectile(null, false, new DamageInformation(0, SLIME_TRAIL_KNOCKBACK, combatComponent.DAMAGE), hitbox.x, hitbox.y));
            //new ParticleEffect(ParticleTypes.getSlimeTrailParameters(), hitbox.x, hitbox.y);
        }
    }
}
