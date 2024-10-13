package com.mikm.entities.enemies.states;

import com.badlogic.gdx.math.Vector2;
import com.mikm.RandomUtils;
import com.mikm.entities.Entity;
import com.mikm.entities.animation.AnimationName;

public class WanderingState extends DashInducingState {
    private Vector2 wanderForce;
    private final float TOTAL_WANDER_TIME = 1f;
    private final float MIN_WANDER_FORCE = .2f;

    public WanderingState(Entity entity, int contactDamage, float detectionCircleRadius, float timeBetweenDashes) {
        super(entity, contactDamage, detectionCircleRadius, timeBetweenDashes);
    }


    @Override
    public void enter() {
        super.enter();
        wanderForce = new Vector2(getRandomWanderFloat(), getRandomWanderFloat());
    }

    @Override
    public void enter(float dashTimer) {
        super.enter(dashTimer);
        wanderForce = new Vector2(getRandomWanderFloat(), getRandomWanderFloat());
    }

    @Override
    public void update() {
        super.update();
        entity.xVel = wanderForce.x;
        entity.yVel = wanderForce.y;
    }

    @Override
    public void checkForStateTransition() {
        super.checkForStateTransition();
        if (timeElapsedInState > TOTAL_WANDER_TIME) {
            DashInducingStandingState dashInducingStandingState = (DashInducingStandingState) entity.standingState;
            dashInducingStandingState.enter(timeSinceLastDash);
        }
    }

    private float getRandomWanderFloat() {
        float randomForcePositive = entity.getSpeed() * RandomUtils.getFloat(MIN_WANDER_FORCE, 1);
        int randomSign = RandomUtils.getBoolean() ? 1 : -1;
        return randomSign * randomForcePositive;
    }

    @Override
    protected AnimationName getAnimationName() {
        return AnimationName.WALK;
    }
}
