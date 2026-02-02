package com.mikm._systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mikm.utils.DeltaTime;
import com.mikm.utils.ExtraMathUtils;
import com.mikm.Vector2Int;
import com.mikm._components.WorldColliderComponent;
import com.mikm._components.Transform;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm.rendering.screens.Application;

import java.util.ArrayList;

//only if Application !timestop and !paused
public class WorldCollisionMovementSystem extends IteratingSystem {
    private final boolean NO_CLIP = false;

    public WorldCollisionMovementSystem() {
        super(Family.all(WorldColliderComponent.class, Transform.class).get());
    }

    protected void processEntity(Entity entity, float deltaTime) {
        if (!Application.getInstance().systemShouldTick()) {
            return;
        }
        if (NO_CLIP) {
            Transform transform = Transform.MAPPER.get(entity);
            transform.x += transform.xVel * DeltaTime.deltaTimeMultiplier();
            transform.y += transform.yVel * DeltaTime.deltaTimeMultiplier();
            updateDirection(Transform.MAPPER.get(entity));
            return;
        }
        moveWithCollisions(entity);
        if (inWall(entity)) {
            ejectFromWalls(entity);
        }
        updateDirection(Transform.MAPPER.get(entity));
    }

    private void updateDirection(Transform transform) {
        if (!(transform.xVel == 0 && transform.yVel == 0)) {
            //goes from xVel, yVel to one of eight directions
            transform.direction = ExtraMathUtils.angleToVector2Int(MathUtils.atan2(transform.yVel, transform.xVel));
        }
    }

    public void moveWithCollisions(Entity entity) {
        WorldColliderComponent collider = WorldColliderComponent.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);
        //float dt = Math.min(Gdx.graphics.getDeltaTime(), 1f / 15f); // clamp to max 1/15 second
        float dt = DeltaTime.deltaTimeMultiplier();
        float speed = (float)Math.sqrt(transform.xVel * transform.xVel + transform.yVel * transform.yVel);

        //Determine number of steps to use (never allow one step to move more than MAX_DIST, unless steps > 8)
        float MAX_DIST = Application.TILE_WIDTH * 0.25f;
        int steps = 1;
        if (speed * dt > MAX_DIST && speed > 0f) {
            steps = Math.min(8, (int)Math.ceil((speed * dt) / MAX_DIST));
        }

        float subDt = dt / steps;
        for (int i = 0; i < steps; i++) {
            updateCollisions(entity, subDt);
        }
    }

    public void ejectFromWalls(Entity entity) {
        if (!inWall(entity)) {
            return;
        }
        boolean[][] collidableMap = Application.getInstance().currentScreen.isCollidableGrid();
        //boolean[][] rockCollidableMap = Application.getInstance().caveScreen.caveTilemapCreator.rockCollidablePositions;

        WorldColliderComponent collider = WorldColliderComponent.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);


        Vector2 center = new Vector2(collider.getHitbox(transform).x, collider.getHitbox(transform).y);
        Vector2Int start = ExtraMathUtils.toTileCoordinates(center.x, center.y);
        int maxRadius = 12;
        Vector2Int best = null;
        float bestDist2 = Float.MAX_VALUE;
        for (int r = 0; r <= maxRadius; r++) {
            for (int y = start.y - r; y <= start.y + r; y++) {
                for (int x = start.x - r; x <= start.x + r; x++) {
                    if (x < 0 || y < 0 || y >= collidableMap.length || x >= collidableMap[0].length) continue;
                    boolean blocked = collidableMap[y][x];
                    //if (collider.isBat && rockCollidableMap[y][x]) blocked = false;
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
            transform.x = best.x * Application.TILE_WIDTH;
            transform.y = best.y * Application.TILE_HEIGHT;
            transform.xVel = 0f;
            transform.yVel = 0f;
        }
    }

    private void updateCollisions(Entity entity, float deltaTime) {
        WorldColliderComponent collider = WorldColliderComponent.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);
        Circle hitbox = collider.getHitbox(transform);

        boolean[][] collidableMap = Application.getInstance().currentScreen.isCollidableGrid();
        //boolean[][] rockCollidableMap = Application.getInstance().caveScreen.caveTilemapCreator.rockCollidablePositions;
        boolean[][] rockCollidableMap = new boolean[collidableMap.length][collidableMap[0].length];

        collider.tilePosition.set(
                ExtraMathUtils.toTileCoordinates(hitbox.x, hitbox.y)
        );

        collider.nextPosition.set(
                hitbox.x + transform.xVel * deltaTime,
                hitbox.y + transform.yVel * deltaTime
        );

        collider.nextTilePosition.set(
                ExtraMathUtils.toTileCoordinates(collider.nextPosition)
        );

        ArrayList<Vector2Int> tiles = getWallTilePositionsToCheck(collider);
        for (Vector2Int v : tiles) {
            boolean collidable = false;
            try {
                collidable = collidableMap[v.y][v.x];
                if (collider.IS_BAT && rockCollidableMap[v.y][v.x]) collidable = false;
            } catch (Exception ignored) {}

            if (isOutOfBounds(v, collidableMap) || collidable) {
                Vector2 nearest = new Vector2(
                        ExtraMathUtils.clamp(collider.nextPosition.x, v.x * Application.TILE_WIDTH, (v.x + 1) * Application.TILE_WIDTH),
                        ExtraMathUtils.clamp(collider.nextPosition.y, v.y * Application.TILE_HEIGHT, (v.y + 1) * Application.TILE_HEIGHT)
                );
                Vector2 vecToNearest = nearest.sub(collider.nextPosition);
                float overlap = collider.RADIUS - vecToNearest.len() * (collider.RADIUS / 7f);
                if (Float.isNaN(overlap)) continue;

                if (overlap > 0f) {
                    Vector2 dir = vecToNearest.nor();
                    transform.xVel -= dir.x * overlap;
                    transform.yVel -= dir.y * overlap;
                }
            }
        }

        // Move entity after collision resolution
        transform.x += transform.xVel * deltaTime;
        transform.y += transform.yVel * deltaTime;


    }


    private static boolean isOutOfBounds(Vector2Int v, boolean[][] map) {
        return v.x < 0 || v.x >= map[0].length || v.y < 0 || v.y >= map.length;
    }

    private static ArrayList<Vector2Int> getWallTilePositionsToCheck(WorldColliderComponent collider) {
        ArrayList<Vector2Int> out = new ArrayList<>();
        Vector2Int tl = ExtraMathUtils.minComponents(collider.tilePosition, collider.nextTilePosition);
        Vector2Int br = ExtraMathUtils.maxComponents(collider.tilePosition, collider.nextTilePosition);
        tl = new Vector2Int(Math.max(0, tl.x - 1), Math.max(0, tl.y - 1));
        br = new Vector2Int(
                Math.min(Application.getInstance().currentScreen.getMapWidth() - 1, br.x + 1),
                Math.min(Application.getInstance().currentScreen.getMapHeight() - 1, br.y + 1)
        );
        for (int y = tl.y; y <= br.y; y++) {
            for (int x = tl.x; x <= br.x; x++) {
                out.add(new Vector2Int(x, y));
            }
        }
        return out;
    }

    public boolean inWall(Entity entity) {
        WorldColliderComponent collider = WorldColliderComponent.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);

        boolean[][] collidableMap = Application.getInstance().currentScreen.isCollidableGrid();
        collider.tilePosition = ExtraMathUtils.toTileCoordinates(collider.getHitbox(transform).x, collider.getHitbox(transform).y);
        if (isOutOfBounds(collider.tilePosition, Application.getInstance().currentScreen.isCollidableGrid())) {
            return true;
        }
        boolean output = collidableMap[collider.tilePosition.y][collider.tilePosition.x];
//        if (collider.isBat) {
//            boolean[][] rockCollidableMap = Application.getInstance().caveScreen.caveTilemapCreator.rockCollidablePositions;
//            if (rockCollidableMap[tilePosition.y][tilePosition.x]) {
//                output = false;
//            }
//        }
        return output;
    }


    public static boolean checkHolePositions(com.badlogic.ashley.core.Entity entity) {
        Transform transform = Transform.MAPPER.get(entity);
        RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(entity);
        WorldColliderComponent worldColliderComponent = WorldColliderComponent.MAPPER.get(entity);
        if (!transform.ENTITY_NAME.equals("player")) {
            throw new RuntimeException("canFall must be called with player as arg");
        }

        if (!routineListComponent.inAction("Fall") && !routineListComponent.inAction("Dive")) {
            boolean[][] holePositions;
            if (Application.getInstance().currentScreen == Application.getInstance().caveScreen) {
                holePositions = Application.getInstance().caveScreen.getHolePositionsToCheck();
            } else if (Application.getInstance().currentScreen == Application.getInstance().townScreen) {
                holePositions = Application.getInstance().townScreen.getHolePositions();
            } else if (Application.getInstance().currentScreen == Application.getInstance().slimeBossRoomScreen) {
                holePositions = Application.getInstance().slimeBossRoomScreen.getHolePositions();
            } else {
                return false;
            }
            ArrayList<Vector2Int> wallTilesToCheck = getWallTilePositionsToCheck(worldColliderComponent);
            boolean aboveHole = false;
            for (Vector2Int checkedWallTilePosition : wallTilesToCheck) {
                int x = 0, y;
                for (y = -1; y <= 1; y += 1) {
                    aboveHole = aboveHole || checkTile(entity, checkedWallTilePosition, holePositions, x, y);
                }
                y=0;
                for (x = -1; x <= 1; x += 1) {
                    aboveHole = aboveHole || checkTile(entity, checkedWallTilePosition, holePositions, x, y);
                }
            }
            return aboveHole;
        }
        return false;
    }


    private static boolean checkTile(Entity entity, Vector2Int checkedWallTilePosition, boolean[][] holePositions, int x, int y) {
        Transform transform = Transform.MAPPER.get(entity);
        Vector2Int v = new Vector2Int(checkedWallTilePosition.x + x, checkedWallTilePosition.y + y);
        Rectangle checkedTileBounds = new Rectangle(v.x * Application.TILE_WIDTH, v.y * Application.TILE_HEIGHT, Application.TILE_WIDTH, Application.TILE_HEIGHT);
        boolean isInBounds = v.x >= 0 && v.x < holePositions[0].length && v.y >= 0 && v.y < holePositions.length;
        boolean vIsHole = false;
        try {
            vIsHole = holePositions[v.y][v.x];
        } catch (Exception e) {

        }
        boolean output = false;
        if (isInBounds && vIsHole) {
            if (checkedTileBounds.contains(transform.getCenteredX(), transform.getCenteredY())) {
                output = true;
            }
        }
        return output;
    }
}
