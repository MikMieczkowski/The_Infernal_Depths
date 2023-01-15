package com.mikm.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.*;
import com.mikm.Vector2Int;
import com.mikm.rendering.Camera;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.GameScreen;

public abstract class InanimateEntity {

    public float x, y;
    public float xVel, yVel;
    public float xScale = 1, yScale = 1;
    public float height;

    private float shadowScale = .75f;
    private final float SHADOW_DISAPPEAR_HEIGHT_FOR_NORMAL_ENTITY = 20;

    public InanimateEntity(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void render(Batch batch) {
        update();
        if (hasShadow()) {
            final float shadowHeightScale = Math.min(shadowScale / (getShadowBounds().width/Application.TILE_WIDTH) * height/SHADOW_DISAPPEAR_HEIGHT_FOR_NORMAL_ENTITY, shadowScale);
            batch.draw(GameScreen.shadowImage, getShadowBounds().x, getShadowBounds().y - 3,getShadowBounds().width/2f, 4, getShadowBounds().width, getShadowBounds().height,
                    xScale * (shadowScale - shadowHeightScale), yScale * (shadowScale - shadowHeightScale), 0);
        }
        draw(batch);
    }
    public void die() {
        Application.currentScreen.inanimateEntities.remove(this);
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

    public boolean checkWallCollisions() {
        //Check tiles in a 5x5 grid around player
        boolean movedPlayer = false;
        boolean[][] isCollidable = Application.currentScreen.getIsCollidableGrid();
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                Vector2Int checkedWallTilePosition = new Vector2Int(getXInt() / Application.TILE_WIDTH + j, getYInt() / Application.TILE_HEIGHT + i);
                Vector2Int checkedWallPosition = new Vector2Int(checkedWallTilePosition.x * Application.TILE_WIDTH, checkedWallTilePosition.y * Application.TILE_HEIGHT);

                boolean isInBounds = checkedWallTilePosition.x > 0 && checkedWallTilePosition.x < isCollidable[0].length && checkedWallTilePosition.y > 0 && checkedWallTilePosition.y < isCollidable.length;
                if (!isInBounds || isCollidable[checkedWallTilePosition.y][checkedWallTilePosition.x]) {
                    boolean collided = checkIfCollidingWithWall(checkedWallPosition);
                    if (collided) {
                        movedPlayer = true;
                    }
                }
            }
        }
        return movedPlayer;
    }

    private boolean checkIfCollidingWithWall(Vector2Int wallPosition) {
        boolean movedPlayer = false;
        Rectangle wallBounds = new Rectangle(wallPosition.x, wallPosition.y, Application.TILE_WIDTH, Application.TILE_HEIGHT);
        if (Intersector.overlaps(getOffsetBoundsH(), wallBounds)) {
            onWallCollision(true, wallBounds);
            movedPlayer = true;
        }
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
}
