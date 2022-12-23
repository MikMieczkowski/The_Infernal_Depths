package com.mikm.entities;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.mikm.rendering.screens.Application;

public abstract class Entity extends Actor {
    public float x, y;

    @Override
    public void draw(Batch batch, float alpha) {
        tick();
        render(batch);
    }

    public abstract void tick();

    public abstract void render(Batch batch);

    public int getXInt() {
        return (int)x;
    }

    public int getYInt() {
        return (int)y;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, Application.defaultTileWidth, Application.defaultTileHeight);
    }
}
