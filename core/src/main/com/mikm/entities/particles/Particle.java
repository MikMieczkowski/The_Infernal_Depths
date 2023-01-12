package com.mikm.entities.particles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.InanimateEntity;

class Particle extends InanimateEntity {
    private ParticleParameters parameters;
    private float size, angle, startingSpeed;
    private float scaleVelocity, velocity, heightAcceleration= 3f;
    private float timer;
    private boolean checkedOnce = false;

    Particle(ParticleParameters parameters, float size, float angle, float startingSpeed) {
        super(0, 0);
        this.parameters = parameters;
        this.size = size;
        this.angle = angle;
        this.startingSpeed = startingSpeed;

        shadowVerticalOffset = -3;
        xScale = size;
        yScale = size;
        hasShadow = parameters.hasShadow;
    }

    void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void update() {
        if (!checkedOnce) {
            if (parameters.collidesWithWalls && checkWallCollisions()) {
                die();
            }
            checkedOnce = true;
        }

        timer += Gdx.graphics.getDeltaTime();

        float speed = startingSpeed;
        if (parameters.shouldDecelerate) {
            final float timeSpentDecelerating = parameters.proportionOfTimeSpentDecelerating * parameters.maxLifeTime;
            speed = startingSpeed * Math.max(0, 1 - (timer / timeSpentDecelerating));
        }
        xVel = speed * MathUtils.cos(angle);
        yVel = speed * MathUtils.sin(angle);
        if (parameters.collidesWithWalls) {
            checkWallCollisions();
        }
        x += xVel;
        y += yVel;

        if (parameters.hasGravity) {
            height = ExtraMathUtils.bounceLerp(timer, parameters.maxLifeTime, parameters.peakHeight,.1f, 8);
        }
        final float percentOfTimeComplete = Math.max(0, 1- timer/ parameters.maxLifeTime);
        xScale = size * percentOfTimeComplete;
        yScale = size * percentOfTimeComplete;

        if (xScale <= 0) {
            die();
        }
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x + 4 + Math.max(1,size/2f), y+ 4 + Math.max(1,size/2f), size, size);
    }

    @Override
    public void draw(Batch batch) {
        batch.draw(parameters.image, x, y+height, 4, 4, 8, 8, xScale, yScale, 0);
    }
}
