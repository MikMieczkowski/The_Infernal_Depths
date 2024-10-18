package com.mikm.entities.enemies.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.Entity;
import com.mikm.entities.State;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.enemies.Bat;
import com.mikm.entities.particles.ParticleEffect;
import com.mikm.entities.particles.ParticleTypes;
import com.mikm.entities.projectiles.DamageInformation;
import com.mikm.rendering.SoundEffects;
import com.mikm.rendering.screens.Application;

public class DamagedState extends State {
    protected final float TOTAL_KNOCKBACK_TIME = .25f;
    private final float JUMP_HEIGHT = 8f;
    final float DEATH_KNOCKBACK_MULTIPLIER = 3f;

    protected DamageInformation damageInformation;
    public boolean dead;


    public DamagedState(Entity entity) {
        super(entity);
    }

    public void enter(DamageInformation damageInformation) {
        if (entity.inInvincibility || !entity.isAttackable) {
            return;
        }
        super.enter();
        this.damageInformation = damageInformation;
        entity.startSquish(TOTAL_KNOCKBACK_TIME*.75f, 1.2f);
        if (damageInformation.damage == 0) {
            return;
        }
        if (entity == Application.player) {
            SoundEffects.play(SoundEffects.playerHit);
        } else {
            SoundEffects.play(SoundEffects.hit);
        }
        if (entity.hitSound!=null) {
            SoundEffects.play(entity.hitSound);
        }
        entity.hp -= damageInformation.damage;
        if (entity.hp <= 0) {
            entity.flash(Color.RED);
            dead = true;
        } else {
            entity.flash(Color.WHITE);
        }
        if (entity == Application.player) {
            Application.getInstance().freezeTime();
            Application.player.equippedWeapon.exitAttackState();
        }
        entity.startInvincibilityFrames();
    }

    @Override
    public void update() {
        super.update();
        Vector2 knockbackForce = new Vector2(MathUtils.cos(damageInformation.knockbackAngle) * damageInformation.knockbackForceMagnitude,
                MathUtils.sin(damageInformation.knockbackAngle) * damageInformation.knockbackForceMagnitude);

        Vector2 sinLerpedKnockbackForce = ExtraMathUtils.sinLerpVector2(timeElapsedInState,TOTAL_KNOCKBACK_TIME,.1f, 1f, knockbackForce);
        float jumpOffset = ExtraMathUtils.sinLerp(timeElapsedInState, TOTAL_KNOCKBACK_TIME * (dead ? 1 : .75f), .1f, 1f, JUMP_HEIGHT);
        if (dead) {
            sinLerpedKnockbackForce = sinLerpedKnockbackForce.scl(DEATH_KNOCKBACK_MULTIPLIER);
            jumpOffset *= 1.5f;
        }
        entity.height = jumpOffset;
        entity.xVel = sinLerpedKnockbackForce.x;
        entity.yVel = sinLerpedKnockbackForce.y;
    }

    @Override
    public void checkForStateTransition() {
        if (timeElapsedInState > TOTAL_KNOCKBACK_TIME) {
            if (dead) {
                new ParticleEffect(ParticleTypes.getKnockbackDustParameters(),damageInformation.knockbackAngle, entity.x, entity.y);
                entity.die();
            }
            entity.standingState.enter();
        }
    }

    @Override
    protected AnimationName getAnimationName() {
        return AnimationName.HIT;
    }
}
