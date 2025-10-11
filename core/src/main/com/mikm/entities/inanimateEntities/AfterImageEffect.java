package com.mikm.entities.inanimateEntities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.entities.inanimateEntities.InanimateEntity;
import com.mikm.rendering.screens.Application;

public class AfterImageEffect extends InanimateEntity {
    private TextureRegion image;
    private float alpha = 1;
    private final float DISAPPEAR_SPEED = 6;

    public AfterImageEffect(TextureRegion image, float x, float y, float xScale, float yScale) {
        super(x, y);
        this.image = image;
        this.xScale = xScale;
        this.yScale = yScale;
    }

    @Override
    public boolean hasShadow() {
        return false;
    }

    @Override
    public void update() {
        alpha -= DISAPPEAR_SPEED * Gdx.graphics.getDeltaTime();
        if (alpha < 0) {
            alpha = 0;
            Application.getInstance().currentScreen.removeInanimateEntity(this);
        }
    }

    @Override
    public void draw(Batch batch) {
        batch.setColor(new Color(1, 1, 1, alpha));
        batch.draw(image, x, y+ height, 0,
                0, 32, 32, xScale, yScale, 0);
        batch.setColor(Color.WHITE);
    }


}
