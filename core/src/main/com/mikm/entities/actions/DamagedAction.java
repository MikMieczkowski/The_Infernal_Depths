package com.mikm.entities.old.states;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mikm.ExtraMathUtils;
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

    private float knockbackAngle;
    public boolean dead;

    public DamagedAction(Entity entity) {
        super(entity);
    }

    public void enter(float knockbackAngle) {
        if (entity.effectsHandler.inInvincibility || !entity.isAttackable) {
            return;
        }
        if (!entity.SHOULD_RESTART_CYCLE_POST_HIT) {
            super.enter();
            this.knockbackAngle = knockbackAngle;
            entity.startSquish(TOTAL_KNOCKBACK_TIME * .75f, 1.2f);
        }
        if (entity.DAMAGE == 0) {
            SoundEffects.playLoud(FAILED_HIT_SOUND_EFFECT);
            return;
        }
        SoundEffects.play(entity.HURT_SOUND_EFFECT);

        SoundEffects.play(entity.HURT_SOUND_EFFECT);
        entity.hp -= entity.DAMAGE;
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
        Vector2 knockbackForce = new Vector2(MathUtils.cos(knockbackAngle) * entity.KNOCKBACK,
                MathUtils.sin(knockbackAngle) * entity.KNOCKBACK);

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
    public void onExit() {
        if (dead) {
            new ParticleEffect(ParticleTypes.getKnockbackDustParameters(), knockbackAngle, entity.x, entity.y);
            entity.die();
        }
    }
}
