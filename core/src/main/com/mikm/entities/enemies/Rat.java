package com.mikm.entities.enemies;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.mikm.Vector2Int;
import com.mikm.entities.UnanimatedEntity;
import com.mikm.rendering.screens.Application;

public class Rat extends UnanimatedEntity {
    public Rat(int x, int y) {
        super(x, y);
    }

    @Override
    public void update() {

    }

    @Override
    public void render(Batch batch) {
        batch.draw(Application.testTexture, x, y);
    }

    @Override
    public void onWallCollision(Vector2Int wallPosition) {

    }
}
