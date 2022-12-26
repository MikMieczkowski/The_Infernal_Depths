package com.mikm.entities;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.mikm.Vector2Int;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.GameScreen;

public abstract class Entity extends Actor {
    public GameScreen screen;
    public float x, y;
    public float xVel, yVel;

    @Override
    public void draw(Batch batch, float alpha) {
        update();
        render(batch);
    }

    public abstract void update();

    public abstract void render(Batch batch);

    public int getXInt() {
        return (int)x;
    }

    public int getYInt() {
        return (int)y;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, Application.defaultTileWidth, Application.defaultTileHeight);
    }

    public Rectangle getOffsetBoundsH() {
        return new Rectangle(getBounds().x + xVel, getBounds().y, getBounds().width, getBounds().height);
    }

    public Rectangle getOffsetBoundsV() {
        return new Rectangle(getBounds().x, getBounds().y + yVel, getBounds().width, getBounds().height);
    }

    public void checkWallCollisions() {
        TiledMapTileLayer[] collideableLayers = new TiledMapTileLayer[screen.getCollidableTiledMapTileLayerIDs().length];
        for (int i = 0; i < screen.getCollidableTiledMapTileLayerIDs().length; i++) {
            collideableLayers[i] = (TiledMapTileLayer) screen.tiledMap.getLayers().get(screen.getCollidableTiledMapTileLayerIDs()[i]);
        }

        //Check tiles in a 5x5 grid around player
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                for (TiledMapTileLayer collideableLayer : collideableLayers){
                    Vector2Int wallPositionInTiles = new Vector2Int(getXInt() / Application.defaultTileWidth + i, getYInt() / Application.defaultTileHeight + j);
                    TiledMapTileLayer.Cell wall = collideableLayer.getCell(wallPositionInTiles.x, wallPositionInTiles.y);
                    Vector2Int wallPosition = new Vector2Int(wallPositionInTiles.x * Application.defaultTileWidth, wallPositionInTiles.y * Application.defaultTileHeight);
                    if (wall != null) {
                        setPositionBasedOnWallIntersection(wallPosition);
                    }
                }
            }
        }


    }

    private void setPositionBasedOnWallIntersection(Vector2Int cellPosition) {
        Rectangle wallBounds = new Rectangle(cellPosition.x, cellPosition.y, Application.defaultTileWidth, Application.defaultTileHeight);
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
