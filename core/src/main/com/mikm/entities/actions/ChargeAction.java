package com.mikm.entities.actions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.Entity;
import com.mikm.entities.inanimateEntities.AfterImageEffect;
import com.mikm.rendering.sound.SoundEffects;
import com.mikm.rendering.screens.Application;

import java.util.Objects;
import java.util.function.Supplier;

public class ChargeAction extends Action {
    private float SPEED;
    private int JUMP_HEIGHT;
    private float BOUNCE_COEFFICIENT;
    private float BOUNCE_FREQUENCY;
    private boolean MULTIPLE_BOUNCES = false;
    private boolean ACCELERATE = false;

    private boolean HAS_AFTERIMAGES = false;
    private float TIME_BETWEEN_DASH_EFFECT_IMAGES = .05f;

    private String MOVEMENT_DIRECTION_TYPE;
    private float ANGLE_OF_APPROACH_FROM_PERPENDICULAR;

    private String START_SOUND_EFFECT;
    private String END_SOUND_EFFECT;
    private Supplier<Float> getAngle;

    public ChargeAction(Entity entity) {
        super(entity);
    }


    private float timeSinceLastDashEffectImage;
    private float angleToPlayer;
    private float bounceTimer;
    private float startHeight = 0;

    @Override
    public void postConfigRead() {
        Supplier<Float> angleToPlayer;
        if (!entity.NAME.equals("player")) {
            angleToPlayer = () -> MathUtils.atan2(Application.player.getHitbox().y - entity.getHitbox().y, Application.player.getHitbox().x - entity.getHitbox().x);
        } else {
            angleToPlayer = null;
        }

        if (MOVEMENT_DIRECTION_TYPE == null || MOVEMENT_DIRECTION_TYPE.equals("Player")) {
            getAngle = () -> angleToPlayer.get();
        } else if (MOVEMENT_DIRECTION_TYPE.equals("LeftOfPlayer")) {
            getAngle = () -> angleToPlayer.get() + MathUtils.PI/2f - ANGLE_OF_APPROACH_FROM_PERPENDICULAR * MathUtils.degRad;
        } else if (MOVEMENT_DIRECTION_TYPE.equals("RightOfPlayer")) {
            getAngle = () -> angleToPlayer.get() - MathUtils.PI/2f + ANGLE_OF_APPROACH_FROM_PERPENDICULAR * MathUtils.degRad;
        } else if (MOVEMENT_DIRECTION_TYPE.equals("CurrentDir")) {
            getAngle = () -> MathUtils.atan2(entity.yVel, entity.xVel);
        } else {
            throw new RuntimeException("Undefined AcceleratedMove MOVEMENT_DIRECTION_TYPE " + MOVEMENT_DIRECTION_TYPE);
        }

        if (SPEED == 0) {
            SPEED = entity.SPEED;
        }
    }

    @Override
    public void enter() {
        super.enter();
        startHeight = entity.height;
        bounceTimer = 0;
        entity.xVel = 0;
        entity.yVel = 0;
        SoundEffects.play(START_SOUND_EFFECT);
        angleToPlayer = getAngle.get();
    }

    @Override
    public void update() {
        super.update();
        float bounce;
        if (MULTIPLE_BOUNCES) {
            bounce = ExtraMathUtils.bounceLerp(bounceTimer, MAX_TIME, JUMP_HEIGHT, BOUNCE_COEFFICIENT, BOUNCE_FREQUENCY);
        } else {
            bounce = ExtraMathUtils.sinLerp(bounceTimer, MAX_TIME, JUMP_HEIGHT);
        }
        entity.height = startHeight + bounce;
        bounceTimer += Gdx.graphics.getDeltaTime();
        float accelerateVel = 1;
        if (ACCELERATE) {
            accelerateVel = timeElapsedInState/MAX_TIME;
        }
        entity.xVel = MathUtils.cos(angleToPlayer) * SPEED * accelerateVel;
        entity.yVel = MathUtils.sin(angleToPlayer) * SPEED * accelerateVel;

        if (HAS_AFTERIMAGES && timeSinceLastDashEffectImage > TIME_BETWEEN_DASH_EFFECT_IMAGES) {
            timeSinceLastDashEffectImage -= TIME_BETWEEN_DASH_EFFECT_IMAGES;
            Application.getInstance().currentScreen.addInanimateEntity(new AfterImageEffect(entity.animationHandler.getCurrentFrame(), entity.x, entity.y + entity.height, entity.xScale, entity.yScale));
        }
    }



    @Override
    public void onExit() {
        SoundEffects.playLoud(END_SOUND_EFFECT);
        entity.height = startHeight;
        entity.xVel = 0;
        entity.yVel = 0;
        super.onExit();
    }
}
