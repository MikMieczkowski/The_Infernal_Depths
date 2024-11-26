package com.mikm.entities.enemies;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.mikm.entities.Entity;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.animation.DirectionalAnimation;
import com.mikm.entities.enemies.states.DashBuildUpState;
import com.mikm.entities.enemies.states.DashingState;
import com.mikm.entities.enemies.states.DashInducingStandingState;
import com.mikm.entities.enemies.states.WanderingState;
import com.mikm.rendering.SoundEffects;
import com.mikm.rendering.screens.Application;

import java.util.HashMap;
import java.util.Map;

public class Slime extends Entity {
    public final float SPEED = 1;
    private final float DETECTION_CIRCLE_RADIUS = 100f;
    private final int CONTACT_DAMAGE = 1;
    public final float TIME_BETWEEN_DASHES = 2f;
    private final float DASH_SPEED = 6f;
    private float angle;

    private static Map<AnimationName, DirectionalAnimation> animations = new HashMap<>();

    public DashBuildUpState dashBuildUpState;

    private Slime() {
        //serialization constructor
        super(0, 0);
    }

    public Slime(int x, int y) {
        super(x, y);
    }

    @Override
    public void createStates() {
        standingState = new DashInducingStandingState(this, CONTACT_DAMAGE, DETECTION_CIRCLE_RADIUS, TIME_BETWEEN_DASHES);
        walkingState = new WanderingState(this, CONTACT_DAMAGE, DETECTION_CIRCLE_RADIUS, TIME_BETWEEN_DASHES);
        dashingState= new DashingState(this, DASH_SPEED);
        detectedPlayerState = dashingState;
        dashBuildUpState = new DashBuildUpState(this, 1);
        super.detectedPlayerBuildUpState = dashBuildUpState;
        standingState.enter();
    }

    @Override
    protected void createAnimations() {
        DirectionalAnimation walk = new DirectionalAnimation("Slime_Walk", Application.TILE_WIDTH, Application.TILE_HEIGHT, .33f, Animation.PlayMode.LOOP);
        DirectionalAnimation stand = walk.createDirectionalAnimationFromFirstFrames();
        animations.put(AnimationName.WALK, walk);
        animations.put(AnimationName.HIT, stand);
        animations.put(AnimationName.STAND, stand);
    }

    @Override
    protected Map<?,?> getAnimations() {
        return animations;
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

    @Override
    public Sound getHitSound() {
        return SoundEffects.slimeHit;
    }
}
