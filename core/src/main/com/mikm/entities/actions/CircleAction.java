package com.mikm.entities.actions;

import com.badlogic.gdx.math.MathUtils;
import com.mikm.DeltaTime;
import com.mikm.entities.Entity;
import com.mikm.entities.inanimateEntities.particles.ParticleEffect;
import com.mikm.entities.inanimateEntities.particles.ParticleTypes;
import com.mikm.rendering.screens.Application;

public class CircleAction extends Action {
    private float ANGULAR_SPEED;
    private float SPEED = 0;
    private float angle;
    private float distanceTraveledSinceLastProjectile = 0;

    public CircleAction(Entity entity) {
        super(entity);
    }

    @Override
    public void enter() {
        super.enter();
        // Ensure a usable speed if not configured explicitly
        if (SPEED == 0) {
            SPEED = entity.SPEED;
        }
        // Seed starting angle relative to player so initial frame is not visually idle
        float angleToPlayer = MathUtils.atan2(
                Application.player.getHitbox().y - entity.getHitbox().y,
                Application.player.getHitbox().x - entity.getHitbox().x);
        // Start perpendicular for a natural circular path around the player
        angle = angleToPlayer + MathUtils.PI / 2f;
        entity.xVel = SPEED * MathUtils.cos(angle);
        entity.yVel = SPEED * MathUtils.sin(angle);
    }

    @Override
    public void postConfigRead() {
        if (SPEED == 0) {
            SPEED = entity.SPEED;
        }
    }

    @Override
    public void update() {
        super.update();
        angle += ANGULAR_SPEED * DeltaTime.deltaTime();
        entity.height = 3 + MathUtils.sin(timeElapsedInState * 3) * 3;
        entity.xVel = SPEED * MathUtils.cos(angle);
        entity.yVel = SPEED * MathUtils.sin(angle);
        distanceTraveledSinceLastProjectile += SPEED;
        if (distanceTraveledSinceLastProjectile > 10) {
            new ParticleEffect(ParticleTypes.getLightningParameters(), entity.x, entity.y);
            distanceTraveledSinceLastProjectile = 0;
        }
    }
}
