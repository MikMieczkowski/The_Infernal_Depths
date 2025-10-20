package com.mikm.entities.actions;

import com.mikm.entities.Entity;

public class ExplodeAction extends Action {
    private float HITBOX_MULTIPLIER;
    private int MULTIPLY_ACTIVATE_FRAME;
    private float activateTime;
    private boolean activatedHitboxMultiplier = false;

    public ExplodeAction(Entity entity) {
        super(entity);
    }

    @Override
    public void postConfigRead() {
        super.postConfigRead();
        activateTime = (MULTIPLY_ACTIVATE_FRAME - 1) * animation.getFrameDuration();
    }

    @Override
    public void enter() {
        super.enter();
        entity.xVel = 0;
        entity.yVel = 0;
    }

    @Override
    public void update() {
        super.update();
        // Keep velocity clamped to zero during explode to avoid residual motion from collisions
        entity.xVel = 0;
        entity.yVel = 0;
        if (timeElapsedInState > activateTime && !activatedHitboxMultiplier) {
            entity.getHitbox().radius *= HITBOX_MULTIPLIER;
            activatedHitboxMultiplier = true;
        }
    }

    @Override
    public void onExit() {
        entity.getHitbox().radius /= HITBOX_MULTIPLIER;
        activatedHitboxMultiplier = false;
        super.onExit();
    }
}
