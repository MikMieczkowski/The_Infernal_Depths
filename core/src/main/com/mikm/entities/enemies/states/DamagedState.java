package com.mikm.entities.enemies.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.particles.ParticleParameters;
import com.mikm.entities.particles.ParticleSystem;
import com.mikm.entities.projectiles.DamageInformation;
import com.mikm.entities.Entity;
import com.mikm.entities.State;
import com.mikm.entities.animation.AnimationManager;
import com.mikm.entities.animation.ActionAnimationAllDirections;

public class DamagedState extends State {
    private final float TOTAL_KNOCKBACK_TIME = .25f;
    private final float JUMP_HEIGHT = 8f;
    private final float DEATH_KNOCKBACK_MULTIPLIER = 3f;

    private DamageInformation damageInformation;
    private float knockbackTime;
    private float knockbackSinLerpTimer;
    private boolean dead;


    public DamagedState(Entity entity) {
        super(entity);
        ActionAnimationAllDirections actionAnimationAllDirections = new ActionAnimationAllDirections(1, Animation.PlayMode.NORMAL, entity.entityActionSpritesheets.hit);
        animationManager = new AnimationManager(entity, actionAnimationAllDirections);
    }

    @Override
    public void enter() {
        throw new RuntimeException("must provide parameters for entering damagedstate");
    }

    public void enter(DamageInformation damageInformation) {
        super.enter();
        this.damageInformation = damageInformation;
        entity.hp -= damageInformation.damage;
        if (entity.hp <= 0) {
            entity.flash(Color.RED);
            dead = true;
        } else {
            entity.flash(Color.WHITE);
        }
        entity.startSquish(TOTAL_KNOCKBACK_TIME*.75f, 1.2f);
        knockbackTime = 0;
        knockbackSinLerpTimer = 0;
    }

    @Override
    public void update() {
        super.update();
        Vector2 knockbackForce = new Vector2(MathUtils.cos(damageInformation.knockbackAngle) * damageInformation.knockbackForceMagnitude,
                MathUtils.sin(damageInformation.knockbackAngle) * damageInformation.knockbackForceMagnitude);

        knockbackSinLerpTimer+=Gdx.graphics.getDeltaTime();
        Vector2 sinLerpedKnockbackForce = ExtraMathUtils.sinLerpVector2(knockbackSinLerpTimer,TOTAL_KNOCKBACK_TIME,.1f, 1f, knockbackForce);
        float jumpOffset = ExtraMathUtils.sinLerp(knockbackSinLerpTimer, TOTAL_KNOCKBACK_TIME * (dead ? 1 : .75f), .1f, 1f, JUMP_HEIGHT);
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
        knockbackTime += Gdx.graphics.getDeltaTime();
        if (knockbackTime > TOTAL_KNOCKBACK_TIME) {
            if (dead) {
                new ParticleSystem(ParticleParameters.getKnockbackDustParameters(),damageInformation.knockbackAngle, entity.x, entity.y);
                entity.die();
            }
            knockbackTime = 0;
            entity.standingState.enter();
        }
    }
}
