package com.mikm.entities;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.Vector2Int;

public class Hurtbox extends UnanimatedEntity {
    private TextureRegion image;
    private boolean breaksByWalls;
    public Hurtbox(TextureRegion image, boolean breaksByWalls) {
        this.image = image;
        this.breaksByWalls = breaksByWalls;
    }

    @Override
    public void update() {

    }

    @Override
    public void render(Batch batch) {
        batch.draw(image, x, y);
    }

    @Override
    public void onWallCollision(Vector2Int wallPosition) {
        if (breaksByWalls) {

        }
    }
}
