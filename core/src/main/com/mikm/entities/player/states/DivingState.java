package com.mikm.entities.player.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mikm.Vector2Int;
import com.mikm.entities.player.ANIMATIONS;
import com.mikm.entities.player.InputAxis;
import com.mikm.entities.player.Player;
import com.mikm.rendering.screens.Application;

public class DivingState extends State {
    private Vector2 diveForce = new Vector2();
    private Vector2Int diveDirection = new Vector2Int();
    private float sinCounter;

    public DivingState(Player player) {
        super(player);
    }

    @Override
    public void enter() {
        super.enter();
        player.xVel = 0;
        player.yVel = 0;
        animationTime = 0;
        sinCounter = player.diveStartingSinCount;

        diveForce = new Vector2(player.diveSpeed * MathUtils.sin(sinCounter) * InputAxis.getHorizontalAxis(),
                player.diveSpeed * MathUtils.sin(sinCounter) * InputAxis.getVerticalAxis());
        diveDirection = new Vector2Int(player.direction.x, player.direction.y);
        super.update();
    }

    @Override
    void createAnimations() {
        for (int i = 0; i < 5; i++) {
            int indexOfAnimation = i + ANIMATIONS.Character_RollDown.ordinal();
            animations.add(new Animation<>(.07f, player.spritesheets.get(indexOfAnimation)));
            animations.get(i).setPlayMode(Animation.PlayMode.NORMAL);
        }
    }



    @Override
    public void update() {
        player.xVel = diveForce.x;
        player.yVel = diveForce.y;
        setDiveForce();
    }

    @Override
    public void handleInput() {
        if (InputAxis.isDiveButtonPressed() && sinCounter > MathUtils.PI - player.diveEndTimeFrame) {
            player.walkingState.enter();
        }
    }

    private void setDiveForce() {
        if (sinCounter < MathUtils.PI) {
            sinCounter += player.diveFriction - (player.diveFrictionSpeed * player.diveFriction * sinCounter);
        } else {
            player.walkingState.enter();
            return;
        }
        if (sinCounter >= MathUtils.PI) {
            sinCounter = MathUtils.PI;
        }

        Vector2 normalizedDiveDirection = getNormalizedDiveDirection(diveDirection);
        diveForce = new Vector2(player.diveSpeed * MathUtils.sin(sinCounter) * normalizedDiveDirection.x,
                player.diveSpeed * MathUtils.sin(sinCounter) * normalizedDiveDirection.y);
    }

    private Vector2 getNormalizedDiveDirection(Vector2Int diveDirection) {
        Vector2 diveDirectionVector2 = new Vector2(diveDirection.x, diveDirection.y);
        float magnitude = diveDirectionVector2.len();
        if (magnitude > 1) {
            magnitude = 1;
        }
        return new Vector2(diveDirectionVector2.nor().x * magnitude, diveDirectionVector2.nor().y * magnitude);
    }
}
