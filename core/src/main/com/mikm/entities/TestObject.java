package com.mikm.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.mikm.rendering.screens.Application;

public class TestObject extends InanimateEntity {
    private final int width;
    private final Color color;
    public TestObject(int x, int y) {
        super(x,y);
        this.width = Application.TILE_WIDTH;
        this.color = new Color(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, .2f);
    }

    public TestObject(int x, int y, int width, Color color) {
        super(x,y);
        this.width = width;
        this.color = new Color(color.r, color.g, color.b, .5f);
    }

    @Override
    public void update() {

    }

    @Override
    public boolean hasShadow() {
        return false;
    }

    @Override
    public void draw(Batch batch) {
        batch.setColor(color);
        batch.draw(Application.testTexture, x+16-width, y+16-width, width, width);
        batch.setColor(Color.WHITE);
    }
}
