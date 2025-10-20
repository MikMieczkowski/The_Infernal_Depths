package com.mikm.entities.inanimateEntities;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.mikm.Assets;
import com.mikm.entities.inanimateEntities.particles.ParticleEffect;
import com.mikm.entities.inanimateEntities.particles.ParticleTypes;
import com.mikm.rendering.sound.SoundEffects;
import com.mikm.rendering.cave.RockType;
import com.mikm.rendering.screens.Application;

public class Grave extends InanimateEntity {
    private static final TextureRegion IMAGE = Assets.getInstance().getTextureRegion("grave");
    public int[] ores = new int[RockType.SIZE];

    private final float TALKING_RANGE_DIAMETER = 72;
    private String REWARD_SOUND_EFFECT = "reward.ogg";

    public Grave(float x, float y, boolean loadedFromFile) {
        super(x, y);
        if (!loadedFromFile) throw new RuntimeException("Use other constructor");
    }
    public Grave(float x, float y) {
        super(x, y);
        new ParticleEffect(ParticleTypes.getRockParameters(RockType.NORMAL), x, y);
        new ParticleEffect(ParticleTypes.getRockParameters(RockType.NORMAL), x, y);
        new ParticleEffect(ParticleTypes.getRockParameters(RockType.NORMAL), x, y);
        for (int i = 0; i < RockType.SIZE; i++) {
            ores[i] = RockType.get(i).tempOreAmount;
            RockType.get(i).increaseOreAmount(-RockType.get(i).tempOreAmount);
            RockType.get(i).tempOreAmount = 0;
        }
        if (Application.getInstance().currentScreen == Application.getInstance().slimeBossRoomScreen) {
            Application.getInstance().slimeBossRoomScreen.graves.add(this);
        }
        if (Application.getInstance().currentScreen == Application.getInstance().motiScreen) {
            Application.getInstance().motiScreen.graves.add(this);
        }
    }

    public boolean isPlayerInTalkingRange() {
        return Intersector.overlaps(Application.player.getHitbox(), new Circle(x, y, TALKING_RANGE_DIAMETER/2));
    }

    @Override
    public void update() {
        if (!Application.player.dead && isPlayerInTalkingRange()) {
            for (int i = 0; i < RockType.SIZE; i++) {
                RockType.get(i).increaseOreAmount(ores[i]);
                new ParticleEffect(ParticleTypes.getLightningParameters(), x, y);
                SoundEffects.playQuiet(REWARD_SOUND_EFFECT);
                if (Application.getInstance().currentScreen == Application.getInstance().slimeBossRoomScreen) {
                    Application.getInstance().slimeBossRoomScreen.graves.remove(this);
                }
                if (Application.getInstance().currentScreen == Application.getInstance().motiScreen) {
                    Application.getInstance().motiScreen.graves.remove(this);
                }
                die();
                Application.getInstance().currentScreen.inanimateEntities.doAfterRender(()-> {
                    Application.getInstance().caveScreen.updateCurrentMemento();
                });
            }
        }
    }

    @Override
    public boolean hasShadow() {
        return false;
    }

    @Override
    public void draw() {
        Application.batch.draw(IMAGE, x, y);
    }
}
