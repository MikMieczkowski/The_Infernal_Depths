package com.mikm.entities.actions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.mikm.Assets;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.Entity;
import com.mikm.rendering.sound.SoundEffects;
import com.mikm.rendering.screens.Application;

public class ChargeAtPlayerAction extends Action {
    private int SPEED;
    private int JUMP_HEIGHT;
    private String START_SOUND_EFFECT;
    private String END_SOUND_EFFECT;

    public ChargeAtPlayerAction(Entity entity) {
        super(entity);
    }


    private float angleToPlayer;
    private float bounceTimer;
    private float startHeight = 0;

    @Override
    public void enter() {
        super.enter();
        startHeight = entity.height;
        bounceTimer = 0;
        entity.xVel = 0;
        entity.yVel = 0;
        angleToPlayer = MathUtils.atan2(Application.player.getHitbox().y - entity.y, Application.player.getHitbox().x - entity.x);
        SoundEffects.play(START_SOUND_EFFECT);

    }

    @Override
    public void update() {
        super.update();
        entity.height = startHeight + ExtraMathUtils.bounceLerp(bounceTimer, MAX_TIME, JUMP_HEIGHT, 1, 10.6f);
        bounceTimer += Gdx.graphics.getDeltaTime();
        entity.xVel = MathUtils.cos(angleToPlayer) * SPEED * (timeElapsedInState / MAX_TIME);
        entity.yVel = MathUtils.sin(angleToPlayer) * SPEED * (timeElapsedInState / MAX_TIME);
    }



    @Override
    public void onExit() {
        SoundEffects.playLoud(END_SOUND_EFFECT);
        entity.height = startHeight;
        entity.xVel = 0;
        entity.yVel = 0;
    }
}
