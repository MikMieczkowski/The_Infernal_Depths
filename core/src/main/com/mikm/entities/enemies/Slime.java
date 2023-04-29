package com.mikm.entities.enemies;

import com.mikm.entities.Entity;
import com.mikm.entities.animation.EntityActionSpritesheets;
import com.mikm.entities.enemies.states.DashBuildUpState;
import com.mikm.entities.enemies.states.DashingState;
import com.mikm.entities.enemies.states.StandingState;
import com.mikm.entities.enemies.states.WanderingState;

public class Slime extends Entity {
    public final float SPEED = 1;
    private final float angle;
    private final boolean slimeBossMinion;
    public static EntityActionSpritesheets entityActionSpritesheets;
    public DashingState dashingState;
    public DashBuildUpState dashBuildUpState;

    private Slime() {
        super(0, 0, new EntityActionSpritesheets());
        angle = 0;
        slimeBossMinion = false;
    }

    public Slime(int x, int y, EntityActionSpritesheets entityActionSpritesheets) {
        super(x, y, entityActionSpritesheets);
        Slime.entityActionSpritesheets = entityActionSpritesheets;
        slimeBossMinion = false;
        angle = 0;
        createStates();
    }

    public Slime(float x, float y, float angle) {
        super(x, y, entityActionSpritesheets);
        this.angle = angle;
        slimeBossMinion = true;
        hp = 1;
        createStates();
    }

    @Override
    public void createStates() {
        standingState = new StandingState(this, 1);
        walkingState = new WanderingState(this, 1);
        dashingState= new DashingState(this);
        detectedPlayerState = dashingState;
        dashBuildUpState = new DashBuildUpState(this);
        super.detectedPlayerBuildUpState = dashBuildUpState;
        if(!slimeBossMinion) {
            standingState.enter();
        } else {
            dashBuildUpState.enter(angle);
        }
    }

    @Override
    public float getOriginX() {
        return getBounds().width/2f;
    }

    @Override
    public float getSpeed() {
        return 1;
    }

    @Override
    public int getMaxHp() {
        return 3;
    }
}
