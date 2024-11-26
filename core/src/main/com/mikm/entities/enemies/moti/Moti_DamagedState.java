package com.mikm.entities.enemies.moti;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.enemies.states.DamagedState;
import com.mikm.entities.particles.ParticleTypes;
import com.mikm.entities.particles.ParticleEffect;
import com.mikm.entities.projectiles.DamageInformation;
import com.mikm.rendering.SoundEffects;

public class Moti_DamagedState extends DamagedState {
    private Moti moti;
    protected final float TOTAL_KNOCKBACK_TIME = .25f;
    private final float JUMP_HEIGHT = 8f;

    public Moti_DamagedState(Moti moti) {
        super(moti);
        this.moti = moti;
    }

    @Override
    public void enter(DamageInformation damageInformation) {
        super.enter(damageInformation);
        if (moti.tripleDashState.dash < 2) {
            moti.stateManager.trip = !moti.stateManager.trip;
        }
        moti.tripleDashState.dash++;
    }

    @Override
    public void update() {
        timeElapsedInState+=Gdx.graphics.getDeltaTime();
        Vector2 knockbackForce = new Vector2(MathUtils.cos(damageInformation.knockbackAngle) * damageInformation.knockbackForceMagnitude,
                MathUtils.sin(damageInformation.knockbackAngle) * damageInformation.knockbackForceMagnitude);

        Vector2 sinLerpedKnockbackForce = ExtraMathUtils.sinLerpVector2(timeElapsedInState,TOTAL_KNOCKBACK_TIME,.1f, 1f, knockbackForce);
        float jumpOffset = ExtraMathUtils.sinLerp(timeElapsedInState, TOTAL_KNOCKBACK_TIME * (dead ? 1 : .75f), .1f, 1f, JUMP_HEIGHT);
        moti.height = jumpOffset;
        moti.xVel = sinLerpedKnockbackForce.x;
        moti.yVel = sinLerpedKnockbackForce.y;
        if (dead) {
            super.update();
        }
    }

    @Override
    public void checkForStateTransition() {
        if (timeElapsedInState > TOTAL_KNOCKBACK_TIME) {
            if (dead) {
                new ParticleEffect(ParticleTypes.getKnockbackDustParameters(),damageInformation.knockbackAngle, moti.x, moti.y);
                moti.die();
            }
            moti.stateManager.updateState();
        }
    }

    @Override
    protected AnimationName getAnimationName() {
        return AnimationName.HIT;
    }
}
