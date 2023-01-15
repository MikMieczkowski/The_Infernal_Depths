package com.mikm.entities.player.states;

import com.badlogic.gdx.math.Vector2;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.State;
import com.mikm.entities.player.Player;
import com.mikm.input.GameInput;

public abstract class PlayerWalking extends State {
    private Player player;
    private final Vector2 targetVelocity = new Vector2();
    private final Vector2 startingVelocity = new Vector2();

    final float ACCELERATION;
    final float DECELERATION;

    public PlayerWalking(Player player) {
        super(player);
        this.player = player;
        ACCELERATION = player.getSpeed() / player.ACCELERATION_FRAMES;
        DECELERATION = player.getSpeed() / player.DECELERATION_FRAMES;
    }

    public void checkIfWalking() {
        if (GameInput.getHorizontalAxis() != 0) {
            xAccelerate();
        } else {
            xDecelerate();
        }

        if (GameInput.getVerticalAxis() != 0) {
            yAccelerate();
        } else {
            yDecelerate();
        }
    }

    private void xAccelerate() {
        player.xVel += ACCELERATION * GameInput.getHorizontalAxis();
        clampVelocityX();
        startingVelocity.x = player.xVel;
    }

    private void yAccelerate() {
        player.yVel += ACCELERATION * GameInput.getVerticalAxis();
        clampVelocityY();
        startingVelocity.y = player.yVel;
    }

    private void xDecelerate() {
        player.xVel -= DECELERATION * ExtraMathUtils.sign(startingVelocity.x);
        if (!ExtraMathUtils.haveSameSign(player.xVel, startingVelocity.x)) {
            player.xVel = 0;
        }
    }

    private void yDecelerate() {
        player.yVel -= DECELERATION * ExtraMathUtils.sign(startingVelocity.y);
        if (!ExtraMathUtils.haveSameSign(player.yVel, startingVelocity.y)) {
            player.yVel = 0;
        }
    }

    private void clampVelocityX() {
        if (GameInput.getHorizontalAxis() != 0) {
            targetVelocity.x = player.getSpeed() * GameInput.getHorizontalAxis();
        }
        float topXSpeed = Math.abs(targetVelocity.x);

        if (player.xVel < -topXSpeed) {
            player.xVel = -topXSpeed;
        }
        if (player.xVel > topXSpeed) {
            player.xVel = topXSpeed;
        }
    }

    private void clampVelocityY() {
        if (GameInput.getVerticalAxis() != 0) {
            targetVelocity.y = player.getSpeed() * GameInput.getVerticalAxis();
        }
        float topYSpeed = Math.abs(targetVelocity.y);

        if (player.yVel < -topYSpeed) {
            player.yVel = -topYSpeed;
        }
        if (player.yVel > topYSpeed) {
            player.yVel = topYSpeed;
        }
    }

    @Override
    public void checkForStateTransition() {

    }
}
