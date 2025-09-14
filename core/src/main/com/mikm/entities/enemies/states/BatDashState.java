package com.mikm.entities.enemies.states;

import com.mikm.entities.enemies.Bat;

public class BatDashState extends DashingState {

    private float startHeight = 0;
    private final float MAX_DASH_TIME = .6f;
    private Bat bat;
    private final int DASH_DAMAGE = 1;

    public BatDashState(Bat bat, float dashSpeed) {
        super(bat, dashSpeed);
        this.bat = bat;
        //TODO Auto-generated constructor stub
    }

    @Override
    public void checkForStateTransition() {
        boolean changed = false;
        if (timeElapsedInState > MAX_DASH_TIME) {
            //Only change this class provides
            bat.shockState.enter();
            changed = true;
        }
        //changed = changed || handlePlayerCollision(DASH_DAMAGE, true);
        if (changed) {
            bat.height = startHeight;
            bat.xVel = 0;
            bat.yVel = 0;
        }
    }
    
}
