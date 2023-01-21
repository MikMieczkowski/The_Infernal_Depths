package com.mikm.entities.player.weapons;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.entities.projectiles.DamageInformation;

public class Sword extends SwingableWeapon {
    private final float KNOCKBACK_FORCE = 3f;
    private final int DAMAGE = 1;

    public Sword(TextureRegion image, TextureRegion[] sliceSpritesheet) {
        super(image, sliceSpritesheet);
        hurtbox.setDamageInformation(getDamageInformation());
    }


    @Override
    public DamageInformation getDamageInformation() {
        return new DamageInformation(angleToMouse, KNOCKBACK_FORCE, DAMAGE);
    }

    @Override
    public void checkForHit() {
        hurtbox.setDamageInformation(getDamageInformation());
        hurtbox.checkIfHitEntities();
    }
}
