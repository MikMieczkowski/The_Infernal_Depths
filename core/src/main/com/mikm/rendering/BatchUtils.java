package com.mikm.rendering;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class BatchUtils {
    public static void drawFlipped(Batch batch, TextureRegion textureRegion, float x, float y, float width, float height) {
        batch.draw(textureRegion, x + width, y, -width, height);
    }

    public static void drawFlipped(Batch batch, TextureRegion textureRegion, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation, boolean horizontal) {
        if (horizontal) {
            batch.draw(textureRegion, x + width, y, originX - width, originY, -width, height, scaleX, scaleY, rotation);
        } else {
            batch.draw(textureRegion, x, y + height, originX, originY - height, width, -height, scaleX, scaleY, rotation);
        }
    }
}
