package com.mikm.entities.enemies.states;

import com.mikm.entities.State;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.enemies.Slime;

public class DashBuildUpState extends State {
    private final float MAX_BUILDUP_TIME = 1;
    private float angle;
    private Slime slime;
    private boolean slimeBossMinion;

    public DashBuildUpState(Slime slime) {
        super(slime);
        this.slime = slime;
    }

    @Override
    public void enter() {
        super.enter();
        slime.startSquish(0, 1.5f, MAX_BUILDUP_TIME, false);
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
        if (timeElapsedInState > MAX_BUILDUP_TIME) {
            if (slimeBossMinion) {
                slime.dashingState.enter(angle);
            } else {
                slime.dashingState.enter();
            }
        }
    }

    @Override
    protected AnimationName getAnimationName() {
        return AnimationName.STAND;
    }
}
