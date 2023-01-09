package com.mikm.entities.enemies;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.entities.Entity;
import com.mikm.entities.animation.EntityActionSpritesheets;

import java.util.ArrayList;

public class Slime extends Entity {

    public Slime(int x, int y, EntityActionSpritesheets entityActionSpritesheets) {
        super(x, y, entityActionSpritesheets);
        speed = 1f;
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
