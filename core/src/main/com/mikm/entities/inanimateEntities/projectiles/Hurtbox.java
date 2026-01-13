package com.mikm.entities.inanimateEntities.projectiles;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.mikm.entities.DamageInformation;
import com.mikm.rendering.sound.SoundEffects;
import com.mikm.rendering.screens.Application;

//TODO implement DamageSystem

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

    public void checkIfHitEntities(boolean playerProjectile) {
//        for (Entity entity : Application.getInstance().currentScreen.entities) {
//            boolean b;
//            if (playerProjectile) {
//                b = entity != Application.playerOLD;
//            } else {
//                b = entity == Application.playerOLD;
//            }
//            if (b && Intersector.overlaps(getHurtbox(), entity.getHitbox())) {
//                if (damageInformation.damage > 0) {
//                    if (entity.hp == 1) {
//                        SoundEffects.play("hitFinal.ogg");
//                    } else {
//                        SoundEffects.play("hit.ogg");
//                    }
//                } else {
//                    SoundEffects.playLoud("step.ogg");
//                }
//                entity.damagedAction.enterOLD(damageInformation);
//            }
//        }
//        if (Application.getInstance().currentScreen == Application.getInstance().townScreen || Application.getInstance().currentScreen == Application.getInstance().slimeBossRoomScreen) {
//            for (InanimateEntity inanimateEntity : Application.getInstance().currentScreen.inanimateEntities) {
//                if (inanimateEntity.getClass() == Destructible.class&& Intersector.overlaps(inanimateEntity.getHitbox(), getHurtbox())) {
//                    inanimateEntity.die();
//                    Destructible d = (Destructible)inanimateEntity;
//
//                    Application.getInstance().currentScreen.isCollidableGrid()[(int)inanimateEntity.y/ Application.TILE_HEIGHT][(int)inanimateEntity.x / Application.TILE_WIDTH] = false;
//                    SoundEffects.play(d.soundName);
//                    new ParticleEffect(d.particleEffect, inanimateEntity.x, inanimateEntity.y);
//                }
//            }
//        }
    }

    public void checkIfHitPlayer() {
//        boolean hitboxesOverlap = Intersector.overlaps(getHurtbox(), Application.playerOLD.getHitbox());
//        if (hitboxesOverlap) {
//            float angleToPlayer = MathUtils.atan2(Application.playerOLD.getHitbox().y - y, Application.playerOLD.getHitbox().x - x);
//            Application.playerOLD.damagedAction.enterOLD(damageInformation);
//        }
    }

    public Circle getHurtbox() {
        return new Circle(x, y, radius);
    }
}
