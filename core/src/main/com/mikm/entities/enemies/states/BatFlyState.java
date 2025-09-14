package com.mikm.entities.enemies.states;

import com.badlogic.gdx.math.MathUtils;
import com.mikm.RandomUtils;
import com.mikm.entities.Entity;
import com.mikm.entities.State;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.enemies.Bat;
import com.mikm.entities.particles.ParticleEffect;
import com.mikm.entities.particles.ParticleTypes;
import com.mikm.rendering.screens.Application;

public class BatFlyState extends DashInducingState {
    private final float TOTAL_FLY_TIME = 6f;
    private float angle;
    private int distanceTraveledSinceLastProjectile = 0;
    private Bat bat;

    public BatFlyState(Bat bat, int contactDamage, float detectionCircleRadius, float timeBetweenDashes) {
        super(bat, contactDamage, detectionCircleRadius, timeBetweenDashes);
        this.bat = bat;
    }

    @Override
    public void enter() {
        super.enter();
        angle = RandomUtils.getFloat(0, MathUtils.PI2);
    }
    @Override
    public void update() {
        super.update();
        angle += Bat.ANGULAR_SPEED;
        entity.height = 3+MathUtils.sin(timeElapsedInState*3)*3;
        entity.xVel = Bat.SPEED * MathUtils.cos(angle);
        entity.yVel = Bat.SPEED * MathUtils.sin(angle);
        distanceTraveledSinceLastProjectile += (int) Bat.SPEED;
        if (isPlayerInDetectionCircle() && distanceTraveledSinceLastProjectile > 10) {
            new ParticleEffect(ParticleTypes.getLightningParameters(), entity.x, entity.y);
            distanceTraveledSinceLastProjectile = 0;
        }
    }

    @Override
    protected AnimationName getAnimationName() {
        return AnimationName.WALK;
    }

    @Override
    public void checkForStateTransition() {
        super.checkForStateTransition();
        if (timeElapsedInState > TOTAL_FLY_TIME) {
            entity.xVel = 0;
            entity.yVel = 0;
            //bat.shockState.enter();
        }
    }
}
