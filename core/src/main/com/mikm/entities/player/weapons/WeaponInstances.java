package com.mikm.entities.player.weapons;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.Assets;
import com.mikm.rendering.screens.CaveScreen;

public class WeaponInstances {
    public Sword sword;
    public Pickaxe pickaxe;
    public Bow bow;

    public WeaponInstances(CaveScreen caveScreen) {
        TextureRegion swordImage = Assets.getInstance().getTextureRegion("sword");
        TextureRegion[] bowImages = Assets.getInstance().getSplitTextureRegion("bow")[0];
        TextureRegion arrowImage = Assets.getInstance().getTextureRegion("arrow");
        TextureRegion pickaxeImage = Assets.getInstance().getTextureRegion("pickaxes");
        TextureRegion[] sliceSpritesheet = Assets.getInstance().getSplitTextureRegion("swordSlice", 32, 32)[0];
        sword = new Sword(swordImage, sliceSpritesheet);
        pickaxe = new Pickaxe(caveScreen, pickaxeImage, sliceSpritesheet);
        bow = new Bow(bowImages, arrowImage);
    }
}
