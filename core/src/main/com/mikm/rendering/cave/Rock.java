package com.mikm.rendering.cave;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.RandomUtils;
import com.mikm.entities.InanimateEntity;
import com.mikm.rendering.screens.CaveScreen;

public class Rock extends InanimateEntity {
    private final TextureRegion image;
    public final RockType rockType;

    public Rock(int x, int y, RockType rockType) {
        super(x, y);
        this.rockType = rockType;
        if (rockType == RockType.NORMAL) {
            this.image = CaveScreen.rockImages[CaveScreen.getRecolorLevel()][RandomUtils.getInt(2)];
        } else {
            this.image = CaveScreen.oreImages[rockType.spritesheetPosition];
        }
    }

    @Override
    public void update() {

    }

    @Override
    public boolean hasShadow() {
        return false;
    }

    @Override
    public void draw(Batch batch) {
        batch.draw(image, x, y);
    }
}
