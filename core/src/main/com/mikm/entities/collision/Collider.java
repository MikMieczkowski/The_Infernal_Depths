package com.mikm.entities.collision;

import com.badlogic.gdx.math.Vector2;
import com.mikm.DeltaTime;
import com.mikm.ExtraMathUtils;
import com.mikm.Vector2Int;
import com.mikm.entities.InanimateEntity;
import com.mikm.rendering.cave.CaveTilemapCreator;
import com.mikm.rendering.screens.Application;

import java.util.ArrayList;

public class Collider {
    private InanimateEntity inanimateEntity;
    private Vector2 nextPosition;
    private Vector2Int tilePosition = new Vector2Int();
    private Vector2Int nextTilePosition = new Vector2Int();

    public Collider(InanimateEntity inanimateEntity) {
        this.inanimateEntity = inanimateEntity;
    }

    public void updateCollisions() {
        boolean[][] collidableMap = Application.getInstance().currentScreen.isCollidableGrid();

        tilePosition = ExtraMathUtils.toTileCoordinates(inanimateEntity.getHitbox().x, inanimateEntity.getHitbox().y);
        nextPosition = new Vector2(inanimateEntity.getHitbox().x + inanimateEntity.xVel * DeltaTime.deltaTime(), inanimateEntity.getHitbox().y + inanimateEntity.yVel * DeltaTime.deltaTime());
        nextTilePosition = ExtraMathUtils.toTileCoordinates(nextPosition);

        ArrayList<Vector2Int> tilePositionsToCheck = getWallTilePositionsToCheck();
        for (Vector2Int v : tilePositionsToCheck) {
            boolean vInMap = false;
            try {
                vInMap = collidableMap[v.y][v.x];
            } catch (Exception e) {

            }
            if (isOutOfBounds(v) || vInMap) {
                Vector2 nearestPoint = new Vector2(
                        ExtraMathUtils.clamp(nextPosition.x, v.x * Application.TILE_WIDTH, (v.x+1) * Application.TILE_WIDTH),
                        ExtraMathUtils.clamp(nextPosition.y, v.y * Application.TILE_HEIGHT, (v.y+1) * Application.TILE_HEIGHT)
                );

                Vector2 vectorToNearestPoint = nearestPoint.sub(nextPosition);
                float overlapDistance = inanimateEntity.getHitbox().radius - vectorToNearestPoint.len() * (inanimateEntity.getHitbox().radius/7f);

                if (Float.isNaN(overlapDistance)) {
                    throw new RuntimeException("overlap is NaN");
                }

                if (overlapDistance > 0) {
                    Vector2 direction = vectorToNearestPoint.nor();
                    inanimateEntity.onWallCollision();
                    inanimateEntity.xVel -= direction.x * overlapDistance;
                    inanimateEntity.yVel -= direction.y * overlapDistance;
                }

            }
        }
    }

    public ArrayList<Vector2Int> getWallTilePositionsToCheck() {
        ArrayList<Vector2Int> output = new ArrayList<>();

        Vector2Int topLeftTilePosition = getTopLeftTilePosition();
        Vector2Int bottomRightTilePosition = getBottomRightTilePosition();

        for (int y = topLeftTilePosition.y; y <= bottomRightTilePosition.y; y++) {
            for (int x = topLeftTilePosition.x; x <= bottomRightTilePosition.x; x++) {
                output.add(new Vector2Int(x, y));
            }
        }

        return output;
    }

    private boolean isOutOfBounds(Vector2Int v) {
        boolean[][] collidableMap = Application.getInstance().currentScreen.isCollidableGrid();
        return v.x <= 0 || v.x >= collidableMap.length || v.y <= 0 || v.y >= collidableMap[0].length;
    }

    public boolean inWall() {
        boolean[][] collidableMap = Application.getInstance().currentScreen.isCollidableGrid();
        tilePosition = ExtraMathUtils.toTileCoordinates(inanimateEntity.getHitbox().x, inanimateEntity.getHitbox().y);
        if (isOutOfBounds(tilePosition)) {
            return true;
        }
        return collidableMap[tilePosition.y][tilePosition.x];
    }

    private Vector2Int getTopLeftTilePosition() {
        Vector2Int output = ExtraMathUtils.minComponents(tilePosition, nextTilePosition);
        //increase checking area by 1 tile around
        output = new Vector2Int(output.x - 1, output.y - 1);
        //Clamp to world boundaries
        output = ExtraMathUtils.maxComponents(Vector2Int.ZERO, output);
        return output;
    }

    private Vector2Int getBottomRightTilePosition() {
        Vector2Int output = ExtraMathUtils.maxComponents(tilePosition, nextTilePosition);
        //increase checking area by 1 tile around
        output = new Vector2Int(output.x + 1, output.y + 1);
        //Clamp to world boundaries
        output = ExtraMathUtils.minComponents(new Vector2Int(CaveTilemapCreator.MAP_WIDTH-1, CaveTilemapCreator.MAP_HEIGHT-1), output);
        return output;
    }
}
