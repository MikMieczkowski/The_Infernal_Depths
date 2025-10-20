package com.mikm.rendering.screens;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.mikm.entities.Entity;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.animation.DirectionalAnimation;
import com.mikm.entities.animation.SingleAnimation;
import com.mikm.entities.inanimateEntities.InanimateEntity;

import java.util.HashMap;
import java.util.Map;

public class BlacksmithRoom extends InanimateEntity {
    public BlacksmithRoom(float x, float y) {
        super(x, y);
        // Pass FPS, not frame duration. 10f FPS -> 0.1s per frame
        animationHandler.changeAnimation(new SingleAnimation("blacksmithRoom", 144, 144, 7f, Animation.PlayMode.LOOP, 8));
    }

    @Override
    public void draw() {
        animationHandler.draw();
    }

    @Override
    public void update() {
        animationHandler.update();
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x,y, 144, 144);
    }

    @Override
    public boolean hasShadow() {
        return false;
    }
}
