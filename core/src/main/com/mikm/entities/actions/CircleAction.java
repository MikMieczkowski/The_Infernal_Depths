package com.mikm.entities.actions;

import com.badlogic.gdx.math.MathUtils;
import com.mikm.entities.Entity;
import com.mikm.entities.inanimateEntities.particles.ParticleEffect;
import com.mikm.entities.inanimateEntities.particles.ParticleTypes;

public class CircleBehaviour extends Behaviour {
    private float ANGULAR_SPEED;
    private float angle;
    private int distanceTraveledSinceLastProjectile = 0;

    public CircleBehaviour(Entity entity) {
        super(entity);
    }

    @Override
    public void update() {
        super.update();
        angle += ANGULAR_SPEED;
        entity.height = 3 + MathUtils.sin(timeElapsedInState * 3) * 3;
        entity.xVel = entity.SPEED * MathUtils.cos(angle);
        entity.yVel = entity.SPEED * MathUtils.sin(angle);
        distanceTraveledSinceLastProjectile += (int) entity.SPEED;
        if (distanceTraveledSinceLastProjectile > 10) {
            new ParticleEffect(ParticleTypes.getLightningParameters(), entity.x, entity.y);
            distanceTraveledSinceLastProjectile = 0;
        }
    }
}
