package com.mikm.entities.projectiles;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.entities.InanimateEntity;
import com.mikm.entities.particles.ParticleParameters;
import com.mikm.entities.particles.ParticleSystem;

public class StaticProjectile extends InanimateEntity {
    private TextureRegion image;
    private Hurtbox hurtbox;
    private boolean visible;

    public StaticProjectile(TextureRegion image, boolean visible, float x, float y) {
        super(x, y);
        this.image = image;
        this.visible = visible;
        hurtbox = new Hurtbox(10, true);
        hurtbox.setPosition(x, y);
        new ParticleSystem(ParticleParameters.getSlimeTrailParameters(), x, y);
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
        if (visible) {
            batch.draw(image, x, y);
        }
        drawHitboxes(batch, hurtbox.getHurtbox());
    }
}
