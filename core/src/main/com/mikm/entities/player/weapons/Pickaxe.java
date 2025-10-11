package com.mikm.entities.player.weapons;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.mikm.entities.DamageInformation;
import com.mikm.entities.inanimateEntities.InanimateEntity;
import com.mikm.entities.inanimateEntities.particles.ParticleTypes;
import com.mikm.entities.inanimateEntities.particles.ParticleEffect;
import com.mikm.rendering.sound.SoundEffects;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.CaveScreen;
import com.mikm.rendering.cave.Rock;
import com.mikm.rendering.cave.RockType;

public class Pickaxe extends SwingableWeapon {
    private final static float TIME_PER_SWING = .4f;
    CaveScreen caveScreen;
    private String ROCK_BREAK_SOUND_EFFECT = "rockBreak.ogg";
    private String REWARD_SOUND_EFFECT = "reward.ogg";


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
                SoundEffects.play(ROCK_BREAK_SOUND_EFFECT);
                if (rockType != RockType.NORMAL) {
                    rockType.increaseOreAmount();
                    SoundEffects.playQuiet(REWARD_SOUND_EFFECT);
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
