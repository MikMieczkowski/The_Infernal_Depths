package com.mikm.entities.player.weapons;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.entities.Hurtbox;
import com.mikm.entities.player.Player;

public abstract class Weapon {
    TextureRegion image;
    Hurtbox hurtbox;
    Player player;

    public Weapon(TextureRegion image, Player player) {
        this.image = image;
        this.player = player;
    }
    public abstract void update();

    public abstract void draw(Batch batch);
}
