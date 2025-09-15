package com.mikm.entities.enemies.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.mikm.entities.Entity;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.projectiles.DamageInformation;
import com.mikm.rendering.screens.Application;

public abstract class State {
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
        entity.setDirectionalAnimation(getAnimationName());
        entity.animationManager.resetTimer();
        entity.animationManager.update();
        entity.currentState = this;
    }

    public boolean handlePlayerCollision(int contactDamage, boolean interruptsState) {
        boolean hitboxesOverlap = Intersector.overlaps(entity.getHitbox(), Application.player.getHitbox());
        if (hitboxesOverlap) {
            float angleToPlayer = MathUtils.atan2(Application.player.getCenteredPosition().y - entity.y, Application.player.getCenteredPosition().x - entity.x);
            if (interruptsState) {
                entity.standingState.enter();
            }
            Application.player.damagedState.enter(new DamageInformation(angleToPlayer, CONTACT_KNOCKBACK_FORCE, contactDamage));
            return true;
        }
        return false;
    }

    public void update() {
        timeElapsedInState += Gdx.graphics.getDeltaTime();
        entity.animationManager.update();
    }

    protected abstract AnimationName getAnimationName();
    public abstract void checkForStateTransition();
}
