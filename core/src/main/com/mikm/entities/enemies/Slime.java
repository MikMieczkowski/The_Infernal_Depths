package com.mikm.entities.enemies;

import com.mikm.entities.Entity;
import com.mikm.entities.animation.EntityActionSpritesheets;
import com.mikm.entities.enemies.states.DashingState;
import com.mikm.entities.enemies.states.StandingState;
import com.mikm.entities.enemies.states.WanderingState;

public class Slime extends Entity {

    public Slime(int x, int y, EntityActionSpritesheets entityActionSpritesheets) {
        super(x, y, entityActionSpritesheets);
        speed = 1f;
    }

    @Override
    public void createStates() {
        standingState = new StandingState(this, 1);
        walkingState = new WanderingState(this, 1);
        detectedPlayerState = new DashingState(this);
        standingState.enter();
    }

    @Override
    public int getMaxHp() {
        return 3;
    }
}
