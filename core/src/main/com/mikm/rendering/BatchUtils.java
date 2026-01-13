package com.mikm.rendering;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.rendering.screens.Application;

public class BatchUtils {
    public static void drawFlipped(TextureRegion textureRegion, float x, float y, float width, float height) {
        Application.batch.draw(textureRegion, x + width, y, -width, height);
    }

    public static void draw(TextureRegion textureRegion, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation, boolean horizontal, boolean flipped) {
        if (flipped) {
            if (horizontal) {
                Application.batch.draw(textureRegion, x + width, y, originX - width, originY, -width, height, scaleX, scaleY, rotation);
            } else {
                Application.batch.draw(textureRegion, x, y + height, originX, originY - height, width, -height, scaleX, scaleY, rotation);
            }
        } else {
            Application.batch.draw(textureRegion,x,y,originX,originY,width,height,scaleX,scaleY,rotation);
        }
    }
}
