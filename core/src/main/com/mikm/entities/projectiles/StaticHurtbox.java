package com.mikm.entities.projectiles;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;

public class StaticHurtbox {
    private boolean active;
    private float x, y, radius;


    public StaticHurtbox(float diameter, boolean active) {
        this.radius = diameter/2f;
    }

    public void setPosition(float x, float y, float distanceFromOriginOfRotation, float rotationAngle) {
        this.x = x + distanceFromOriginOfRotation * MathUtils.cos(rotationAngle);
        this.y = y + distanceFromOriginOfRotation * MathUtils.sin(rotationAngle);
    }

    public Circle getHurtbox() {
        return new Circle(x, y, radius);
    }
}
