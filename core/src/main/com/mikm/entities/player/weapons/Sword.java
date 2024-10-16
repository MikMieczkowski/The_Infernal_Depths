package com.mikm.entities.player.weapons;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.DeltaTime;
import com.mikm.debug.DebugRenderer;
import com.mikm.entities.projectiles.DamageInformation;

public class Sword extends SwingableWeapon {
    private float knockbackForce;
    private int damage;
    private int sliceWidth;

    public Sword(TextureRegion image, TextureRegion[] sliceSpritesheet, int damage, int knockbackForce, float timePerSwing, int sliceWidth) {
        super(image, sliceSpritesheet, timePerSwing, sliceWidth);
        this.damage = damage;
        this.knockbackForce = knockbackForce;
        hurtbox.setDamageInformation(getDamageInformation());
    }


    @Override
    public DamageInformation getDamageInformation() {
        return new DamageInformation(angleToMouse, knockbackForce, damage);
    }

}
