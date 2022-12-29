package com.mikm.entities.player;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.mikm.Vector2Int;
import com.mikm.entities.Entity;
import com.mikm.entities.player.states.DivingState;
import com.mikm.entities.player.states.StandingState;
import com.mikm.entities.player.states.State;
import com.mikm.entities.player.states.WalkingState;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.GameScreen;

import java.util.ArrayList;

public class Player extends Entity {
    public Vector2Int direction = Vector2Int.DOWN;

    public ArrayList<TextureRegion[]> spritesheets;
    public static final int playerWidthPixels = 32, playerHeightPixels = 32;
    public final float speed = 2;

    public final float diveSpeed = 6;
    public final float diveFriction = .3f;
    public final float diveFrictionSpeed = .317f;
    public final float diveStartingSinCount = 1;
    public final float diveEndTimeFrame = 0.2f;

    public Group group;
    private PlayerHeldItem playerHeldItem;
    private PlayerBackItem playerBackItem;

    public WalkingState walkingState;
    public DivingState divingState;
    public StandingState standingState;
    public State currentState;

    public Player(int x, int y, ArrayList<TextureRegion[]> spritesheets) {
        this.x = x;
        this.y = y;
        this.spritesheets = spritesheets;

        walkingState = new WalkingState(this);
        divingState = new DivingState(this);
        standingState = new StandingState(this);
        currentState = standingState;
        createGroup();
    }

    public void setScreen(GameScreen screen) {
        this.screen = screen;
    }

    @Override
    public void update() {
        currentState.update();
        currentState.handleInput();
        checkWallCollisions();
        if (InputAxis.isMoving()) {
            direction = new Vector2Int(InputAxis.getHorizontalAxisInt(), InputAxis.getVerticalAxisInt());
        }
        x += xVel;
        y += yVel;
    }

    @Override
    public void render(Batch batch) {
        currentState.animationTime += Gdx.graphics.getDeltaTime();
        drawCurrentAnimation(batch);
    }

    private void drawCurrentAnimation(Batch batch) {
        if (currentState.currentAnimation != null) {
            if (currentState.animationIsFlipped) {
                batch.draw(currentState.currentAnimation.getKeyFrame(currentState.animationTime), x + playerWidthPixels, y, -playerWidthPixels, playerHeightPixels);
            } else {
                batch.draw(currentState.currentAnimation.getKeyFrame(currentState.animationTime), x, y, playerWidthPixels, playerHeightPixels);
            }
        }
    }

    private void createGroup() {
        group = new Group();
        playerBackItem = new PlayerBackItem();
        playerHeldItem = new PlayerHeldItem();
        group.addActor(this);
        group.addActor(playerBackItem);
        group.addActor(playerHeldItem);
    }


    public Rectangle getFullBounds() {
        //return new Rectangle(0, 0, 0,0);
        return new Rectangle(x, y, playerWidthPixels, playerHeightPixels);
    }

    @Override
    public Rectangle getBounds() {
        //return new Rectangle(0, 0, 0,0);
        return new Rectangle(x+8, y+8, 16, 16);
    }
}
