package com.mikm.entities.enemies.slimeBoss;

import com.badlogic.gdx.graphics.Color;
import com.mikm.entities.Entity;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.enemies.states.DamagedState;
import com.mikm.entities.particles.ParticleTypes;
import com.mikm.entities.particles.ParticleEffect;
import com.mikm.entities.projectiles.DamageInformation;

public class SB_DamagedState extends DamagedState {

    private DamageInformation damageInformation;

    public SB_DamagedState(Entity entity) {
        super(entity);
    }

    @Override
    public void enter(DamageInformation damageInformation) {
        if (entity.inInvincibility) {
            return;
        }
        this.damageInformation = damageInformation;
        entity.hp -= damageInformation.damage;
        entity.startInvincibilityFrames();
        if (entity.hp <= 0) {
            entity.flash(Color.RED);
            dead = true;
            super.enter(damageInformation);
        } else {
            entity.flash(Color.WHITE);
        }
    }

    @Override
    public void update() {
        if (dead) {
            super.update();
        }
    }

    @Override
    public void checkForStateTransition() {
        if (timeElapsedInState > .5f) {
            if (dead) {
                new ParticleEffect(ParticleTypes.getKnockbackDustParameters(),damageInformation.knockbackAngle, entity.x, entity.y);
                entity.die();
            }
        }
    }

    @Override
    protected AnimationName getAnimationName() {
        return AnimationName.HIT;
    }
}
