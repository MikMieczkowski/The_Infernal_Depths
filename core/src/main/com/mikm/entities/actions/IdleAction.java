package com.mikm.entities.actions;

import com.mikm.RandomUtils;
import com.mikm.entities.Entity;
import com.mikm.rendering.sound.SoundEffects;

public class IdleAction extends Action {
    private float TIME_MIN = 1;
    private float TIME_MAX = 1;
    private String START_SOUND_EFFECT;

    public IdleAction(Entity entity) {
        super(entity);
    }

    @Override
    public void enter() {
        super.enter();
        SoundEffects.play(START_SOUND_EFFECT);
        entity.xVel = 0;
        entity.yVel = 0;
        MAX_TIME = RandomUtils.getFloat(TIME_MIN, TIME_MAX);
    }

    //does nothing
}
