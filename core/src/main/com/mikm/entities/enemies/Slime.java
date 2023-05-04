package com.mikm.entities.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.mikm.entities.Entity;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.animation.DirectionalAnimation;
import com.mikm.entities.enemies.states.DashBuildUpState;
import com.mikm.entities.enemies.states.DashingState;
import com.mikm.entities.enemies.states.StandingState;
import com.mikm.entities.enemies.states.WanderingState;
import com.mikm.rendering.screens.Application;

import java.util.HashMap;
import java.util.Map;

public class Slime extends Entity {
    public final float SPEED = 1;
    private final float angle;
    private final boolean slimeBossMinion;

    private static Map<AnimationName, DirectionalAnimation> animations = new HashMap<>();

    public DashingState dashingState;
    public DashBuildUpState dashBuildUpState;

    private Slime() {
        //serialization constructor
        super(0, 0);
        angle = 0;
        slimeBossMinion = false;
    }

    public Slime(int x, int y) {
        super(x, y);
        slimeBossMinion = false;
        angle = 0;
        createAnimations();
        createStates();
    }

    public Slime(float x, float y, float angle) {
        super(x, y);
        this.angle = angle;
        slimeBossMinion = true;
        hp = 1;
        createStates();
        createAnimations();
    }

    @Override
    public void createStates() {
        standingState = new StandingState(this, 1);
        walkingState = new WanderingState(this, 1);
        dashingState= new DashingState(this);
        detectedPlayerState = dashingState;
        dashBuildUpState = new DashBuildUpState(this);
        super.detectedPlayerBuildUpState = dashBuildUpState;
        if(!slimeBossMinion) {
            standingState.enter();
        } else {
            dashBuildUpState.enter(angle);
        }
    }

    @Override
    protected void createAnimations() {
        DirectionalAnimation walk = new DirectionalAnimation("Slime_Walk", Application.TILE_WIDTH, Application.TILE_HEIGHT, .33f, Animation.PlayMode.LOOP);
        DirectionalAnimation stand = walk.createDirectionalAnimationFromFirstFrames();
        animations.put(AnimationName.ENTITY_WALK, walk);
        animations.put(AnimationName.HIT, stand);
        animations.put(AnimationName.ENTITY_STAND, stand);
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
}
