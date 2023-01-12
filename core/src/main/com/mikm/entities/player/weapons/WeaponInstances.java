package com.mikm.entities.player.weapons;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.CaveScreen;

public class WeaponInstances {
    public Sword sword;
    public Pickaxe pickaxe;
    public Bow bow;

    public WeaponInstances(CaveScreen caveScreen, TextureAtlas atlas) {
        TextureRegion swordImage = atlas.findRegion("sword").split(Application.TILE_WIDTH, Application.TILE_HEIGHT)[0][0];
        TextureRegion[] bowImages = atlas.findRegion("bow").split(Application.TILE_WIDTH, Application.TILE_HEIGHT)[0];
        TextureRegion arrowImage = atlas.findRegion("arrow").split(Application.TILE_WIDTH, Application.TILE_HEIGHT)[0][0];
        TextureRegion pickaxeImage = atlas.findRegion("pickaxes").split(Application.TILE_WIDTH, Application.TILE_HEIGHT)[0][0];
        TextureRegion[] sliceSpritesheet = atlas.findRegion("swordSlice").split(32, 32)[0];
        sword = new Sword(swordImage, sliceSpritesheet);
        pickaxe = new Pickaxe(caveScreen, pickaxeImage, sliceSpritesheet);
        bow = new Bow(bowImages, arrowImage);
    }
}
