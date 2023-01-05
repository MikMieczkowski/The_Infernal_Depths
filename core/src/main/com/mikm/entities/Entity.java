package com.mikm.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mikm.Vector2Int;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.GameScreen;

import java.util.ArrayList;


public abstract class Entity extends UnanimatedEntity {

    public float originX, originY;
    public float rotation;
    public float xScale = 1, yScale = 1;
    public float xVel, yVel;
    public float height;
    public Vector2Int direction = Vector2Int.DOWN;

    public int hp;
    public float speed;

    public State walkingState;
    public State standingState;
    public State currentState;
    public ArrayList<TextureRegion[]> spritesheets;

    private final float SQUISH_SPEED = .5f;
    private float squishTime;
    private float squishAmount;
    private boolean squishing;
    private float preSquishTimer;
    private boolean triggerSquish = false;
    private float squishDelay;

    private final int MAX_FLASH_TIME = 2;
    private boolean shouldFlash;
    private int flashTimerFrames;
    private Color flashColor;

    public Entity(int x, int y, ArrayList<TextureRegion[]> spritesheets) {
        super(x,y);
        this.spritesheets = spritesheets;
        createStates();
    }

    @Override
    public void update() {
        currentState.update();
        currentState.checkForStateTransition();
        checkWallCollisions();
        x += xVel;
        y += yVel;
    }

    @Override
    public void render(Batch batch) {
        handleFlash(batch);
        currentState.animationManager.draw(batch);
    }

    private void handleFlash(Batch batch) {
        if (shouldFlash) {
            flashTimerFrames++;
            if (flashTimerFrames >= MAX_FLASH_TIME) {
                Application.setFillColorShader(batch, flashColor);
                shouldFlash = false;
                flashTimerFrames = 0;
            }
        } else {
            batch.setShader(null);
        }
    }

    public abstract void createStates();

    public Rectangle getOffsetBoundsH() {
        return new Rectangle(getBounds().x + xVel, getBounds().y, getBounds().width, getBounds().height);
    }

    public Rectangle getOffsetBoundsV() {
        return new Rectangle(getBounds().x, getBounds().y + yVel, getBounds().width, getBounds().height);
    }

    public Vector2 getBoundsOffset() {
        return new Vector2(x - getBounds().x, y-getBounds().y);
    }

    @Override
    public void onWallCollision(Vector2Int wallPosition) {
        setPositionBasedOnWallIntersection(wallPosition);
    }

    private void setPositionBasedOnWallIntersection(Vector2Int wallPosition) {
        Rectangle wallBounds = new Rectangle(wallPosition.x, wallPosition.y, Application.TILE_WIDTH, Application.TILE_HEIGHT);
        if (Intersector.overlaps(getOffsetBoundsH(), wallBounds)) {
            setXPositionToWall(wallBounds);
        }
        if (Intersector.overlaps(getOffsetBoundsV(), wallBounds)) {
            setYPositionToWall(wallBounds);
        }
    }

    private void setXPositionToWall(Rectangle wallBounds) {
        if (xVel > 0) {
            x = wallBounds.x - getBounds().width + getBoundsOffset().x;
        } else if (xVel < 0) {
            x = wallBounds.x + wallBounds.width + getBoundsOffset().x;
        }
        xVel = 0;
    }

    private void setYPositionToWall(Rectangle wallBounds) {
        if (yVel > 0) {
            y = wallBounds.y - getBounds().height + getBoundsOffset().y;
        } else if (yVel < 0) {
            y = wallBounds.y + wallBounds.height + getBoundsOffset().y;
        }
        yVel = 0;
    }

    public void updateSquish() {
        if (triggerSquish) {
            if (preSquishTimer > squishDelay) {
                preSquishTimer = 0;
                triggerSquish = false;
                squishing = true;
            } else {
                preSquishTimer += Gdx.graphics.getDeltaTime();
            }
        }
        if (squishing) {
            xScale = MathUtils.lerp(1, squishAmount, squishTime);
            yScale = MathUtils.lerp(1, 1 / squishAmount, squishTime);
            if (squishTime < 1) {
                squishTime += SQUISH_SPEED;
            } else {
                squishTime = 0;
                squishing = false;
            }
        } else {
            xScale = MathUtils.lerp(xScale, 1, .5f);
            yScale = MathUtils.lerp(yScale, 1, .5f);
        }
    }

    public void startSquish(float squishDelay, float squishAmount) {
        triggerSquish = true;
        this.squishDelay = squishDelay;
        this.squishAmount = squishAmount;
    }

    public void flash(Color color) {
        shouldFlash = true;
        flashColor = color;
    }

    public abstract int getMaxHp();
}
