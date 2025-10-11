package com.mikm.entities.actions;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mikm.DeltaTime;
import com.mikm.RandomUtils;
import com.mikm.entities.DamageInformation;
import com.mikm.entities.Entity;
import com.mikm.entities.inanimateEntities.particles.ParticleEffect;
import com.mikm.entities.inanimateEntities.particles.ParticleTypes;
import com.mikm.entities.inanimateEntities.projectiles.Hurtbox;
import com.mikm.entityLoader.Blackboard;
import com.mikm.input.GameInput;
import com.mikm.rendering.sound.SoundEffects;

public class RollAction extends Action {
    private Vector2 rollVel = new Vector2();
    private float rollSpeedSinCounter, heightSinCounter;
    private boolean jumpDone = false;
    private Hurtbox hurtbox;

    private int DAMAGE;
    private int KNOCKBACK_MULTIPLIER;
    private int HURTBOX_DIAMETER;
    private float END_EARLY;
    private float SPEED;
    private float STARTING_SIN_COUNT;
    private float FRICTION;
    private float FRICTION_SPEED;
    private float MAX_TIME;
    private float JUMP_SPEED;
    private float JUMP_HEIGHT;
    private String END_SOUND_EFFECT;

    public RollAction(Entity entity) {
        super(entity);
        hurtbox = new Hurtbox(HURTBOX_DIAMETER, false);
    }

    @Override
    public void enter() {
        super.enter();
        entity.xVel = 0;
        entity.yVel = 0;
        heightSinCounter = 0;
        jumpDone = false;
        rollSpeedSinCounter = STARTING_SIN_COUNT;
    }

    @Override
    public void onExit() {
        super.onExit();
    }

    @Override
    public void update() {
        super.update();
        setRollForce();
        setJumpHeight();
        entity.xVel = rollVel.x;
        entity.yVel = rollVel.y;
        if (rollSpeedSinCounter >= MathUtils.PI - END_EARLY) {
            IS_DONE = true;
        }
    }

    private void setRollForce() {
        if (rollSpeedSinCounter < MathUtils.PI - END_EARLY) {
            rollSpeedSinCounter += (FRICTION - (FRICTION_SPEED * FRICTION * rollSpeedSinCounter)) * DeltaTime.deltaTime();
        }

        rollVel = new Vector2(SPEED * MathUtils.sin(rollSpeedSinCounter) * GameInput.getHorizontalAxis(),
                SPEED * MathUtils.sin(rollSpeedSinCounter) * GameInput.getVerticalAxis());
    }

    private void setJumpHeight() {
        if (!jumpDone) {
            if (heightSinCounter < MathUtils.PI) {
                heightSinCounter += JUMP_SPEED * DeltaTime.deltaTime();
            }
            if (heightSinCounter >= MathUtils.PI) {
                heightSinCounter = 0;
                hurtbox.setPosition(entity.getHitbox().x, entity.getHitbox().y, 0, 0);
                hurtbox.setDamageInformation(new DamageInformation(RandomUtils.getFloat(0, MathUtils.PI2), entity.KNOCKBACK, entity.DAMAGE));
                hurtbox.checkIfHitEntities(true);
                new ParticleEffect(ParticleTypes.getDiveDustParameters(), entity.getHitbox().x, entity.getBounds().y - 3);
                SoundEffects.playLoud(END_SOUND_EFFECT);
                entity.startSquish(0.01f, 1.2f);
                jumpDone = true;
            }
            entity.height = JUMP_HEIGHT * MathUtils.sin(heightSinCounter);
        }
    }
}
