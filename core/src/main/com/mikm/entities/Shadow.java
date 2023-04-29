package com.mikm.entities;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.GameScreen;

public class Shadow extends InanimateEntity{
    private InanimateEntity entity;
    private float shadowScale = .75f;
    private final float SHADOW_DISAPPEAR_HEIGHT_FOR_NORMAL_ENTITY = 20;

    private Shadow() {
        super(0, 0);
    }

    public Shadow(InanimateEntity entity) {
        super(0, 0);
        this.entity = entity;
    }


    @Override
    public Rectangle getBounds() {
        throw new RuntimeException("tried to access shadow bounds");
    }

    @Override
    public boolean hasShadow() {
        return false;
    }

    @Override
    public void update() {

    }

    @Override
    public int getZOrder() {
        return -2;
    }

    @Override
    public void draw(Batch batch) {
        final float shadowHeightScale = Math.min(shadowScale / (entity.getShadowBounds().width/ Application.TILE_WIDTH) * entity.height/SHADOW_DISAPPEAR_HEIGHT_FOR_NORMAL_ENTITY, shadowScale);
        batch.draw(GameScreen.shadowImage, entity.getShadowBounds().x, entity.getShadowBounds().y - 3,entity.getShadowBounds().width/2f, 4, entity.getShadowBounds().width, entity.getShadowBounds().height,
                entity.xScale * (shadowScale - shadowHeightScale), entity.yScale * (shadowScale - shadowHeightScale), 0);
    }
}
