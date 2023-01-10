package com.mikm.entities.player.weapons;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.mikm.entities.DamageInformation;
import com.mikm.entities.Entity;
import com.mikm.entities.player.Player;

public class Sword extends SwingableWeapon {
    private final float KNOCKBACK_FORCE = 3f;
    private final int DAMAGE = 1;

    public Sword(TextureRegion image, TextureRegion[] sliceSpritesheet, Player player) {
        super(image, sliceSpritesheet, player);
    }

    @Override
    public DamageInformation getDamageInformation() {
        return new DamageInformation(angleToMouse, KNOCKBACK_FORCE, DAMAGE);
    }

    @Override
    public void checkForHit() {
        for (Entity entity : player.screen.entities) {
            if (entity != player && entity.isAttackable() && Intersector.overlaps(staticHurtbox.getHurtbox(), entity.getHitbox())) {
                entity.damagedState.enter(getDamageInformation());
            }
        }
    }
}
