package com.mikm.entities.player.weapons;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.entities.player.Player;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.CaveScreen;

public class WeaponInstances {
    public Sword sword;
    public Pickaxe pickaxe;

    public WeaponInstances(CaveScreen caveScreen, TextureAtlas atlas, Player player) {
        TextureRegion swordImage = atlas.findRegion("Weapons").split(Application.TILE_WIDTH, Application.TILE_HEIGHT)[0][0];
        TextureRegion pickaxeImage = atlas.findRegion("pickaxes").split(Application.TILE_WIDTH, Application.TILE_HEIGHT)[0][0];
        TextureRegion[] sliceSpritesheet = atlas.findRegion("swordSlice").split(32, 32)[0];
        sword = new Sword(swordImage, sliceSpritesheet, player);
        pickaxe = new Pickaxe(caveScreen, pickaxeImage, sliceSpritesheet, player);
    }
}
