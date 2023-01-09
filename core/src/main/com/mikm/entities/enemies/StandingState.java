package com.mikm.entities.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.Entity;
import com.mikm.entities.State;
import com.mikm.entities.animation.AnimationManager;
import com.mikm.entities.animation.ActionAnimationAllDirections;

public class StandingState extends State {
    private float wanderTimer;
    private float timeBetweenWanders;
    private final float timeBetweenWandersMin = 1f, timeBetweenWandersMax = 4f;

    public StandingState(Entity entity) {
        super(entity);
        ActionAnimationAllDirections actionAnimationAllDirections = new ActionAnimationAllDirections(.33f, Animation.PlayMode.LOOP, entity.entityActionSpritesheets.standing);
        animationManager = new AnimationManager(entity, actionAnimationAllDirections);
    }

    @Override
    public void enter() {
        super.enter();
        entity.xVel = 0;
        entity.yVel = 0;
        timeBetweenWanders = ExtraMathUtils.randomFloat(timeBetweenWandersMin, timeBetweenWandersMax);
    }

    @Override
    public void update() {
        super.update();
        wanderTimer += Gdx.graphics.getDeltaTime();
    }

    @Override
    public void checkForStateTransition() {
        if (wanderTimer > timeBetweenWanders) {
            wanderTimer = 0;
            entity.walkingState.enter();
        }
    }
}
