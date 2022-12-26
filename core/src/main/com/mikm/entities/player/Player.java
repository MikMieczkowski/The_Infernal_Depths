package com.mikm.entities.player;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.mikm.entities.Entity;
import com.mikm.entities.player.states.DivingState;
import com.mikm.entities.player.states.StandingState;
import com.mikm.entities.player.states.State;
import com.mikm.entities.player.states.WalkingState;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.GameScreen;

public class Player extends Entity {
    public TextureRegion[][] spritesheet;
    public static final int playerWidthPixels = 22, playerHeightPixels = 22;
    public final float speed = 2;

    public final float diveSpeed = 6;
    public final float diveFriction = .3f;
    public final float diveFrictionSpeed = .317f;
    public final float diveStartingSinCount = 1;
    public final float diveEndTimeFrame = .3f;

    public Group group;
    private PlayerHeldItem playerHeldItem;
    private PlayerBackItem playerBackItem;

    public WalkingState walkingState;
    public DivingState divingState;
    public StandingState standingState;
    public State currentState;

    private boolean isFlipped = false;

    public Player(int x, int y, TextureRegion[][] spritesheet) {
        this.x = x;
        this.y = y;
        this.spritesheet = spritesheet;

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
        checkIfFlipped();
        currentState.update();
        currentState.handleInput();
        checkWallCollisions();
        x += xVel;
        y += yVel;
    }

    @Override
    public void render(Batch batch) {
        currentState.animationTime += Gdx.graphics.getDeltaTime();
        if (isFlipped) {
            batch.draw(currentState.animation.getKeyFrame(currentState.animationTime), x + playerWidthPixels, y,  -playerWidthPixels, playerHeightPixels);
        } else {
            batch.draw(currentState.animation.getKeyFrame(currentState.animationTime), x, y, playerWidthPixels, playerHeightPixels);
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

    private void checkIfFlipped() {
        if (xVel > 0) {
            isFlipped = false;
        }
        if (xVel < 0) {
            isFlipped = true;
        }
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x+6, y-(playerHeightPixels - Application.defaultTileHeight)+6, 10, 18);
    }
}
