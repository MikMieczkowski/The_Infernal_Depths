package com.mikm.entities.enemies.slimeBoss;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.mikm.entities.Entity;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.enemies.states.DamagedState;
import com.mikm.entities.particles.ParticleTypes;
import com.mikm.entities.particles.ParticleEffect;
import com.mikm.entities.projectiles.DamageInformation;
import com.mikm.rendering.SoundEffects;

public class SB_DamagedState extends DamagedState {

    public SB_DamagedState(Entity entity) {
        super(entity);
    }

    @Override
    public void enter(DamageInformation damageInformation) {
        if (entity.inInvincibility) {
            return;
        }
        SoundEffects.play(SoundEffects.hit);
        this.damageInformation = damageInformation;
        entity.hp -= damageInformation.damage;
        entity.startInvincibilityFrames();
        if (entity.hp <= 0) {
            entity.flash(Color.RED);
            dead = true;
            SlimeBoss.defeated = true;
            super.enter();
        } else {
            entity.flash(Color.WHITE);
        }
    }

    @Override
    public void update() {
        timeElapsedInState+=Gdx.graphics.getDeltaTime();
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
