package com.mikm.entities.player.weapons;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.mikm.entities.InanimateEntity;
import com.mikm.entities.particles.ParticleTypes;
import com.mikm.entities.particles.ParticleEffect;
import com.mikm.entities.projectiles.DamageInformation;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.CaveScreen;
import com.mikm.rendering.cave.Rock;
import com.mikm.rendering.cave.RockType;

public class Pickaxe extends SwingableWeapon {
    CaveScreen caveScreen;
    public Pickaxe(CaveScreen caveScreen, TextureRegion image, TextureRegion[] sliceSpritesheet) {
        super(image, sliceSpritesheet);
        this.caveScreen = caveScreen;
    }

    @Override
    public void checkForHit() {
        for (InanimateEntity inanimateEntity : caveScreen.inanimateEntities) {
            if (inanimateEntity.getClass() == Rock.class && Intersector.overlaps(inanimateEntity.getHitbox(), hurtbox.getHurtbox())) {
                inanimateEntity.die();
                caveScreen.isCollidableGrid()[(int)inanimateEntity.y/ Application.TILE_HEIGHT][(int)inanimateEntity.x / Application.TILE_WIDTH] = false;

                RockType rockType = ((Rock)inanimateEntity).rockType;
                if (rockType != RockType.NORMAL) {
                    rockType.increaseOreAmount(rockType);
                }
                new ParticleEffect(ParticleTypes.getRockParameters(rockType), inanimateEntity.x, inanimateEntity.y);
            }
        }
    }

    @Override
    public DamageInformation getDamageInformation() {
        return new DamageInformation(0, 0, 0);
    }
}
