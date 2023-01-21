package com.mikm.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mikm.Vector2Int;
import com.mikm.rendering.Camera;
import com.mikm.rendering.screens.Application;

public abstract class InanimateEntity {

    public float x, y;
    public float xVel, yVel;
    public float xScale = 1, yScale = 1;
    public float height;
    public InanimateEntity shadow;


    public InanimateEntity(float x, float y) {
        this.x = x;
        this.y = y;
    }

    void render(Batch batch) {
        draw(batch);
        update();
    }
    public void die() {
        Application.currentScreen.removeInanimateEntity(this);
    }

    public void drawHitboxes(Batch batch, Circle hitbox) {
        batch.end();
        Application.currentScreen.debugShapeRenderer.setAutoShapeType(true);
        Application.currentScreen.debugShapeRenderer.setProjectionMatrix(Camera.orthographicCamera.combined);
        Application.currentScreen.debugShapeRenderer.setColor(Color.BLUE);
        Application.currentScreen.debugShapeRenderer.begin();
        Application.currentScreen.debugShapeRenderer.circle(hitbox.x, hitbox.y, hitbox.radius);
        Application.currentScreen.debugShapeRenderer.end();
        batch.begin();
    }

    public abstract void update();

    public void moveAndCheckCollisions() {
        checkWallCollisionsX();
        x += xVel;
        checkWallCollisionsY();
        y += yVel;
    }

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
        return new Rectangle(x, y, getBounds().width, getBounds().height);
    }

    public Circle getHitbox() {
        return new Circle(getBounds().x+getBounds().width/2f, getBounds().y+getBounds().height/2f, getBounds().width/2f);
    }

    public void onWallCollision(boolean xCollision, Rectangle wallBounds) {
        if (xCollision) {
            if (xVel > 0) {
                x = wallBounds.x - getBounds().width + getBoundsOffset().x;
            } else if (xVel < 0) {
                x = wallBounds.x + wallBounds.width + getBoundsOffset().x;
            }
            xVel = 0;
        } else {
            if (yVel > 0) {
                y = wallBounds.y - getBounds().height + getBoundsOffset().y;
            } else if (yVel < 0) {
                y = wallBounds.y + wallBounds.height + getBoundsOffset().y;
            }
            yVel = 0;
        }
    }

    public boolean collided() {
        boolean first = checkWallCollisionsX();
        if (first) {
            return true;
        }
        return checkWallCollisionsY();
    }

    public boolean checkWallCollisionsX() {
        //Check tiles in a 5x5 grid around player
        boolean movedPlayer = false;
        boolean[][] isCollidable = Application.currentScreen.getIsCollidableGrid();
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                Vector2Int checkedWallTilePosition = new Vector2Int(getXInt() / Application.TILE_WIDTH + j, getYInt() / Application.TILE_HEIGHT + i);
                Vector2Int checkedWallPosition = new Vector2Int(checkedWallTilePosition.x * Application.TILE_WIDTH, checkedWallTilePosition.y * Application.TILE_HEIGHT);

                boolean isInBounds = checkedWallTilePosition.x > 0 && checkedWallTilePosition.x < isCollidable[0].length && checkedWallTilePosition.y > 0 && checkedWallTilePosition.y < isCollidable.length;
                if (!isInBounds || isCollidable[checkedWallTilePosition.y][checkedWallTilePosition.x]) {
                    boolean collided = checkIfCollidingWithWallX(checkedWallPosition);
                    if (collided) {
                        movedPlayer = true;
                    }
                }
            }
        }
        return movedPlayer;
    }

    public boolean checkWallCollisionsY() {
        //Check tiles in a 5x5 grid around player
        boolean movedPlayer = false;
        boolean[][] isCollidable = Application.currentScreen.getIsCollidableGrid();
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                Vector2Int checkedWallTilePosition = new Vector2Int(getXInt() / Application.TILE_WIDTH + j, getYInt() / Application.TILE_HEIGHT + i);
                Vector2Int checkedWallPosition = new Vector2Int(checkedWallTilePosition.x * Application.TILE_WIDTH, checkedWallTilePosition.y * Application.TILE_HEIGHT);

                boolean isInBounds = checkedWallTilePosition.x > 0 && checkedWallTilePosition.x < isCollidable[0].length && checkedWallTilePosition.y > 0 && checkedWallTilePosition.y < isCollidable.length;
                if (!isInBounds || isCollidable[checkedWallTilePosition.y][checkedWallTilePosition.x]) {
                    boolean collided = checkIfCollidingWithWallY(checkedWallPosition);
                    if (collided) {
                        movedPlayer = true;
                    }
                }
            }
        }
        return movedPlayer;
    }

    private boolean checkIfCollidingWithWallX(Vector2Int wallPosition) {
        boolean movedPlayer = false;
        Rectangle wallBounds = new Rectangle(wallPosition.x, wallPosition.y, Application.TILE_WIDTH, Application.TILE_HEIGHT);
        if (Intersector.overlaps(getOffsetBoundsH(), wallBounds)) {
            onWallCollision(true, wallBounds);
            movedPlayer = true;
        }
        return movedPlayer;
    }

    private boolean checkIfCollidingWithWallY(Vector2Int wallPosition) {
        boolean movedPlayer = false;
        Rectangle wallBounds = new Rectangle(wallPosition.x, wallPosition.y, Application.TILE_WIDTH, Application.TILE_HEIGHT);
        if (Intersector.overlaps(getOffsetBoundsV(), wallBounds)) {
            onWallCollision(false, wallBounds);
            movedPlayer = true;
        }
        return movedPlayer;
    }

    public Rectangle getShadowBounds() {
        return getBounds();
    }

    public Rectangle getOffsetBoundsH() {
        return new Rectangle(getBounds().x + xVel, getBounds().y, getBounds().width, getBounds().height);
    }

    public Rectangle getOffsetBoundsV() {
        return new Rectangle(getBounds().x, getBounds().y + yVel, getBounds().width, getBounds().height);
    }

    public Vector2 getBoundsOffset() {
        return new Vector2(x - getBounds().x, y-getBounds().y);
    }

    public boolean hasShadow() {
        return true;
    }

    public int getZOrder() {
        return 0;
    }
}
