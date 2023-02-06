package com.mikm.entities.collision;


import com.badlogic.gdx.math.Rectangle;

public class Collision {
    public float tNear;
    public Rectangle wall;

    public Collision(float tNear, Rectangle wall) {
        this.tNear = tNear;
        this.wall = wall;
    }

    public float getTNear() {
        return tNear;
    }
}
