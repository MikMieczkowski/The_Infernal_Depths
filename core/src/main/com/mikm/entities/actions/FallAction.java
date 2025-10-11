package com.mikm.entities.actions;

import com.mikm.ExtraMathUtils;
import com.mikm.entities.Entity;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.player.Player;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.sound.SoundEffects;

public class FallAction extends Action {
    private String START_SOUND_EFFECT;

    public FallAction(Entity entity) {
        super(entity);
    }

    @Override
    public void enter() {
        super.enter();
        entity.xVel = 0;
        entity.yVel = 0;
        entity.height = 0;
        entity.isAttackable = false;
        SoundEffects.play(START_SOUND_EFFECT);
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void onExit() {
        Application.getInstance().currentScreen.entities.doAfterRender(() -> {
            Application.getInstance().setGameScreen(Application.getInstance().caveScreen);
            Application.getInstance().caveScreen.increaseFloor();
            entity.isAttackable = true;
        });
        super.onExit();
    }
}
