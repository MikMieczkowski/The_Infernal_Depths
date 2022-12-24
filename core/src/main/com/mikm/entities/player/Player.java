package com.mikm.entities.player;


import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.mikm.entities.Entity;
import com.mikm.rendering.screens.GameScreen;

public class Player extends Entity {
    public TextureRegion img;
    public final float speed = 2;

    public Group group;
    private PlayerHeldItem playerHeldItem;
    private PlayerBackItem playerBackItem;

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
        handleInput();
        checkWallCollisions();
        x += xVel;
        y += yVel;
    }

    @Override
    public void render(Batch batch) {
        batch.draw(img, x, y);
    }

    private void handleInput() {
        xVel = InputAxis.getHorizontalAxis() * speed * InputAxis.movementVectorNormalizationMultiplier();
        yVel = InputAxis.getVerticalAxis() * speed * InputAxis.movementVectorNormalizationMultiplier();
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
