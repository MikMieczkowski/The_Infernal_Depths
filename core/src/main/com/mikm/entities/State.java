package com.mikm.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.mikm.entities.animation.AnimationManager;
import com.mikm.entities.projectiles.DamageInformation;
import com.mikm.rendering.screens.Application;

public abstract class State {
    public AnimationManager animationManager;
    public final Entity entity;
    public static final int CONTACT_KNOCKBACK_FORCE = 2;
    public float timeElapsedInState;

    public State(Entity entity) {
        this.entity = entity;
    }

    public void enter() {
        if (entity.currentState == this) {
            return;
        }
        timeElapsedInState = 0;
        animationManager.resetTimer();
        animationManager.setCurrentAnimation();
        entity.currentState = this;
    }

    public void checkIfCollidedWithPlayer(float contactDamage, boolean interruptsState) {
        boolean hitboxesOverlap = Intersector.overlaps(entity.getHitbox(), Application.player.getHitbox());
        if (hitboxesOverlap && Application.player.isAttackable) {
            float angleToPlayer = MathUtils.atan2(Application.player.getCenteredPosition().y - entity.y, Application.player.getCenteredPosition().x - entity.x);
            if (interruptsState) {
                entity.standingState.enter();
            }
            Application.player.damagedState.enter(new DamageInformation(angleToPlayer, CONTACT_KNOCKBACK_FORCE, contactDamage));
        }
    }

    public void update() {
        timeElapsedInState += Gdx.graphics.getDeltaTime();
        animationManager.setCurrentAnimation();
    }

    public abstract void checkForStateTransition();
}
