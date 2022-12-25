package com.mikm.entities.player;


import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.mikm.entities.Entity;
import com.mikm.entities.player.states.DivingState;
import com.mikm.entities.player.states.State;
import com.mikm.entities.player.states.WalkingState;
import com.mikm.rendering.screens.GameScreen;

public class Player extends Entity {
    public TextureRegion img;
    public final float speed = 2;

    public final float diveSpeed = 6;
    public final float diveFriction = .3f;
    public final float diveFrictionSpeed = .317f;
    public final float diveStartingSinCount = 1;
    public final float diveEndTimeFrame = .3f;

    public Group group;
    private PlayerHeldItem playerHeldItem;
    private PlayerBackItem playerBackItem;

    public WalkingState walkingState = new WalkingState(this);
    public DivingState divingState = new DivingState(this);
    public State currentState = walkingState;

    public Player(int x, int y, TextureRegion textureRegion) {
        this.x = x;
        this.y = y;
        img = textureRegion;

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
        x += xVel;
        y += yVel;
    }

    @Override
    public void render(Batch batch) {
        batch.draw(img, x, y);
    }

    private void handleInput() {

    }

    private void createGroup() {
        group = new Group();
        playerBackItem = new PlayerBackItem();
        playerHeldItem = new PlayerHeldItem();
        group.addActor(this);
        group.addActor(playerBackItem);
        group.addActor(playerHeldItem);
    }
}
