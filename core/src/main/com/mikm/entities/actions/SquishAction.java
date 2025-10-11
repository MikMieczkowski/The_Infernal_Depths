package com.mikm.entities.actions;

import com.mikm.entities.Entity;

public class SquishAction extends Action {
    private float AMOUNT;

    public SquishAction(Entity entity) {
        super(entity);
    }

    @Override
    public void enter() {
        super.enter();
        entity.xVel = 0;
        entity.yVel = 0;
        entity.startSquish(0, AMOUNT, MAX_TIME, true);
    }

    @Override
    public void update() {
        super.update();
        entity.xVel = 0;
        entity.yVel = 0;
    }
}
