package com.mikm.rendering.screens;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.mikm.entities.Entity;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.animation.DirectionalAnimation;
import com.mikm.entities.enemies.states.IdleState;

import java.util.HashMap;
import java.util.Map;

public class BlacksmithRoom extends Entity {
    private static Map<AnimationName, DirectionalAnimation> animations = new HashMap<>();
    public BlacksmithRoom(float x, float y) {
        super(x, y);
    }

    @Override
    public int getMaxHp() {
        return 0;
    }

    @Override
    protected void createStates() {
        standingState = new IdleState(this);
        walkingState = standingState;
        detectedPlayerState = standingState;
        detectedPlayerBuildUpState = standingState;
        standingState.enter();
    }

    @Override
    public void update() {

        currentState.update();
    }


    @Override
    protected void createAnimations() {
        DirectionalAnimation idle = new DirectionalAnimation("blacksmithRoom", 144, 144, .1f, 8, Animation.PlayMode.LOOP);
        animations.put(AnimationName.STAND, idle);
    }

    @Override
    protected Map<?, ?> getAnimations() {
        return animations;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, 144, 144);
    }

    @Override
    public Circle getHitbox() {
        return new Circle(0,0,0);
    }
    @Override
    public int getZOrder() {
        return -2;
    }

    @Override
    public boolean hasShadow() {
        return false;
    }
}
