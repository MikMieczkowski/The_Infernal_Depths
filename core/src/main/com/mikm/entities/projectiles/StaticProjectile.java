package com.mikm.entities.projectiles;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.entities.InanimateEntity;
import com.mikm.entities.enemies.slimeBoss.SB_SimmerAttack;
import com.mikm.entities.particles.ParticleTypes;
import com.mikm.entities.particles.ParticleEffect;
import com.mikm.rendering.screens.Application;

public class StaticProjectile extends InanimateEntity {
    private TextureRegion image;
    private Hurtbox hurtbox;
    private boolean visible;

    public StaticProjectile(TextureRegion image, boolean visible, DamageInformation damageInformation, float x, float y) {
        super(x, y);
        this.image = image;
        this.visible = visible;
        hurtbox = new Hurtbox(10, true);
        hurtbox.setPosition(x+5, y+5);
        hurtbox.setDamageInformation(damageInformation);
    }

    @Override
    public void update() {
        if (Application.player.currentState != Application.player.divingState) {
            hurtbox.checkIfHitPlayer();
        }
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
    }
}
