package com.mikm.rendering.cave;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.utils.RandomUtils;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.CaveScreen;

//TODO rock
//public class Rock extends InanimateEntity {
//    private final TextureRegion image;
//    public final RockType rockType;
//    //used for serialization
//    public int recolorLevel;
//    public Rock(int x, int y, RockType rockType, int recolorLevel) {
//        super(x, y);
//        this.rockType = rockType;
//        if (rockType == RockType.NORMAL) {
//            this.image = CaveScreen.rockImages[recolorLevel][RandomUtils.getInt(2)];
//        } else {
//            this.image = CaveScreen.oreImages[rockType.spritesheetPosition];
//        }
//        this.recolorLevel = recolorLevel;
//    }
//
//    @Override
//    public void update() {
//
//    }
//
//    @Override
//    public boolean hasShadow() {
//        return false;
//    }
//
//    @Override
//    public void draw() {
//        Application.batch.draw(image, x, y);
//    }
//}
