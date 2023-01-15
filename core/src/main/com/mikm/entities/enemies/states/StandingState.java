package com.mikm.entities.enemies.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.Entity;
import com.mikm.entities.animation.ActionAnimationAllDirections;
import com.mikm.entities.animation.AnimationManager;

public class StandingState extends DashInducingState {
    private float wanderTimer;
    private float timeBetweenWanders;
    private final float TIME_BETWEEN_WANDERS_MIN = 1f, TIME_BETWEEN_WANDERS_MAX = 4f;

    public StandingState(Entity entity, float contactDamage) {
        super(entity, contactDamage);
        ActionAnimationAllDirections actionAnimationAllDirections = new ActionAnimationAllDirections(.33f, Animation.PlayMode.LOOP, entity.entityActionSpritesheets.standing);
        animationManager = new AnimationManager(entity, actionAnimationAllDirections);
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
        wanderTimer = 0;
        timeBetweenWanders = ExtraMathUtils.randomFloat(TIME_BETWEEN_WANDERS_MIN, TIME_BETWEEN_WANDERS_MAX);
        entity.xVel = 0;
        entity.yVel = 0;
    }

    @Override
    public void update() {
        super.update();
        wanderTimer += Gdx.graphics.getDeltaTime();
    }


    @Override
    public void checkForStateTransition() {
        super.checkForStateTransition();
        if (wanderTimer > timeBetweenWanders) {
            WanderingState wanderingState = (WanderingState) entity.walkingState;
            wanderingState.enter(timeSinceLastDash);
        }
    }
}
