package com.mikm.entities.actions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.DamageInformation;
import com.mikm.entities.Entity;
import com.mikm.entities.actions.Action;
import com.mikm.entities.inanimateEntities.particles.ParticleEffect;
import com.mikm.entities.inanimateEntities.particles.ParticleTypes;
import com.mikm.rendering.sound.SoundEffects;
import com.mikm.rendering.screens.Application;

public class DamagedAction extends Action {
    private final String FAILED_HIT_SOUND_EFFECT = "bowImpact.ogg";

    protected final float TOTAL_KNOCKBACK_TIME = .25f;
    private final float JUMP_HEIGHT = 8f;
    final float DEATH_KNOCKBACK_MULTIPLIER = 3f;

    public boolean dead;
    private DamageInformation damageInformation;
    public boolean active;

    public DamagedAction(Entity entity) {
        super(entity);
    }

    public void enter(DamageInformation damageInformation) {
        if (entity.effectsHandler.inInvincibility || !entity.isAttackable) {
            return;
        }
        // Always capture the incoming damage info and reset state for consistent knockback
        this.damageInformation = damageInformation;
        dead = false;
        active = true;
        timeElapsedInState = 0;
        if (entity.routineHandler.currentRoutine.cycle.currentAction.ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO == null) {
            super.enter();
            entity.startSquish(TOTAL_KNOCKBACK_TIME * .75f, 1.2f);
        }
        if (damageInformation.damage == 0) {
            SoundEffects.playLoud(FAILED_HIT_SOUND_EFFECT);
            return;
        }
        SoundEffects.play(entity.HURT_SOUND_EFFECT);
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

        float totalTime = TOTAL_KNOCKBACK_TIME * (dead ? 1f : .75f);
        if (timeElapsedInState >= totalTime && active) {
            // finish damaged state and trigger death if applicable
            onExit();
            active = false;
            if (!dead) {
                entity.xVel = 0;
                entity.yVel = 0;
            }
        }
    }

    @Override
    public void onExit() {
        if (dead) {
            new ParticleEffect(ParticleTypes.getKnockbackDustParameters(), damageInformation.knockbackAngle, entity.x, entity.y);
            if (entity.NAME.equals("slime")) {
                com.mikm.rendering.sound.SoundEffects.play("slimeDeath.ogg");
            }
            entity.die();
        }
        active = false;
        super.onExit();
    }
}
