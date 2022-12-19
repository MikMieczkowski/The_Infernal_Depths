package com.mikm.entities;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public abstract class Entity extends Actor {
    protected float x, y;

    @Override
    public void draw(Batch batch, float alpha) {
        tick();
        render(batch);
    }

    public abstract void tick();

    public abstract void render(Batch batch);
}
