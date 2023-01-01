package com.mikm.entities.player.weapons;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.entities.player.Player;

public class Sword extends Weapon {

    public Sword(TextureRegion image, Player player) {
        super(image, player);
    }

    @Override
    public void update() {

    }

    @Override
    public void draw(Batch batch) {
        batch.draw(image, player.x, player.y);
    }
}
