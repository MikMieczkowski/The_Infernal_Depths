package com.mikm.entities.player.weapons;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.Assets;
import com.mikm.rendering.screens.CaveScreen;

public class WeaponInstances {
    public Pickaxe pickaxe;
    public Sword copperSword;
    public Sword ironSword;
    public Sword crystalSword;
    public Sword infernalSword;
    public Sword[] swords;
    public Bow copperBow;
    public Bow ironBow;
    public Bow crystalBow;
    public Bow infernalBow;
    public Bow[] bows;

    public WeaponInstances(CaveScreen caveScreen) {
        TextureRegion[][] items = Assets.getInstance().getSplitTextureRegion("items");
        TextureRegion arrowImage = Assets.getInstance().getTextureRegion("arrow");
        TextureRegion[] strings = Assets.getInstance().getSplitTextureRegion("strings")[0];
        TextureRegion[] sliceSpritesheet = Assets.getInstance().getSplitTextureRegion("swordSlice", 32, 32)[0];
        copperSword = new Sword(items[0][0], sliceSpritesheet, 1, 3, .4f, 32);
        ironSword = new Sword(items[0][1], sliceSpritesheet, 1, 3, .3f, 32);
        crystalSword = new Sword(items[0][2], sliceSpritesheet, 2, 5, .3f, 48);
        infernalSword = new Sword(items[0][3], sliceSpritesheet, 3, 7, .1f, 64);
        pickaxe = new Pickaxe(caveScreen, items[1][0], sliceSpritesheet);
        copperBow = new Bow(items[0][4], strings, arrowImage, 1, 1, .2f, .2f, 2);
        ironBow = new Bow(items[0][5], strings, arrowImage, 3, 3, .4f, .4f, 2);
        crystalBow = new Bow(items[0][6], strings, arrowImage, 3, 3, .1f, .3f, 4);
        infernalBow = new Bow(items[0][7], strings, arrowImage, 3, 10, .01f, .01f, 10);
        swords = new Sword[]{copperSword, ironSword, crystalSword, infernalSword};
        bows = new Bow[]{copperBow, ironBow, crystalBow, infernalBow};
    }
}
