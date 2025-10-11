package com.mikm.entities.collision;

import com.badlogic.gdx.math.Vector2;
import com.mikm.DeltaTime;
import com.mikm.ExtraMathUtils;
import com.mikm.Vector2Int;
import com.mikm.entities.inanimateEntities.InanimateEntity;
import com.mikm.rendering.cave.CaveTilemapCreator;
import com.mikm.rendering.screens.Application;

import java.util.ArrayList;

public class Collider {
    private InanimateEntity inanimateEntity;
    private Vector2 nextPosition;
    private Vector2Int tilePosition = new Vector2Int();
    private Vector2Int nextTilePosition = new Vector2Int();
    public boolean isBat = false;

    public Collider(InanimateEntity inanimateEntity) {
        this.inanimateEntity = inanimateEntity;
    }

    public void updateCollisions() {
        updateCollisions(DeltaTime.deltaTime());
    }

    public void updateCollisions(float dt) {
        boolean[][] collidableMap = Application.getInstance().currentScreen.isCollidableGrid();
        boolean[][] rockCollidableMap = Application.getInstance().caveScreen.caveTilemapCreator.rockCollidablePositions;

        tilePosition = ExtraMathUtils.toTileCoordinates(inanimateEntity.getHitbox().x, inanimateEntity.getHitbox().y);
        nextPosition = new Vector2(inanimateEntity.getHitbox().x + inanimateEntity.xVel * dt, inanimateEntity.getHitbox().y + inanimateEntity.yVel * dt);
        nextTilePosition = ExtraMathUtils.toTileCoordinates(nextPosition);

        ArrayList<Vector2Int> tilePositionsToCheck = getWallTilePositionsToCheck();
        for (Vector2Int v : tilePositionsToCheck) {
            boolean vInMap = false;
            try {
                vInMap = collidableMap[v.y][v.x];
                if (isBat) {
                    if (rockCollidableMap[v.y][v.x]) {
                        vInMap = false;
                    }
                }
            } catch (Exception e) {

            }
            if (isOutOfBounds(v) || vInMap) {
                Vector2 nearestPoint = new Vector2(
                        ExtraMathUtils.clamp(nextPosition.x, v.x * Application.TILE_WIDTH, (v.x + 1) * Application.TILE_WIDTH),
                        ExtraMathUtils.clamp(nextPosition.y, v.y * Application.TILE_HEIGHT, (v.y + 1) * Application.TILE_HEIGHT)
                );

                Vector2 vectorToNearestPoint = nearestPoint.sub(nextPosition);
                float overlapDistance = inanimateEntity.getHitbox().radius - vectorToNearestPoint.len() * (inanimateEntity.getHitbox().radius / 7f);

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

	public void moveWithCollisions(float dt) {
		float speed = (float)Math.sqrt(inanimateEntity.xVel * inanimateEntity.xVel + inanimateEntity.yVel * inanimateEntity.yVel);
		float maxStep = Application.TILE_WIDTH * 0.25f;
		int steps = 1;
		if (speed * dt > maxStep && speed > 0f) {
			steps = Math.min(8, (int)Math.ceil((speed * dt) / maxStep));
		}
		float subDt = dt / steps;
		for (int i = 0; i < steps; i++) {
			updateCollisions(subDt);
			if (subDt < 3) {
				inanimateEntity.x += inanimateEntity.xVel * subDt;
				inanimateEntity.y += inanimateEntity.yVel * subDt;
			}
		}
	}

	public void ejectFromWalls() {
		if (!inWall()) {
			return;
		}
		boolean[][] collidableMap = Application.getInstance().currentScreen.isCollidableGrid();
		boolean[][] rockCollidableMap = Application.getInstance().caveScreen.caveTilemapCreator.rockCollidablePositions;
		Vector2 center = new Vector2(inanimateEntity.getHitbox().x, inanimateEntity.getHitbox().y);
		Vector2Int start = ExtraMathUtils.toTileCoordinates(center.x, center.y);
		int maxRadius = 12;
		Vector2Int best = null;
		float bestDist2 = Float.MAX_VALUE;
		for (int r = 0; r <= maxRadius; r++) {
			for (int y = start.y - r; y <= start.y + r; y++) {
				for (int x = start.x - r; x <= start.x + r; x++) {
					if (x < 0 || y < 0 || y >= collidableMap.length || x >= collidableMap[0].length) continue;
					boolean blocked = collidableMap[y][x];
					if (isBat && rockCollidableMap[y][x]) blocked = false;
					if (!blocked) {
						float cx = x * Application.TILE_WIDTH + Application.TILE_WIDTH / 2f;
						float cy = y * Application.TILE_HEIGHT + Application.TILE_HEIGHT / 2f;
						float dx = cx - center.x;
						float dy = cy - center.y;
						float d2 = dx*dx + dy*dy;
						if (d2 < bestDist2) {
							bestDist2 = d2;
							best = new Vector2Int(x, y);
						}
					}
				}
			}
			if (best != null) break;
		}
		if (best != null) {
			// Place entity so that hitbox.x - radius and hitbox.y - radius equal the free tile's bottom-left
			inanimateEntity.x = best.x * Application.TILE_WIDTH;
			inanimateEntity.y = best.y * Application.TILE_HEIGHT;
			inanimateEntity.xVel = 0f;
			inanimateEntity.yVel = 0f;
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
        return v.x < 0 || v.x >= collidableMap[0].length || v.y < 0 || v.y >= collidableMap.length;
    }

    public boolean inWall() {
        boolean[][] collidableMap = Application.getInstance().currentScreen.isCollidableGrid();
        tilePosition = ExtraMathUtils.toTileCoordinates(inanimateEntity.getHitbox().x, inanimateEntity.getHitbox().y);
        if (isOutOfBounds(tilePosition)) {
            return true;
        }
        boolean output = collidableMap[tilePosition.y][tilePosition.x];
        if (isBat) {
            boolean[][] rockCollidableMap = Application.getInstance().caveScreen.caveTilemapCreator.rockCollidablePositions;
            if (rockCollidableMap[tilePosition.y][tilePosition.x]) {
                output = false;
            }
        }
        return output;
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
