package com.mikm.entities.collision;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mikm.Vector2Int;
import com.mikm.entities.InanimateEntity;
import com.mikm.rendering.screens.Application;

import java.util.ArrayList;
import java.util.Comparator;

public class Collider {
    Vector2 nearPercentages, farPercentages;
    float tNear;
    Vector2 nearIntersectionNormal, nearIntersection;

    private final InanimateEntity inanimateEntity;

    public Collider(InanimateEntity inanimateEntity) {
        this.inanimateEntity = inanimateEntity;
    }

    public void updateCollisions() {
        boolean[][] collidableMap = Application.currentScreen.getIsCollidableGrid();

        ArrayList<Collision> collisions = new ArrayList<>();
        for (int y = -2; y <= 2; y++) {
            for (int x = -2; x <= 2; x++) {
                Vector2Int checkedWallTilePosition = new Vector2Int(inanimateEntity.getXInt() / Application.TILE_WIDTH + x, inanimateEntity.getYInt() / Application.TILE_HEIGHT + y);
                boolean isInBounds = checkedWallTilePosition.x > 0 && checkedWallTilePosition.x < collidableMap.length && checkedWallTilePosition.y > 0 && checkedWallTilePosition.y < collidableMap[0].length;

                if (isInBounds && !collidableMap[checkedWallTilePosition.y][checkedWallTilePosition.x]) {
                    continue;
                }

                Vector2Int checkedWallPosition = new Vector2Int(checkedWallTilePosition.x * Application.TILE_WIDTH, checkedWallTilePosition.y * Application.TILE_HEIGHT);
                Rectangle wall = new Rectangle(checkedWallPosition.x, checkedWallPosition.y, Application.TILE_WIDTH, Application.TILE_HEIGHT);
                if (collidedWith(wall)) {
                    collisions.add(new Collision(tNear, wall));
                }
            }
        }
        collisions.sort(Comparator.comparing(Collision::getTNear));
        for (Collision collision : collisions) {
            if (collidedWith(collision.wall)) {
                inanimateEntity.onWallCollision();
                inanimateEntity.xVel += nearIntersectionNormal.x * Math.abs(inanimateEntity.xVel) * (1 - tNear);
                inanimateEntity.yVel += nearIntersectionNormal.y * Math.abs(inanimateEntity.yVel) * (1 - tNear);
            }
        }
    }

    public boolean inWall() {
        Vector2Int tilePosition = new Vector2Int(inanimateEntity.getXInt() / Application.TILE_WIDTH, inanimateEntity.getYInt() / Application.TILE_HEIGHT);
        boolean[][] collidableMap = Application.currentScreen.getIsCollidableGrid();
        boolean isInBounds = tilePosition.x > 0 && tilePosition.x < collidableMap.length && tilePosition.y > 0 && tilePosition.y < collidableMap[0].length;
        if (!isInBounds) {
            return true;
        }
        return collidableMap[tilePosition.y][tilePosition.x];
    }

    private boolean collidedWith(Rectangle wallBounds) {
        if (inanimateEntity.xVel == 0 && inanimateEntity.yVel == 0)
            return false;

        // Expand target rectangle by source dimensions
        Rectangle bounds = inanimateEntity.getBounds();
        Rectangle expandedWallBounds = new Rectangle(wallBounds.x - bounds.width / 2,
                wallBounds.y - bounds.height / 2,
                wallBounds.width + bounds.width,
                wallBounds.height + bounds.height);

        Ray ray = new Ray(bounds.x + bounds.width/2, bounds.y + bounds.height/2,
                bounds.x + bounds.width/2 + inanimateEntity.xVel, bounds.y + bounds.height/2 + inanimateEntity.yVel);
        if (rayIntersectsRectangle(ray, expandedWallBounds)) {
            return (tNear >= 0.0f && tNear < 1.0f);
        }
        return false;
    }

    public boolean rayIntersectsRectangle(Ray ray, Rectangle rectangle) {
//        nearPercentages = new Vector2();
//        farPercentages = new Vector2();
//        tNear = 0;
//        contactNormal = new Vector2();

        final float cachedDivisionByPQVectorX = 1f/ray.pqVector.x;
        final float cachedDivisionByPQVectorY = 1f/ray.pqVector.y;
        findPercentagesOfDistanceAlongVectorToRectangle(ray, rectangle, cachedDivisionByPQVectorX, cachedDivisionByPQVectorY);

        if (Float.isNaN(farPercentages.y) || Float.isNaN(farPercentages.x))  {
            return false;
        }
        if (Float.isNaN(nearPercentages.y) || Float.isNaN(nearPercentages.x)) {
            return false;
        }

        checkIfNearGreaterThanFar();

        if (nearPercentages.x > farPercentages.y || nearPercentages.y > farPercentages.x) {
            return false;
        }

        tNear = Math.max(nearPercentages.x, nearPercentages.y);
        float tFar = Math.min(farPercentages.x, farPercentages.y);

        if (tFar < 0) {
            return false;
        }

        nearIntersection = ray.getPointFromTValue(tNear);

        findContactNormal(cachedDivisionByPQVectorX, cachedDivisionByPQVectorY);
        return true;
    }

    private void findPercentagesOfDistanceAlongVectorToRectangle(Ray ray, Rectangle rectangle, float cachedDivisionByPQVectorX, float cachedDivisionByPQVectorY) {
        float xPercentageNear = (rectangle.x - ray.startPoint.x) * cachedDivisionByPQVectorX;
        float yPercentageNear = (rectangle.y + rectangle.height - ray.startPoint.y) * cachedDivisionByPQVectorY;
        nearPercentages = new Vector2(xPercentageNear, yPercentageNear);

        float xPercentageFar = (rectangle.x + rectangle.width - ray.startPoint.x) * cachedDivisionByPQVectorX;
        float yPercentageFar = (rectangle.y - ray.startPoint.y) * cachedDivisionByPQVectorY;
        farPercentages = new Vector2(xPercentageFar, yPercentageFar);
    }

    private void checkIfNearGreaterThanFar() {
        if (nearPercentages.x > farPercentages.x) {
            swapIntersectionXValues();
        }
        if (nearPercentages.y > farPercentages.y) {
            swapIntersectionYValues();
        }
    }

    private void swapIntersectionXValues() {
        float temp = nearPercentages.x;
        nearPercentages.x = farPercentages.x;
        farPercentages.x = temp;
    }

    private void swapIntersectionYValues() {
        float temp = nearPercentages.y;
        nearPercentages.y = farPercentages.y;
        farPercentages.y = temp;
    }

    private void findContactNormal(float cachedDivisionByPQVectorX, float cachedDivisionByPQVectorY) {
        if (nearPercentages.x > nearPercentages.y) {
            if (cachedDivisionByPQVectorX < 0) {
                nearIntersectionNormal = new Vector2(1, 0);
            } else {
                nearIntersectionNormal = new Vector2(-1, 0);
            }
        } else if (nearPercentages.x < nearPercentages.y) {
            if (cachedDivisionByPQVectorY < 0) {
                nearIntersectionNormal = new Vector2(0, 1);
            } else {
                nearIntersectionNormal = new Vector2(0, -1);
            }
        } else {
            nearIntersectionNormal = Vector2.Zero;
        }
    }
}
