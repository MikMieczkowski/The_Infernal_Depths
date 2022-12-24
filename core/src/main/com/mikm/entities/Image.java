package com.mikm.entities;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public abstract class Image extends Actor {
    public float x, y;

    @Override
    public void draw(Batch batch, float alpha) {
        update();
        render(batch);
    }

    public abstract void update();

    public abstract void render(Batch batch);

    public int getXInt() {
        return (int)x;
    }

    public int getYInt() {
        return (int)y;
    }
}
