package com.mikm.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
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

    private float squishTime;
    private float squishAmount;
    private boolean squishing;
    private final float squishSpeed = .5f;
    private float preSquishTimer;
    private boolean triggerSquish = false;
    private float squishDelay;

    public Entity(int x, int y, ArrayList<TextureRegion[]> spritesheets) {
        this.x = x;
        this.y = y;
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

    public void setScreen(GameScreen screen) {
        this.screen = screen;
        screen.stage.addActor(this);
    }

    @Override
    public void render(Batch batch) {
        currentState.animationManager.draw(batch);
    }

    public abstract void createStates();

    public Rectangle getOffsetBoundsH() {
        return new Rectangle(getBounds().x + xVel, getBounds().y, getBounds().width, getBounds().height);
    }

    public Rectangle getOffsetBoundsV() {
        return new Rectangle(getBounds().x, getBounds().y + yVel, getBounds().width, getBounds().height);
    }

    @Override
    public void onWallCollision(Vector2Int wallPosition) {
        setPositionBasedOnWallIntersection(wallPosition);
    }

    private void setPositionBasedOnWallIntersection(Vector2Int wallPosition) {
        Rectangle wallBounds = new Rectangle(wallPosition.x, wallPosition.y, Application.defaultTileWidth, Application.defaultTileHeight);
        if (Intersector.overlaps(getOffsetBoundsH(), wallBounds)) {
            setXPositionToWall(wallBounds);
        }
        if (Intersector.overlaps(getOffsetBoundsV(), wallBounds)) {
            setYPositionToWall(wallBounds);
        }
    }

    private void setXPositionToWall(Rectangle wallBounds) {
        if (xVel > 0) {
            x = wallBounds.x - getBounds().width + (x - getBounds().x);
        } else if (xVel < 0) {
            x = wallBounds.x + wallBounds.width + (x - getBounds().x);
        }
        xVel = 0;
    }

    private void setYPositionToWall(Rectangle wallBounds) {
        if (yVel > 0) {
            y = wallBounds.y - getBounds().height + (y - getBounds().y);
        } else if (yVel < 0) {
            y = wallBounds.y + wallBounds.height + (y - getBounds().y);
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
                squishTime += squishSpeed;
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

    public abstract int getMaxHp();
}
