package com.mikm.entities.player;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.mikm.InputAxis;
import com.mikm.entities.Entity;

public class Player extends Entity {
    private TextureRegion img;
    public float xVel, yVel;
    public final float speed = 5;

    public Group group;
    private PlayerHeldItem playerHeldItem;
    private PlayerBackItem playerBackItem;

    public Player(int x, int y, TextureRegion textureRegion) {
        this.x = x;
        this.y = y;
        img = textureRegion;

        createGroup();
    }

    @Override
    public void tick() {
        handleInput();
        x += xVel;
        y += yVel;
    }

    @Override
    public void render(Batch batch) {
        batch.draw(img, x, y, 64, 64);
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
