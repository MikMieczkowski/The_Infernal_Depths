package com.mikm.entities.enemies;

import com.mikm.entities.Entity;
import com.mikm.entities.animation.EntityActionSpritesheets;
import com.mikm.entities.enemies.states.StandingState;
import com.mikm.entities.enemies.states.WanderingState;

public class Slime extends Entity {

    public Slime(int x, int y, EntityActionSpritesheets entityActionSpritesheets) {
        super(x, y, entityActionSpritesheets);
        speed = 1f;
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    @Override
    public void createStates() {
        standingState = new StandingState(this);
        walkingState = new WanderingState(this);
        standingState.enter();
    }

    @Override
    public int getMaxHp() {
        return 3;
    }
}
