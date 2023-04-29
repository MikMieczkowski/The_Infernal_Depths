package com.mikm.entities.enemies.slimeBoss;

import com.mikm.entities.State;
import com.mikm.entities.player.Player;
import com.mikm.rendering.screens.Application;

public class SB_SimmerBuildUpState extends State {
    private final SlimeBoss slimeBoss;
    private Player player;

    private final float MAX_BUILDUP_TIME = 2f;
    private final float SIMMER_BUILDUP_SQUISH_AMOUNT = .6f;

    public SB_SimmerBuildUpState(SlimeBoss slimeBoss) {
        super(slimeBoss);
        this.slimeBoss = slimeBoss;
        this.player = Application.player;

        animationManager = slimeBoss.jumpState.animationManager;
    }

    public void enter() {
        super.enter();
        slimeBoss.xVel = 0;
        slimeBoss.yVel = 0;
        slimeBoss.startSquish(0, SIMMER_BUILDUP_SQUISH_AMOUNT, MAX_BUILDUP_TIME, true);
    }

    @Override
    public void update() {
        super.update();
        handlePlayerCollision(1, false);
    }

    @Override
    public void checkForStateTransition() {
        if (timeElapsedInState > MAX_BUILDUP_TIME) {
            slimeBoss.startSquish(0,1.1f, .05f, true);
            slimeBoss.simmerState.enter();
        }
    }
}
