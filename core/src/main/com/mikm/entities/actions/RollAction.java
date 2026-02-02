package com.mikm.entities.actions;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mikm._components.*;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm.utils.DeltaTime;
import com.mikm.utils.RandomUtils;
import com.mikm.entities.DamageInformation;
import com.mikm.entities.inanimateEntities.projectiles.Hurtbox;
import com.mikm.input.GameInput;
import com.mikm.rendering.sound.SoundEffects;

public class RollAction extends Action {
    @Copyable private int DAMAGE;
    @Copyable private int KNOCKBACK_MULTIPLIER;
    @Copyable private int HURTBOX_DIAMETER;
    @Copyable private float END_EARLY;
    @Copyable private float SPEED;
    @Copyable private float STARTING_SIN_COUNT;
    @Copyable private float FRICTION;
    @Copyable private float FRICTION_SPEED;
    @Copyable private float MAX_TIME;
    @Copyable private float JUMP_SPEED;
    @Copyable private float JUMP_HEIGHT;
    @Copyable private String END_SOUND_EFFECT;

    private static final ComponentMapper<RollActionComponent> MAPPER = ComponentMapper.getFor(RollActionComponent.class);
    class RollActionComponent implements Component {
        Vector2 rollVel = new Vector2();
        float rollSpeedSinCounter;
        float heightSinCounter;
        boolean jumpDone = false;
        Hurtbox hurtbox;
    }

    public RollAction(){}

    @Override
    public Component createActionComponent() {
        return new RollActionComponent();
    }

    @Override
    public void postConfigRead(Entity entity) {
        super.postConfigRead(entity);
        RollActionComponent data = MAPPER.get(entity);
        data.hurtbox = new Hurtbox(HURTBOX_DIAMETER, false);
    }

    @Override
    public void enter(Entity entity) {
        super.enter(entity);
        Transform transform = Transform.MAPPER.get(entity);
        RollActionComponent data = MAPPER.get(entity);
        
        transform.xVel = 0;
        transform.yVel = 0;
        data.heightSinCounter = 0;
        data.jumpDone = false;
        data.rollSpeedSinCounter = STARTING_SIN_COUNT;
    }

    @Override
    public void onExit(Entity entity) {
        super.onExit(entity);
    }

    @Override
    public void update(Entity entity) {
        super.update(entity);
        Transform transform = Transform.MAPPER.get(entity);
        RollActionComponent data = MAPPER.get(entity);
        RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(entity);
        
        setRollForce(entity);
        setJumpHeight(entity);
        transform.xVel = data.rollVel.x;
        transform.yVel = data.rollVel.y;
        if (data.rollSpeedSinCounter >= MathUtils.PI - END_EARLY) {
            routineListComponent.CURRENT_ACTION_IS_DONE = true;
        }
    }

    private void setRollForce(Entity entity) {
        RollActionComponent data = MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);
        if (data.rollSpeedSinCounter < MathUtils.PI - END_EARLY) {
            data.rollSpeedSinCounter += (FRICTION - (FRICTION_SPEED * FRICTION * data.rollSpeedSinCounter)) * DeltaTime.deltaTimeMultiplier();
        }

        float globalSpeed = transform.SPEED;
        data.rollVel = new Vector2(SPEED * globalSpeed * MathUtils.sin(data.rollSpeedSinCounter) * GameInput.getHorizontalAxis(),
                SPEED * globalSpeed * MathUtils.sin(data.rollSpeedSinCounter) * GameInput.getVerticalAxis());
    }

    private void setJumpHeight(Entity entity) {
        Transform transform = Transform.MAPPER.get(entity);
        WorldColliderComponent collider = WorldColliderComponent.MAPPER.get(entity);
        CombatComponent combatComponent = CombatComponent.MAPPER.get(entity);
        RollActionComponent data = MAPPER.get(entity);
        
        if (!data.jumpDone) {
            if (data.heightSinCounter < MathUtils.PI) {
                data.heightSinCounter += JUMP_SPEED * DeltaTime.deltaTimeMultiplier();
            }
            if (data.heightSinCounter >= MathUtils.PI) {
                data.heightSinCounter = 0;
                com.badlogic.gdx.math.Circle hitbox = collider.getHitbox(transform);
                data.hurtbox.setPosition(hitbox.x, hitbox.y, 0, 0);
                data.hurtbox.setDamageInformation(new DamageInformation(RandomUtils.getFloat(0, MathUtils.PI2), combatComponent.KNOCKBACK, combatComponent.DAMAGE));
                data.hurtbox.checkIfHitEntities(true);
                //TODO particle
                //new ParticleEffect(ParticleTypes.getDiveDustParameters(), hitbox.x, transform.getBounds().y - 3);
                SoundEffects.playLoud(END_SOUND_EFFECT);
                EffectsComponent.MAPPER.get(entity).startSquish(0.01f, 1.2f);
                data.jumpDone = true;
            }
            transform.height = JUMP_HEIGHT * MathUtils.sin(data.heightSinCounter);
        }
    }
}
