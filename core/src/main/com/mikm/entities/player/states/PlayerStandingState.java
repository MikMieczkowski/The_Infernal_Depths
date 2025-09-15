package com.mikm.entities.player.states;

import com.badlogic.gdx.math.Vector2;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.enemies.states.State;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.player.Player;
import com.mikm.input.GameInput;


public class PlayerStandingState extends State {
    private final Player player;
    private Vector2 startingVelocity;

    public PlayerStandingState(Player player) {
        super(player);
        this.player = player;
    }

    @Override
    public void enter() {
        super.enter();
        //Prevents frame of being in wrong state
        checkForStateTransition();
        startingVelocity = new Vector2(player.xVel, player.yVel);
    }

    @Override
    public void update() {
        super.update();

        float DECELERATION = player.getSpeed() / player.DECELERATION_FRAMES;
        player.xVel -= DECELERATION * ExtraMathUtils.sign(startingVelocity.x);
        player.yVel -= DECELERATION * ExtraMathUtils.sign(startingVelocity.y);
        if (!ExtraMathUtils.haveSameSign(player.xVel, startingVelocity.x)) {
            player.xVel = 0;
        }
        if (!ExtraMathUtils.haveSameSign(player.yVel, startingVelocity.y)) {
            player.yVel = 0;
        }

//        int xVelDirection = ExtraMathUtils.sign(player.xVel);
//        int yVelDirection = ExtraMathUtils.sign(player.yVel);
//        float absXVel = Math.abs(player.xVel);
//        float absYVel = Math.abs(player.yVel);
//
//        float deceleration = player.speed / player.DECELERATION_FRAMES;
//        absXVel -= deceleration;
//        absYVel -= deceleration;
//
//        if (absXVel < 0) {
//            absXVel = 0;
//        }
//        if (absYVel < 0) {
//            absYVel = 0;
//        }
//
//        player.xVel = xVelDirection * absXVel;
//        player.yVel = yVelDirection * absYVel;
    }

    @Override
    public void checkForStateTransition() {
        if (GameInput.isMoving()) {
            player.walkingState.enter();
        }
        if (GameInput.isAttackButtonPressed()) {
            player.attackingState.enter();
        }
    }

    @Override
    protected AnimationName getAnimationName() {
        return AnimationName.STAND;
    }
}
