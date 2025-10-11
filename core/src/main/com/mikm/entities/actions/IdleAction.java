package com.mikm.entities.actions;

import com.mikm.RandomUtils;
import com.mikm.entities.Entity;

public class IdleBehaviour extends Behaviour {
    private float TIME_MIN = 1;
    private float TIME_MAX = 4;

    public IdleBehaviour(Entity entity) {
        super(entity);
    }

    @Override
    public void enter() {
        super.enter();
        MAX_TIME = RandomUtils.getFloat(TIME_MIN, TIME_MAX);
    }

    //does nothing
}
