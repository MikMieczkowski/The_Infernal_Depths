package com.mikm.entities.enemies.states;

import com.mikm.entities.Entity;
import com.mikm.entities.animation.AnimationName;

public class IdleState extends State {
    public IdleState(Entity entity) {
        super(entity);
    }

    @Override
    protected AnimationName getAnimationName() {
        return AnimationName.STAND;
    }

    @Override
    public void checkForStateTransition() {

    }
}
