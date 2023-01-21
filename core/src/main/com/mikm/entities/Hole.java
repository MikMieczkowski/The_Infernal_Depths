package com.mikm.entities;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.rendering.screens.CaveScreen;

public class Hole extends InanimateEntity {
    private TextureRegion[] images;
    public Hole(float x, float y) {
        super(x, y);
        images = CaveScreen.holeImages;
    }

    @Override
    public void update() {

    }

    @Override
    public void draw(Batch batch) {
        //batch.draw(images[CaveScreen.], )
    }
}
