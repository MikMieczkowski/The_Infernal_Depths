package com.mikm.entities.enemies.states;

import com.badlogic.gdx.Gdx;
import com.mikm.entities.Entity;
import com.mikm.entities.State;

public class DashBuildUpState extends State {
    private final float MAX_BUILDUP_TIME = 1;
    private float buildupTime;

    public DashBuildUpState(Entity entity) {
        super(entity);
        animationManager = entity.standingState.animationManager;
    }

    @Override
    public void enter() {
        super.enter();
        buildupTime = 0;
        entity.startSquish(0, 1.5f, MAX_BUILDUP_TIME, false);
    }

    @Override
    public void update() {
        super.update();
        buildupTime += Gdx.graphics.getDeltaTime();
    }

    @Override
    public void checkForStateTransition() {
        if (buildupTime > MAX_BUILDUP_TIME) {
            buildupTime = 0;
            entity.detectedPlayerState.enter();
        }
    }
}
