package com.mikm.entities.player.weapons;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.entities.player.Player;
import com.mikm.rendering.screens.Application;

public class WeaponInstances {
    public Sword sword;

    public WeaponInstances(TextureAtlas atlas, Player player) {
        TextureRegion swordSprite = atlas.findRegion("Weapons").split(Application.defaultTileWidth, Application.defaultTileHeight)[0][0];
        sword = new Sword(swordSprite, player);
    }
}
