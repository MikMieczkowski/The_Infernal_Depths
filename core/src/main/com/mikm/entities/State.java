package com.mikm.entities;

import com.mikm.entities.animation.AnimationManager;

public abstract class State {
    public AnimationManager animationManager;
    public final Entity entity;

    public State(Entity entity) {
        this.entity = entity;
    }

    public void enter() {
        if (entity.currentState == this) {
            return;
        }
        animationManager.resetTimer();
        animationManager.setCurrentAnimation();
        entity.currentState = this;
        entity.xVel = 0;
        entity.yVel = 0;
    }

    public void update() {
        animationManager.setCurrentAnimation();
    }

    public abstract void checkForStateTransition();
}
