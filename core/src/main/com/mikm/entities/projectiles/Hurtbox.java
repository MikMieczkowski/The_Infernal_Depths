package com.mikm.entities.projectiles;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.mikm.entities.Destructible;
import com.mikm.entities.Entity;
import com.mikm.entities.InanimateEntity;
import com.mikm.entities.particles.ParticleEffect;
import com.mikm.entities.particles.ParticleTypes;
import com.mikm.rendering.SoundEffects;
import com.mikm.rendering.cave.Rock;
import com.mikm.rendering.cave.RockType;
import com.mikm.rendering.screens.Application;

public class Hurtbox {
    private boolean active;
    public float x, y, radius;
    private DamageInformation damageInformation;


    public Hurtbox(float diameter, boolean active) {
        this.radius = diameter/2f;
    }

    public void setPosition(float x, float y, float distanceFromOriginOfRotation, float rotationAngle) {
        this.x = x + distanceFromOriginOfRotation * MathUtils.cos(rotationAngle);
        this.y = y + distanceFromOriginOfRotation * MathUtils.sin(rotationAngle);
    }

    public void setPosition(float x, float y) {
        this.x=x;
        this.y=y;
    }

    public void setDamageInformation(DamageInformation damageInformation) {
        this.damageInformation = damageInformation;
    }

    public void checkIfHitEntities() {
        for (Entity entity : Application.getInstance().currentScreen.entities) {
            if (entity != Application.player && Intersector.overlaps(getHurtbox(), entity.getHitbox())) {
                entity.damagedState.enter(damageInformation);
            }
        }
        if (Application.getInstance().currentScreen == Application.getInstance().townScreen) {
            for (InanimateEntity inanimateEntity : Application.getInstance().townScreen.inanimateEntities) {
                if (inanimateEntity.getClass() == Destructible.class&& Intersector.overlaps(inanimateEntity.getHitbox(), getHurtbox())) {
                    inanimateEntity.die();
                    Destructible d = (Destructible)inanimateEntity;

                    Application.getInstance().townScreen.isCollidableGrid()[(int)inanimateEntity.y/ Application.TILE_HEIGHT][(int)inanimateEntity.x / Application.TILE_WIDTH] = false;
                    SoundEffects.play(d.sound);
                    new ParticleEffect(d.particleEffect, inanimateEntity.x, inanimateEntity.y);
                }
            }
        }
    }

    public void checkIfHitPlayer() {
        boolean hitboxesOverlap = Intersector.overlaps(getHurtbox(), Application.player.getHitbox());
        if (hitboxesOverlap) {
            float angleToPlayer = MathUtils.atan2(Application.player.getCenteredPosition().y - y, Application.player.getCenteredPosition().x - x);
            Application.player.damagedState.enter(new DamageInformation(angleToPlayer, damageInformation.knockbackForceMagnitude, damageInformation.damage));
        }
    }

    public Circle getHurtbox() {
        return new Circle(x, y, radius);
    }
}
