package com.mikm.entities.inanimateEntities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.mikm.entities.Entity;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.animation.DirectionalAnimation;
import com.mikm.entities.enemies.states.IdleState;
import com.mikm.rendering.screens.Application;

import java.util.HashMap;
import java.util.Map;

public class HealingEffect extends Entity {
    private static Map<AnimationName, DirectionalAnimation> animations = new HashMap<>();
    public HealingEffect(float x, float y) {
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
        if (animationManager.isFinished()) {
            die();
        }
    }


    @Override
    protected void createAnimations() {
        DirectionalAnimation idle = new DirectionalAnimation("healingEffect", 48, 160, .1f, 30, Animation.PlayMode.NORMAL);

        animations.put(AnimationName.STAND, idle);
    }

    @Override
    protected Map<?, ?> getAnimations() {
        return animations;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, 48, 160);
    }

    @Override
    public Circle getHitbox() {
        return new Circle(0,0,0);
    }
    @Override
    public int getZOrder() {
        return 2;
    }

    @Override
    public boolean hasShadow() {
        return false;
    }
}
