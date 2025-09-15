package com.mikm.entities.inanimateEntities;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.Assets;
import com.mikm.entities.particles.ParticleTypes;

public class Destructible extends InanimateEntity {
    private final TextureRegion image;
    public ParticleTypes particleEffect;
    public Sound sound;
    private TextureRegion destructibleShadow = Assets.getInstance().getTextureRegion("destructibleShadow", 48,48);

    public Destructible(TextureRegion image, ParticleTypes particleEffect, Sound sound, float x, float y) {
        super(x, y);
        this.image = image;
        this.sound = sound;
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
