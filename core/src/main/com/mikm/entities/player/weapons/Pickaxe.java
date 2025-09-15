package com.mikm.entities.player.weapons;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.mikm.entities.inanimateEntities.InanimateEntity;
import com.mikm.entities.particles.ParticleTypes;
import com.mikm.entities.particles.ParticleEffect;
import com.mikm.entities.projectiles.DamageInformation;
import com.mikm.rendering.SoundEffects;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.CaveScreen;
import com.mikm.rendering.cave.Rock;
import com.mikm.rendering.cave.RockType;

public class Pickaxe extends SwingableWeapon {
    private final static float TIME_PER_SWING = .4f;
    CaveScreen caveScreen;
    public Pickaxe(CaveScreen caveScreen, TextureRegion image, TextureRegion[] sliceSpritesheet) {
        super(image, sliceSpritesheet, TIME_PER_SWING, 32);
        this.caveScreen = caveScreen;
    }

    @Override
    public void checkForHit() {
        super.checkForHit();
        if (Application.getInstance().currentScreen != Application.getInstance().caveScreen) {
            return;
        }
        for (InanimateEntity inanimateEntity : caveScreen.inanimateEntities) {
            if (inanimateEntity.getClass() == Rock.class && Intersector.overlaps(inanimateEntity.getHitbox(), hurtbox.getHurtbox())) {
                inanimateEntity.die();

                //
                boolean[][] rockGrid = caveScreen.caveTilemapCreator.rockCollidablePositions;
                boolean[][] grid = caveScreen.isCollidableGrid();
                grid[(int)inanimateEntity.y/ Application.TILE_HEIGHT][(int)inanimateEntity.x / Application.TILE_WIDTH] = false;
                rockGrid[(int)inanimateEntity.y/ Application.TILE_HEIGHT][(int)inanimateEntity.x / Application.TILE_WIDTH] = false;
                RockType rockType = ((Rock)inanimateEntity).rockType;
                SoundEffects.play(SoundEffects.rockBreak);
                if (rockType != RockType.NORMAL) {
                    rockType.increaseOreAmount();
                    SoundEffects.playQuiet(SoundEffects.reward);
                }
                new ParticleEffect(ParticleTypes.getRockParameters(rockType), inanimateEntity.x, inanimateEntity.y);
            }
        }
    }

    @Override
    public DamageInformation getDamageInformation() {
        return new DamageInformation(angleToMouse, 1, 0);
    }
}
