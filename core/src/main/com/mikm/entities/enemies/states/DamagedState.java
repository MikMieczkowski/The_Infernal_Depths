package com.mikm.entities.enemies.states;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.Entity;
import com.mikm.entities.State;
import com.mikm.entities.animation.OneDirectionalAnimationManager;
import com.mikm.entities.particles.ParticleTypes;
import com.mikm.entities.particles.ParticleEffect;
import com.mikm.entities.projectiles.DamageInformation;
import com.mikm.rendering.screens.Application;

public class DamagedState extends State {
    private final float TOTAL_KNOCKBACK_TIME = .25f;
    private final float JUMP_HEIGHT = 8f;
    private final float DEATH_KNOCKBACK_MULTIPLIER = 3f;

    private DamageInformation damageInformation;
    public boolean dead;


    public DamagedState(Entity entity) {
        super(entity);
        OneDirectionalAnimationManager oneDirectionalAnimationManager = new OneDirectionalAnimationManager(entity);
        oneDirectionalAnimationManager.animation = new Animation<>(1, entity.entityActionSpritesheets.hit);
        animationManager = oneDirectionalAnimationManager;
    }

    public void enter(DamageInformation damageInformation) {
        if (entity.inInvincibility) {
            return;
        }
        super.enter();
        this.damageInformation = damageInformation;
        entity.hp -= damageInformation.damage;
        if (entity.hp <= 0) {
            entity.flash(Color.RED);
            dead = true;
        } else {
            entity.flash(Color.WHITE);
        }
        if (entity == Application.player) {
            Application.freezeTime();
            Application.player.equippedWeapon.exitAttackState();
        }
        entity.startSquish(TOTAL_KNOCKBACK_TIME*.75f, 1.2f);
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
}
