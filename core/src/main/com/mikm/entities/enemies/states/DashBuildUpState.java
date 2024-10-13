package com.mikm.entities.enemies.states;

import com.mikm.entities.Entity;
import com.mikm.entities.State;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.enemies.Slime;

public class DashBuildUpState extends State {
    private float maxBuildupTime;
    private float angle;
    private Entity entity;
    private boolean slimeBossMinion;

    public DashBuildUpState(Entity entity, float maxBuildupTime) {
        super(entity);
        this.maxBuildupTime = maxBuildupTime;
        this.entity = entity;
    }

    @Override
    public void enter() {
        super.enter();
        entity.startSquish(0, 1.5f, maxBuildupTime, false);
        slimeBossMinion = false;
    }

    public void enter(float angle) {
        super.enter();
        this.angle = angle;
        slimeBossMinion = true;
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void checkForStateTransition() {
        if (timeElapsedInState > maxBuildupTime) {
            if (slimeBossMinion) {
                entity.dashingState.enter(angle);
            } else {
                entity.dashingState.enter();
            }
        }
    }

    @Override
    protected AnimationName getAnimationName() {
        return AnimationName.STAND;
    }
}
