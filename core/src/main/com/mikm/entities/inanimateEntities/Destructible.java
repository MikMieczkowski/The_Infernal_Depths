package com.mikm.entities.inanimateEntities;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.Assets;
import com.mikm.entities.inanimateEntities.particles.ParticleTypes;

public class Destructible extends InanimateEntity {
    private final TextureRegion image;
    public ParticleTypes particleEffect;
    public String soundName;
    private TextureRegion destructibleShadow = Assets.getInstance().getTextureRegion("destructibleShadow", 48,48);

    public Destructible(TextureRegion image, ParticleTypes particleEffect, String soundName, float x, float y) {
        super(x, y);
        this.image = image;
        this.soundName = soundName;
        this.particleEffect = particleEffect;
    }

    @Override
    public void update() {

    }

    @Override
    public boolean hasShadow() {
        return false;
    }

    @Override
    public void draw(Batch batch) {
        batch.draw(image, x, y);
        batch.draw(destructibleShadow, x-4, y-4, 24, 24);
    }
}
