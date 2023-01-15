package com.mikm.entities;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.mikm.entities.animation.AnimationManager;
import com.mikm.entities.projectiles.DamageInformation;
import com.mikm.rendering.screens.Application;

public abstract class State {
    public AnimationManager animationManager;
    public final Entity entity;
    public static final int CONTACT_KNOCKBACK_FORCE = 2;

    public State(Entity entity) {
        this.entity = entity;
    }

    public void enter() {
        if (entity.currentState == this) {
            return;
        }
        animationManager.resetTimer();
        animationManager.setCurrentAnimation();
        entity.currentState = this;
    }

    public void checkIfCollidedWithPlayer(float contactDamage) {
        boolean hitboxesOverlap = Intersector.overlaps(entity.getHitbox(), Application.player.getHitbox());
        if (hitboxesOverlap) {
            float angleToPlayer = MathUtils.atan2(Application.player.getCenteredPosition().y - entity.y, Application.player.getCenteredPosition().x - entity.x);
            entity.standingState.enter();
            Application.player.damagedState.enter(new DamageInformation(angleToPlayer, CONTACT_KNOCKBACK_FORCE, contactDamage));
        }
    }

    public void update() {
        animationManager.setCurrentAnimation();
    }

    public abstract void checkForStateTransition();
}
