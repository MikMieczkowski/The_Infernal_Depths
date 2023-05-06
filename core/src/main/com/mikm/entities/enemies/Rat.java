package com.mikm.entities.enemies;

import com.mikm.entities.Entity;
import com.mikm.entities.enemies.states.StandingState;
import com.mikm.entities.enemies.states.WanderingState;

import java.util.Map;

public class Rat extends Entity {

    public Rat(int x, int y) {
        super(x, y);
        damagesPlayer = false;
        isAttackable = false;
    }

    @Override
    public void createStates() {
        walkingState = new WanderingState(this, 0);
        standingState = new StandingState(this, 0);
        standingState.enter();
    }

    @Override
    protected void createAnimations() {

    }

    @Override
    protected Map<?,?> getAnimations() {
        return null;
    }

    @Override
    public int getMaxHp() {
        return 0;
    }
}
