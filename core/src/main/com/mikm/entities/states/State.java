package com.mikm.entities.states;

import com.mikm.entities.Entity;
import com.mikm.entities.animation.AnimationManager;

public abstract class State {
    public AnimationManager animationManager;
    private final Entity entity;

    public State(Entity entity) {
        this.entity = entity;
    }

    public void enter() {
        animationManager.resetTimer();
        animationManager.setCurrentAnimation();
        entity.currentState = this;
    }

    public void update() {
        animationManager.setCurrentAnimation();
    }

    public abstract void checkForStateTransition();
}
