package com.mikm.entities;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.mikm.Assets;
import com.mikm.input.GameInput;
import com.mikm.rendering.screens.Application;

public class Rope extends InanimateEntity {
    private static TextureRegion image = Assets.getInstance().getTextureRegion("rope");
    public Rope(float x, float y) {
        super(x, y);
    }

    @Override
    public void update() {
        if (Intersector.overlaps(Application.player.getHitbox(), getHitbox())) {
            Application.getInstance().caveScreen.displayButtonIndicator = true;
            Application.getInstance().caveScreen.buttonIndicatorPosition = new Vector2(x, y);
            if (GameInput.isTalkButtonJustPressed()) {
                Application.getInstance().caveScreen.entities.doAfterRender(()->{
                    Application.getInstance().caveScreen.decreaseFloor();
                });
            }
        } else {
            Application.getInstance().caveScreen.displayButtonIndicator = false;
        }
    }

    @Override
    public void draw(Batch batch) {
        batch.draw(image, x, y);
    }
}
