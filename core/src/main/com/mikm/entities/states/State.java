package com.mikm.entities.states;

import com.mikm.entities.Entity;
import com.mikm.entities.animation.AnimationSet;

public abstract class State<E extends Entity> {
    public AnimationSet animationSet;
    public E entity;

    public State(E entity) {
        this.entity = entity;
    }

    public void enter() {
        animationSet.setCurrentAnimation();
        entity.currentState = this;
    }

    public void update() {
        animationSet.setCurrentAnimation();
    }

    public abstract void handleInput();
}
