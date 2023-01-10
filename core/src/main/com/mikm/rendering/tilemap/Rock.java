package com.mikm.rendering.tilemap;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.Vector2Int;
import com.mikm.entities.InanimateEntity;

public class Rock extends InanimateEntity {
    private final TextureRegion image;
    public Rock(TextureRegion image, int x, int y) {
        super(x, y);
        this.image = image;
        hasShadow = false;
    }

    @Override
    public void update() {

    }

    @Override
    public void draw(Batch batch) {
        batch.draw(image, x, y);
    }

    @Override
    public void onWallCollision(Vector2Int wallPosition) {

    }
}
