package com.mikm.entities.actions;

import com.badlogic.gdx.math.MathUtils;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.DamageInformation;
import com.mikm.entities.Entity;
import com.mikm.entities.inanimateEntities.particles.ParticleEffect;
import com.mikm.entities.inanimateEntities.particles.ParticleTypes;
import com.mikm.entities.inanimateEntities.projectiles.StaticProjectile;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.sound.SoundEffects;

public class SimmerTowardsPlayerAction extends Action {
    private float SPEED_MIN;
    private float SPEED_MAX;
    private Float SLIME_TRAIL_KNOCKBACK;
    private Float SLIME_TRAIL_DAMAGE;
    private String SLIME_TRAIL_SOUND_EFFECT;

    private float distanceTraveledSinceLastProjectile;
    private float angle;

    public SimmerTowardsPlayerAction(Entity entity) {
        super(entity);
    }

    @Override
    public void postConfigRead() {
        super.postConfigRead();
        if (SLIME_TRAIL_KNOCKBACK == null) {
            SLIME_TRAIL_KNOCKBACK = 1F;
        }
        if (SLIME_TRAIL_DAMAGE == null) {
            SLIME_TRAIL_DAMAGE = 1F;
        }
    }

    @Override
    public void enter() {
        super.enter();
        angle = MathUtils.atan2(Application.player.y - entity.y, Application.player.x - entity.x);
    }

    @Override
    public void update() {
        super.update();

        float moveSpeed = ExtraMathUtils.lerp(timeElapsedInState, MAX_TIME, .3f, 1, SPEED_MIN, SPEED_MAX);
        moveTowardsPlayer(moveSpeed);
        handleSlimeTrail(moveSpeed);
    }

    private void moveTowardsPlayer(float moveSpeed) {
        float angleToPlayer = MathUtils.atan2(Application.player.y - entity.y, Application.player.x - entity.x);
        angle = ExtraMathUtils.lerpAngle(.5f, MAX_TIME, angle, angleToPlayer);
        entity.xVel = MathUtils.cos(angle) * moveSpeed;
        entity.yVel =  MathUtils.sin(angle) * moveSpeed;
    }

    private void handleSlimeTrail(float moveSpeed) {
        distanceTraveledSinceLastProjectile += moveSpeed;
        if (distanceTraveledSinceLastProjectile > 20) {
            SoundEffects.play(SLIME_TRAIL_SOUND_EFFECT);
            distanceTraveledSinceLastProjectile -= 20;
            Application.getInstance().currentScreen.addInanimateEntity(new StaticProjectile(null, false, new DamageInformation(0, SLIME_TRAIL_KNOCKBACK, entity.DAMAGE), entity.getHitbox().x, entity.getHitbox().y));
            new ParticleEffect(ParticleTypes.getSlimeTrailParameters(), entity.getHitbox().x, entity.getHitbox().y);
        }
    }
}
