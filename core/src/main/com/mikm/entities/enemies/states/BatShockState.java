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

public class BatShockState extends State {
    private final float TOTAL_SHOCK_TIME = 0.1f*6;
    public BatShockState(Entity entity) {
        super(entity);
    }

    @Override
    public void enter() {
        super.enter();
    }
    @Override
    public void update() {
        super.update();
        //if (isPlayerInDetectionCircle() && distanceTraveledSinceLastProjectile > 20) {
        //    new ParticleEffect(ParticleTypes.getLightningParameters(), entity.x, entity.y);
        //    distanceTraveledSinceLastProjectile = 0;
        //}
    }

    @Override
    protected AnimationName getAnimationName() {
        return AnimationName.BAT_SHOCK;
    }

    @Override
    public void checkForStateTransition() {
        if (timeElapsedInState > TOTAL_SHOCK_TIME) {
            //batflystate
            State standingState = entity.standingState;
            entity.xVel = 0;
            entity.yVel = 0;
            standingState.enter();
        }
        handlePlayerCollision(Bat.SHOCK_DAMAGE, true);
    }
}
