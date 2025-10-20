package com.mikm.entities.inanimateEntities;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Intersector;
import com.mikm.rendering.screens.Application;

public class Door extends InanimateEntity {
    private int screenNumber;

    public Door(float x, float y, int screenNumber) {
        super(x, y);
        this.screenNumber = screenNumber;
    }

    @Override
    public void update() {
        if (Intersector.overlaps(Application.player.getHitbox(), getHitbox())) {
            Application.getInstance().setGameScreen(Application.getInstance().screens[screenNumber]);
        }
    }

    @Override
    public void draw() {

    }

    @Override
    public boolean hasShadow() {
        return false;
    }
}
