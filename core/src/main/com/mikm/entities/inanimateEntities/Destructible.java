package com.mikm.entities.inanimateEntities;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.mikm.Assets;
import com.mikm.entities.animation.SuperAnimation;
import com.mikm.entities.inanimateEntities.particles.ParticleTypes;
import com.mikm.rendering.screens.Application;

public class Destructible extends InanimateEntity {
    private boolean animated = false;
    private TextureRegion image;
    public ParticleTypes particleEffect;
    public String soundName;
    private TextureRegion destructibleShadow = Assets.getInstance().getTextureRegion("destructibleShadow", 48,48);
    public boolean hasShadow = true;
    public int width, height;


    public Destructible(TextureRegion image, ParticleTypes particleEffect, String soundName, float x, float y) {
        super(x, y);
        this.image = image;
        this.soundName = soundName;
        this.particleEffect = particleEffect;
    }

    public Destructible(SuperAnimation animation, ParticleTypes particleEffect, String soundName, float x, float y) {
        super(x, y);
        animated = true;
        animationHandler.changeAnimation(animation);
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
    public void draw() {
        if (animated) {
            animationHandler.draw();
        } else {
            Application.batch.draw(image, x, y);
        }
        if (hasShadow) {
            Application.batch.draw(destructibleShadow, x - 4, y - 4, 24, 24);
        }
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}
