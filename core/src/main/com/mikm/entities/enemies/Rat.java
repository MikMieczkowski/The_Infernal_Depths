package com.mikm.entities.enemies;

import com.mikm.entities.Entity;
import com.mikm.entities.animation.EntityActionSpritesheets;
import com.mikm.entities.enemies.states.StandingState;
import com.mikm.entities.enemies.states.WanderingState;

public class Rat extends Entity {

    public Rat(int x, int y, EntityActionSpritesheets entityActionSpritesheets) {
        super(x, y, entityActionSpritesheets);
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public void createStates() {
        walkingState = new WanderingState(this);
        standingState = new StandingState(this);
        standingState.enter();
    }

    @Override
    public int getMaxHp() {
        return 0;
    }
}
