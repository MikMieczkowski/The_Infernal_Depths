package com.mikm.entities.player.states;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mikm.entities.animation.AnimationManager;
import com.mikm.entities.animation.ActionAnimationAllDirections;
import com.mikm.input.GameInput;
import com.mikm.entities.player.Player;
import com.mikm.entities.State;

public class PlayerRollingState extends State {
    private final Player player;
    private Vector2 rollForce = new Vector2();
    private float rollSpeedSinCounter, heightSinCounter;
    private boolean jumpDone = false;

    public PlayerRollingState(Player player) {
        super(player);
        this.player = player;
        ActionAnimationAllDirections actionAnimationAllDirections = new ActionAnimationAllDirections(.055f, Animation.PlayMode.NORMAL,
                player.entityActionSpritesheets.playerRolling);
        animationManager = new AnimationManager(player, actionAnimationAllDirections);
    }

    @Override
    public void enter() {
        super.enter();
        player.xVel = 0;
        player.yVel = 0;
        heightSinCounter = 0;
        jumpDone = false;
        rollSpeedSinCounter = player.ROLL_STARTING_SIN_COUNT;
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
        if (rollSpeedSinCounter < MathUtils.PI - player.ROLL_ENDING_TIME) {
            rollSpeedSinCounter += player.ROLL_FRICTION - (player.ROLL_FRICTION_SPEED * player.ROLL_FRICTION * rollSpeedSinCounter);
        } else {
            player.height = 0;
            player.walkingState.enter();
            return;
        }
        if (rollSpeedSinCounter >= MathUtils.PI) {
            rollSpeedSinCounter = MathUtils.PI;
        }


        rollForce = new Vector2(player.ROLL_SPEED * MathUtils.sin(rollSpeedSinCounter) * GameInput.getHorizontalAxis(),
                player.ROLL_SPEED * MathUtils.sin(rollSpeedSinCounter) * GameInput.getVerticalAxis());
    }

    private void setJumpHeight() {
        if (!jumpDone) {
            if (heightSinCounter < MathUtils.PI) {
                heightSinCounter += player.ROLL_JUMP_SPEED;
            }
            if (heightSinCounter >= MathUtils.PI) {
                heightSinCounter = 0;
                player.startSquish(0.01f, 1.2f);
                jumpDone = true;
            }
            player.height = player.ROLL_JUMP_HEIGHT * MathUtils.sin(heightSinCounter);
        }
    }
}
