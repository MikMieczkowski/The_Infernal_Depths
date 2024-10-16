package com.mikm.entities;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.mikm.Assets;
import com.mikm.entities.particles.ParticleEffect;
import com.mikm.entities.particles.ParticleTypes;
import com.mikm.input.GameInput;
import com.mikm.rendering.SoundEffects;
import com.mikm.rendering.cave.RockType;
import com.mikm.rendering.screens.Application;

public class Grave extends InanimateEntity {
    private static final TextureRegion IMAGE = Assets.getInstance().getTextureRegion("grave");
    public int[] ores = new int[RockType.SIZE];

    private final float TALKING_RANGE_DIAMETER = 72;
    public Grave(float x, float y, boolean loadedFromFile) {
        super(x, y);
        if (!loadedFromFile) throw new RuntimeException("Use other constructor");
    }
    public Grave(float x, float y) {
        super(x, y);
        if (Application.getInstance().currentScreen == Application.getInstance().caveScreen) {
            Application.getInstance().caveScreen.graves.add(this);
        }
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
                SoundEffects.playQuiet(SoundEffects.reward);
                if (Application.getInstance().currentScreen == Application.getInstance().slimeBossRoomScreen) {
                    Application.getInstance().slimeBossRoomScreen.graves.remove(this);
                }
                if (Application.getInstance().currentScreen == Application.getInstance().caveScreen) {
                    Application.getInstance().caveScreen.graves.remove(this);
                }
                Application.getInstance().caveScreen.updateCurrentMemento();
                die();
            }
        }
    }

    @Override
    public boolean hasShadow() {
        return false;
    }

    @Override
    public void draw(Batch batch) {
        batch.draw(IMAGE, x, y);
    }
}
