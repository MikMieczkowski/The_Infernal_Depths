package com.mikm.entities.actions;

import com.mikm.entities.Entity;

public class ExplodeBehaviour extends Behaviour {
    private float HITBOX_MULTIPLIER;
    private int MULTIPLY_ACTIVATE_FRAME;
    private float activateTime;
    private boolean activatedHitboxMultiplier = false;

    public ExplodeBehaviour(Entity entity) {
        super(entity);
    }

    @Override
    public void postConfigRead() {
        activateTime = (MULTIPLY_ACTIVATE_FRAME - 1) * entity.animationHandler.getTimePerFrame();
    }

    @Override
    public void update() {
        super.update();
        if (timeElapsedInState > activateTime && !activatedHitboxMultiplier) {
            entity.hitbox.radius *= HITBOX_MULTIPLIER;
            activatedHitboxMultiplier = true;
        }
    }

    @Override
    public void onExit() {
        entity.hitbox.radius /= HITBOX_MULTIPLIER;
        activatedHitboxMultiplier = false;
    }
}
