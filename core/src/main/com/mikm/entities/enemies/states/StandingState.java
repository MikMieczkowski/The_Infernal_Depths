package com.mikm.entities.enemies.states;

import com.mikm.RandomUtils;
import com.mikm.entities.Entity;
import com.mikm.entities.animation.AnimationName;

public class StandingState extends DashInducingState {
    private float timeBetweenWanders;
    private final float TIME_BETWEEN_WANDERS_MIN = 1f, TIME_BETWEEN_WANDERS_MAX = 4f;

    public StandingState(Entity entity, int contactDamage) {
        super(entity, contactDamage, 0, 0);
    }

    @Override
    public void enter() {
        super.enter();
        standingStateEnter();
    }

    @Override
    public void enter(float dashTimer) {
        super.enter(dashTimer);
        standingStateEnter();
    }

    private void standingStateEnter() {
        timeBetweenWanders = RandomUtils.getFloat(TIME_BETWEEN_WANDERS_MIN, TIME_BETWEEN_WANDERS_MAX);
        entity.xVel = 0;
        entity.yVel = 0;
    }

    @Override
    public void update() {
        super.update();
    }


    @Override
    public void checkForStateTransition() {
        super.checkForStateTransition();
        if (timeElapsedInState > timeBetweenWanders) {
            WanderingState wanderingState = (WanderingState) entity.walkingState;
            wanderingState.enter(timeSinceLastDash);
        }
    }

    @Override
    protected AnimationName getAnimationName() {
        return AnimationName.STAND;
    }
}
