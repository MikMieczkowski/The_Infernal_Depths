package com.mikm.entities.projectiles;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.mikm.entities.Entity;
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
        for (Entity entity : Application.currentScreen.entities) {
            if (entity != Application.player && entity.isAttackable && Intersector.overlaps(getHurtbox(), entity.getHitbox())) {
                entity.damagedState.enter(damageInformation);
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
