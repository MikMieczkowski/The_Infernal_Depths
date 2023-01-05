package com.mikm.entities;

import com.mikm.entities.animation.AnimationManager;

public abstract class State {
    public AnimationManager animationManager;
    public final Entity entity;

    public State(Entity entity) {
        this.entity = entity;
    }

    public void enter() {
        animationManager.resetTimer();
        animationManager.setCurrentAnimationDirectionally();
        entity.currentState = this;
    }

    public void update() {
        animationManager.setCurrentAnimationDirectionally();
    }

    public abstract void checkForStateTransition();
}
