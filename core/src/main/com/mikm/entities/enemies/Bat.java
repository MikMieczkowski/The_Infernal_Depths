package com.mikm.entities.enemies;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.mikm.RandomUtils;
import com.mikm.debug.DebugRenderer;
import com.mikm.entities.Entity;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.animation.DirectionalAnimation;
import com.mikm.entities.enemies.states.*;
import com.mikm.rendering.SoundEffects;
import com.mikm.rendering.screens.Application;

import java.util.HashMap;
import java.util.Map;

public class Bat extends Entity {
    public static final float SPEED = 4;
    public static final float ANGULAR_SPEED = 1.57f;
    private final float DETECTION_CIRCLE_RADIUS = 200f;
    private final int CONTACT_DAMAGE = 1;
    public final float TIME_BETWEEN_DASHES = .2f;
    private final float DASH_BUILDUP_TIME = .1f;
    private final float DASH_SPEED = 4f;
    public DashBuildUpState dashBuildUpState;
    private static Map<AnimationName, DirectionalAnimation> animations = new HashMap<>();

    public Bat() {
        super(0,0);
    }
    public Bat(float x, float y) {
        super(x, y);
        this.x-=xVel;
        this.y-=yVel;
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public int getMaxHp() {
        return 3;
    }

    @Override
    protected void createStates() {
        standingState = new BatFlyState(this, CONTACT_DAMAGE, DETECTION_CIRCLE_RADIUS, TIME_BETWEEN_DASHES);
        walkingState = new BatFlyState(this, CONTACT_DAMAGE, DETECTION_CIRCLE_RADIUS, TIME_BETWEEN_DASHES);
        dashingState = new DashingState(this, DASH_SPEED);
        detectedPlayerState = dashingState;
        dashBuildUpState = new BatDashBuildupState(this, DASH_BUILDUP_TIME);
        detectedPlayerBuildUpState = dashBuildUpState;
        standingState.enter();
    }

    @Override
    protected void createAnimations() {
        DirectionalAnimation walk = new DirectionalAnimation("bat", Application.TILE_WIDTH, Application.TILE_HEIGHT, .1f, 2, Animation.PlayMode.LOOP);
        DirectionalAnimation stand = walk.createDirectionalAnimationFromFirstFrames();
        animations.put(AnimationName.WALK, walk);
        animations.put(AnimationName.HIT, stand);
        animations.put(AnimationName.STAND, stand);
    }

    @Override
    protected Map<?, ?> getAnimations() {
        return animations;
    }

    @Override
    public Sound getHitSound() {
        return SoundEffects.batHit;
    }
}
