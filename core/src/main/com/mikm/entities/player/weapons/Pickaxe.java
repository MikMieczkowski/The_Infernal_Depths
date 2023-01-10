package com.mikm.entities.player.weapons;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.mikm.entities.DamageInformation;
import com.mikm.entities.player.Player;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.CaveScreen;
import com.mikm.rendering.tilemap.Rock;

public class Pickaxe extends SwingableWeapon {
    CaveScreen caveScreen;
    public Pickaxe(CaveScreen caveScreen, TextureRegion image, TextureRegion[] sliceSpritesheet, Player player) {
        super(image, sliceSpritesheet, player);
        this.caveScreen = caveScreen;
    }

    @Override
    public void checkForHit() {
        for (Rock rock : caveScreen.rocks) {
            if (Intersector.overlaps(rock.getHitbox(), staticHurtbox.getHurtbox())) {
                caveScreen.rocks.remove(rock);
                caveScreen.getCollidableTilePositions()[(int)rock.y/ Application.TILE_HEIGHT][(int)rock.x / Application.TILE_WIDTH] = false;
            }
        }
    }

    @Override
    public DamageInformation getDamageInformation() {
        return new DamageInformation(0, 0, 0);
    }
}
