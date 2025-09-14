package com.mikm.entities.enemies;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.mikm.RandomUtils;
import com.mikm.debug.DebugRenderer;
import com.mikm.entities.Entity;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.animation.DirectionalAnimation;
import com.mikm.entities.enemies.states.*;
import com.mikm.rendering.SoundEffects;
import com.mikm.rendering.screens.Application;
import com.mikm.entities.enemies.states.BatShockState;

import java.util.HashMap;
import java.util.Map;

public class Bat extends Entity {
    public static final float SPEED = 1f;
    public static float ANGULAR_SPEED = .1f;
    private final float DETECTION_CIRCLE_RADIUS = 200f;
    private final int CONTACT_DAMAGE = 1;
    public final float TIME_BETWEEN_DASHES = 1f;
    private final float DASH_BUILDUP_TIME = .1f;
    private final float DASH_SPEED = 2f;
    public static final int SHOCK_DAMAGE = 1;
    public DashBuildUpState dashBuildUpState;
    public BatShockState shockState;
    private static Map<AnimationName, DirectionalAnimation> animations = new HashMap<>();

    public Bat() {
        super(0,0);
        collider.isBat = true;
    }
    public Bat(float x, float y) {
        super(x, y);
        this.x-=xVel;
        this.y-=yVel;
        collider.isBat = true;
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
        dashingState = new BatDashState(this, DASH_SPEED);
        shockState = new BatShockState(this);
        detectedPlayerState = dashingState;
        dashBuildUpState = new BatDashBuildupState(this, DASH_BUILDUP_TIME);
        detectedPlayerBuildUpState = dashBuildUpState;
        standingState.enter();
    }

    @Override
    protected void createAnimations() {
        DirectionalAnimation walk = new DirectionalAnimation("batFly", 32, 32, .1f, 4, Animation.PlayMode.LOOP);
        DirectionalAnimation stand = walk.createDirectionalAnimationFromFirstFrames();
        DirectionalAnimation shock = new DirectionalAnimation("batShock", 32, 32, .1f, 6, Animation.PlayMode.NORMAL);
        animations.put(AnimationName.WALK, walk);
        animations.put(AnimationName.HIT, stand);
        animations.put(AnimationName.STAND, stand);
        animations.put(AnimationName.BAT_SHOCK, shock);
    }

    @Override
    protected Map<?, ?> getAnimations() {
        return animations;
    }

    @Override
    public Sound getHitSound() {
        return SoundEffects.batHit;
    }
    @Override
    public Rectangle getShadowBounds() {
        return new Rectangle(x + 8, y + 6, Application.TILE_WIDTH, Application.TILE_HEIGHT);
    }
    @Override
    public Rectangle getFullBounds() {
        return new Rectangle(x, y, 32, 32);
    }

    @Override
    public Circle getHitbox() {
        if (currentState == shockState && currentState.timeElapsedInState > .1f*3)
            return new Circle(x+16,y+16,20);
        else 
            return new Circle(x+16, y+16, 8);
    }
}
