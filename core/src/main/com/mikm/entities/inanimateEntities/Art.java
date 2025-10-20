package com.mikm.entities.inanimateEntities;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.rendering.screens.Application;

public class Art extends InanimateEntity {
    TextureRegion img;
    public Art(TextureRegion img, float x, float y) {
        super(x, y);
        this.img = img;
    }

    @Override
    public void update() {

    }

    @Override
    public void draw() {
        Application.batch.draw(img, x, y);
    }

    @Override
    public boolean hasShadow() {
        return false;
    }
}
