package com.mikm.entities.enemies.moti;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.mikm.Assets;
import com.mikm.debug.DebugRenderer;
import com.mikm.entities.particles.ParticleTypes;
import com.mikm.entities.projectiles.DamageInformation;
import com.mikm.entities.projectiles.Projectile;
import com.mikm.entities.projectiles.StaticProjectile;
import com.mikm.rendering.screens.Application;

public class Moti_WebProjectile extends Projectile {
    private float distanceTraveledSinceLastProjectile;
    private final int WEB_DAMAGE = 1, WEB_KNOCKBACK = 1;
    public Moti_WebProjectile(TextureRegion image, ParticleTypes deathParticleParameters, float lifeTime, float x, float y, boolean playerProjectile) {
        super(image, deathParticleParameters, lifeTime, x, y, playerProjectile);
    }

    @Override
    public void draw(Batch batch) {
        super.draw(batch);
        DebugRenderer.getInstance().drawHitboxes(getHitbox());
    }

    @Override
    public void update() {
        super.update();
        handleWebTrail(speed);
    }
    @Override
    public Rectangle getBounds() {
        return new Rectangle(x+4, y+4, 8, 8);
    }

    private void handleWebTrail(float moveSpeed) {
        distanceTraveledSinceLastProjectile += moveSpeed;
        if (distanceTraveledSinceLastProjectile > 10) {
            distanceTraveledSinceLastProjectile -= 10;
            Application.getInstance().currentScreen.addInanimateEntity(new StaticProjectile(Assets.testTexture, true, new DamageInformation(0, WEB_KNOCKBACK, WEB_DAMAGE),x, y));
        }
    }

}
