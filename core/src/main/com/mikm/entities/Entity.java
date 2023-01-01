package com.mikm.entities;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.mikm.Vector2Int;
import com.mikm.entities.states.State;
import com.mikm.rendering.screens.Application;

import java.util.ArrayList;

public abstract class Entity extends Image {


    public float xVel, yVel;
    public float height;
    public Vector2Int direction = Vector2Int.DOWN;

    public State currentState;

    public Entity(int x, int y) {
        this.x = x;
        this.y = y;
    }

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
}
