package com.mikm.entities.player.states;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mikm.entities.animation.AnimationManager;
import com.mikm.entities.animation.DirectionalAnimationSet;
import com.mikm.input.InputAxis;
import com.mikm.entities.player.PlayerAnimationNames;
import com.mikm.entities.player.Player;
import com.mikm.entities.states.State;

public class PlayerRollingState extends State {
    private final Player player;
    private Vector2 rollForce = new Vector2();
    private float rollSpeedSinCounter, heightSinCounter;
    private boolean jumpDone = false;

    public PlayerRollingState(Player player) {
        super(player);
        this.player = player;
        DirectionalAnimationSet directionalAnimationSet = new DirectionalAnimationSet(.055f, Animation.PlayMode.NORMAL,
                player.spritesheets, 5, PlayerAnimationNames.ROLL_DOWN.ordinal());
        animationManager = new AnimationManager(player, directionalAnimationSet);
    }

    @Override
    public void enter() {
        super.enter();
        player.xVel = 0;
        player.yVel = 0;
        heightSinCounter = 0;
        jumpDone = false;
        rollSpeedSinCounter = player.rollStartingSinCount;
    }

    @Override
    public void update() {
        super.update();
        setRollForce();
        setJumpHeight();
        player.xVel = rollForce.x;
        player.yVel = rollForce.y;
    }

    @Override
    public void checkForStateTransition() {

    }

    private void setRollForce() {
        if (rollSpeedSinCounter < MathUtils.PI - player.rollEndingTime) {
            rollSpeedSinCounter += player.rollFriction - (player.rollFrictionSpeed * player.rollFriction * rollSpeedSinCounter);
        } else {
            player.height = 0;
            player.walkingState.enter();
            return;
        }
        if (rollSpeedSinCounter >= MathUtils.PI) {
            rollSpeedSinCounter = MathUtils.PI;
        }


        rollForce = new Vector2(player.rollSpeed * MathUtils.sin(rollSpeedSinCounter) * InputAxis.getHorizontalAxis(),
                player.rollSpeed * MathUtils.sin(rollSpeedSinCounter) * InputAxis.getVerticalAxis());
    }

    private void setJumpHeight() {
        if (!jumpDone) {
            if (heightSinCounter < MathUtils.PI) {
                heightSinCounter += player.rollJumpSpeed;
            }
            if (heightSinCounter >= MathUtils.PI) {
                heightSinCounter = 0;
                jumpDone = true;
            }
            player.height = player.rollJumpHeight * MathUtils.sin(heightSinCounter);
        }
    }
}
