package com.mikm.entities.actions;

import com.mikm.ExtraMathUtils;
import com.mikm.entities.Entity;

public class WanderBehaviour extends Behaviour {
    private float SPEED_MIN;
    private float SPEED_MAX;

    public WanderBehaviour(Entity entity) {
        super(entity);
    }

    @Override
    public void enter() {
        super.enter();
        entity.xVel = ExtraMathUtils.getRandomWanderVel(SPEED_MIN, SPEED_MAX);
        entity.yVel = ExtraMathUtils.getRandomWanderVel(SPEED_MIN, SPEED_MAX);
    }
}
