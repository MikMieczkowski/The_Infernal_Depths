package com.mikm.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.mikm.Vector2Int;
import com.mikm.rendering.Camera;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.GameScreen;

public abstract class InanimateEntity {
    public GameScreen screen;
    public float x, y;
    public float xVel, yVel;
    public boolean hasShadow = true;
    public int shadowVerticalOffset = 0;

    public InanimateEntity(int x, int y) {
        this.x = x;
        this.y = y;
    }

    void render(Batch batch) {
        update();
        if (hasShadow) {
            batch.draw(GameScreen.shadowImage, getBounds().x, getBounds().y - 3 - shadowVerticalOffset);
        }
        draw(batch);
    }

    public void drawHitboxes(Batch batch, Circle hitbox) {
        batch.end();
        screen.debugShapeRenderer.setAutoShapeType(true);
        screen.debugShapeRenderer.setProjectionMatrix(Camera.orthographicCamera.combined);
        screen.debugShapeRenderer.setColor(Color.BLUE);
        screen.debugShapeRenderer.begin();
        screen.debugShapeRenderer.circle(hitbox.x, hitbox.y, hitbox.radius);
        screen.debugShapeRenderer.end();
        batch.begin();
    }

    public abstract void update();

    public abstract void draw(Batch batch);

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

    public Circle getHitbox() {
        return new Circle(getBounds().x+getBounds().width/2f, getBounds().y+getBounds().height/2f, getBounds().width/2f);
    }

    public abstract void onWallCollision(Vector2Int wallPosition);

    public void checkWallCollisions() {
        //Check tiles in a 5x5 grid around player
        boolean[][] isCollidable = screen.getCollidableTilePositions();
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                Vector2Int checkedWallTilePosition = new Vector2Int(getXInt() / Application.TILE_WIDTH + j, getYInt() / Application.TILE_HEIGHT + i);
                Vector2Int checkedWallPosition = new Vector2Int(checkedWallTilePosition.x * Application.TILE_WIDTH, checkedWallTilePosition.y * Application.TILE_HEIGHT);

                boolean isInBounds = checkedWallTilePosition.x > 0 && checkedWallTilePosition.x < isCollidable[0].length && checkedWallTilePosition.y > 0 && checkedWallTilePosition.y < isCollidable.length;
                if (!isInBounds || isCollidable[checkedWallTilePosition.y][checkedWallTilePosition.x]) {
                    onWallCollision(checkedWallPosition);
                }
            }
        }
    }
}
