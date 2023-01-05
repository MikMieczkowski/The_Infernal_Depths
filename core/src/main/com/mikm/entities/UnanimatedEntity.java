package com.mikm.entities;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.mikm.Vector2Int;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.GameScreen;

public abstract class UnanimatedEntity extends Actor{
    public GameScreen screen;
    public float x, y;

    public UnanimatedEntity(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setScreen(GameScreen screen) {
        this.screen = screen;
        screen.stage.addActor(this);
    }

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
        return new Rectangle(x, y, Application.TILE_WIDTH, Application.TILE_HEIGHT);
    }

    public Rectangle getFullBounds() {
        return new Rectangle(x, y, Application.TILE_WIDTH, Application.TILE_HEIGHT);
    }

    public abstract void onWallCollision(Vector2Int wallPosition);

    public void checkWallCollisions() {
        TiledMapTileLayer[] collideableLayers = new TiledMapTileLayer[screen.getCollidableTiledMapTileLayerIDs().length];
        for (int i = 0; i < screen.getCollidableTiledMapTileLayerIDs().length; i++) {
            collideableLayers[i] = (TiledMapTileLayer) screen.tiledMap.getLayers().get(screen.getCollidableTiledMapTileLayerIDs()[i]);
        }

        //Check tiles in a 5x5 grid around player
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                for (TiledMapTileLayer collideableLayer : collideableLayers){
                    Vector2Int wallPositionInTiles = new Vector2Int(getXInt() / Application.TILE_WIDTH + i, getYInt() / Application.TILE_HEIGHT + j);
                    TiledMapTileLayer.Cell wall = collideableLayer.getCell(wallPositionInTiles.x, wallPositionInTiles.y);
                    Vector2Int wallPosition = new Vector2Int(wallPositionInTiles.x * Application.TILE_WIDTH, wallPositionInTiles.y * Application.TILE_HEIGHT);
                    if (wall != null) {
                        onWallCollision(wallPosition);
                    }
                }
            }
        }
    }
}
