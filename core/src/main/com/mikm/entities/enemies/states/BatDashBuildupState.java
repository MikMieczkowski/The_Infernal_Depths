package com.mikm.entities.enemies.states;

import com.mikm.entities.Entity;

public class BatDashBuildupState extends DashBuildUpState {
    private Entity entity;
    public BatDashBuildupState(Entity entity, float maxBuildupTime) {
        super(entity, maxBuildupTime);
        this.entity = entity;
    }

    @Override
    public void update() {
        super.update();

    }

    @Override
    public void enter() {
        if (entity.currentState == this) {
            return;
        }
        timeElapsedInState = 0;
        entity.xVel = 0;
        entity.yVel = 0;
        entity.setDirectionalAnimation(getAnimationName());
        entity.animationManager.resetTimer();
        entity.animationManager.update();
        entity.currentState = this;
    }
}
