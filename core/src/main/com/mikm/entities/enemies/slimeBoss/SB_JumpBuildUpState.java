package com.mikm.entities.enemies.slimeBoss;

import com.mikm.entities.State;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.player.Player;
import com.mikm.rendering.screens.Application;

public class SB_JumpBuildUpState extends State {
    private SlimeBoss slimeBoss;
    private Player player;

    private float MAX_BUILDUP_TIME;
    private float jumpDistance;
    private float timeSpentJumping;

    private float SHORT_JUMP_SQUISH_AMOUNT = 1.2f;
    private float SHORT_JUMP_BUILD_UP_TIME = 1;
    private float SHORT_JUMP_DISTANCE = 50;
    private float SHORT_JUMP_TIME_SPENT_JUMPING = 1;

    private float LONG_JUMP_SQUISH_AMOUNT = 2;
    private float LONG_JUMP_BUILD_UP_TIME = 1.5f;
    private float LONG_JUMP_DISTANCE = 150;
    private float LONG_JUMP_TIME_SPENT_JUMPING = .75f;

    public SB_JumpBuildUpState(SlimeBoss slimeBoss) {
        super(slimeBoss);
        this.slimeBoss = slimeBoss;
        this.player = Application.player;
    }

    @Override
    public void enter() {
        throw new RuntimeException("provide parameters");
    }

    public void enter(boolean shortJump) {
        super.enter();
        slimeBoss.xVel = 0;
        slimeBoss.yVel = 0;
        if (shortJump) {
            this.MAX_BUILDUP_TIME = SHORT_JUMP_BUILD_UP_TIME;
            this.jumpDistance = SHORT_JUMP_DISTANCE;
            this.timeSpentJumping = SHORT_JUMP_TIME_SPENT_JUMPING;
            slimeBoss.startSquish(0, SHORT_JUMP_SQUISH_AMOUNT, MAX_BUILDUP_TIME, true);
        } else {
            this.MAX_BUILDUP_TIME = LONG_JUMP_BUILD_UP_TIME;
            this.jumpDistance = LONG_JUMP_DISTANCE;
            this.timeSpentJumping = LONG_JUMP_TIME_SPENT_JUMPING;
            slimeBoss.startSquish(0, LONG_JUMP_SQUISH_AMOUNT, MAX_BUILDUP_TIME, true);
        }
    }

    @Override
    public void update() {
        super.update();
        handlePlayerCollision(1, false);
    }

    @Override
    public void checkForStateTransition() {
        if (timeElapsedInState > MAX_BUILDUP_TIME) {
            slimeBoss.jumpState.enter(jumpDistance, timeSpentJumping);
        }
    }

    @Override
    protected AnimationName getAnimationName() {
        return AnimationName.STAND;
    }
}
