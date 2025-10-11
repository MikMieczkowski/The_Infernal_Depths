package com.mikm.entities.inanimateEntities.particles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.inanimateEntities.InanimateEntity;
import com.mikm.rendering.screens.Application;

public class Particle extends InanimateEntity {
    public ParticleTypes parameters;
    private float size, angle, startingSpeed;
    private float timer;
    private boolean checkedOnce = false;
    private Color color;
    private Color startColor, endColor;

    Particle(ParticleTypes parameters, Color startColor, Color endColor, float size, float angle, float startingSpeed) {
        super(0, 0);
        this.parameters = parameters;
        this.size = size;
        this.angle = angle;
        this.startingSpeed = startingSpeed;
        color = startColor;
        this.startColor = startColor;
        this.endColor = endColor;

        xScale = size;
        yScale = size;
    }

    void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean hasShadow() {
        return parameters.hasShadow;
    }

    @Override
    public void update() {

        timer += Gdx.graphics.getDeltaTime();

        float speed = startingSpeed;
        if (parameters.shouldDecelerate) {
            final float timeSpentDecelerating = parameters.proportionOfTimeSpentDecelerating * parameters.maxLifeTime;
            speed = startingSpeed * Math.max(0, 1 - (timer / timeSpentDecelerating));
        }
        xVel = speed * MathUtils.cos(angle);
        yVel = speed * MathUtils.sin(angle);

        if (parameters.collidesWithWalls) {
            collider.updateCollisions();
        }
        move();

        if (parameters.hasGravity) {
            height = ExtraMathUtils.bounceLerp(timer, parameters.maxLifeTime, parameters.peakHeight,.1f, 8);
        }
        final float percentOfTimeComplete = Math.max(0, 1- timer/ parameters.maxLifeTime);
        if (xScale > parameters.finalScale) {
            xScale = size * percentOfTimeComplete;
            yScale = size * percentOfTimeComplete;
        }

        if (xScale <= 0) {
            die();
        }

        if (parameters.usesColor) {
            color = ExtraMathUtils.lerpColor(timer, parameters.maxLifeTime, startColor, endColor);
        }
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x + 4 + Math.max(1,size/2f), y+ 4 + Math.max(1,size/2f), size, size);
    }

    @Override
    public Rectangle getShadowBounds() {
        return new Rectangle(x, y +3, 8, 8);
    }

    @Override
    public void draw(Batch batch) {
        if (!checkedOnce) {
            if (parameters.collidesWithWalls && collider.inWall()) {
                die();
                return;
            }
            checkedOnce = true;
        }
        if (parameters.usesColor) {
            batch.setColor(color);
        }
        batch.draw(parameters.image, x, y+height, 4, 4, 8, 8, xScale, yScale, 0);
        if (parameters.usesColor) {
            batch.setColor(Color.WHITE);
        }
    }

    @Override
    public void die() {
        if (Application.getInstance().currentScreen == Application.getInstance().townScreen) {
            Application.getInstance().townScreen.removeSmokeParticle(this);
        } else {
            super.die();
        }
    }
}
