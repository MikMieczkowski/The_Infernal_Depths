package com.mikm.entities.inanimateEntities;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

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
    public void draw(Batch batch) {
        batch.draw(img, x, y);
    }

    @Override
    public int getZOrder() {
        return -1;
    }

    @Override
    public boolean hasShadow() {
        return false;
    }
}
