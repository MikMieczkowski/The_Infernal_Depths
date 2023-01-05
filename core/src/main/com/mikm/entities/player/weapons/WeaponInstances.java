package com.mikm.entities.player.weapons;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.entities.player.Player;
import com.mikm.rendering.screens.Application;

public class WeaponInstances {
    public Sword sword;

    public WeaponInstances(TextureAtlas atlas, Player player) {
        TextureRegion swordSprite = atlas.findRegion("Weapons").split(Application.TILE_WIDTH, Application.TILE_HEIGHT)[0][0];
        TextureRegion[] sliceSpritesheet = atlas.findRegion("swordSlice").split(32, 32)[0];
        sword = new Sword(swordSprite, sliceSpritesheet, player);
    }
}
