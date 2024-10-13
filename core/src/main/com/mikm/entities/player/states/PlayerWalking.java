package com.mikm.entities.player.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.mikm.DeltaTime;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.State;
import com.mikm.entities.player.Player;
import com.mikm.input.GameInput;
import com.mikm.rendering.SoundEffects;

public abstract class PlayerWalking extends State {
    private Player player;
    private final Vector2 targetVelocity = new Vector2();
    private final Vector2 startingVelocity = new Vector2();

    float ACCELERATION;
    float DECELERATION;
    private final float STEP_DELAY = .66f;
    private float stepTimer = 0;


    public PlayerWalking(Player player) {
        super(player);
        this.player = player;
    }

    public void checkIfWalking() {
        ACCELERATION = player.getSpeed() * DeltaTime.deltaTime() / player.ACCELERATION_FRAMES;
        DECELERATION = player.getSpeed() * DeltaTime.deltaTime() / player.DECELERATION_FRAMES;
        stepTimer -= Gdx.graphics.getDeltaTime();
        if (stepTimer < 0 && (player.xVel !=0 || player.yVel !=0)) {
            stepTimer += STEP_DELAY;
            SoundEffects.play(SoundEffects.step);
        };
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
