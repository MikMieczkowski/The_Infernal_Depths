package com.mikm.rendering.cave;

import com.mikm.ExtraMathUtils;
import com.mikm.Vector2Int;
import com.mikm.entities.animation.EntityActionSpritesheets;
import com.mikm.entities.enemies.Slime;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.CaveScreen;

import java.util.ArrayList;

import static com.mikm.rendering.cave.CaveTilemap.MAP_HEIGHT;
import static com.mikm.rendering.cave.CaveTilemap.MAP_WIDTH;

public class CaveTilemapEntitySpawner {
    private final CaveScreen caveScreen;

    private final EntityActionSpritesheets slimeActionSpritesheets;

    private boolean[][] ruleCellPositions;
    private boolean[][] isCollidable;
    public ArrayList<Vector2Int> openTilePositions;

    private final int MIN_ENEMIES = 90, MAX_ENEMIES = 100;

    CaveTilemapEntitySpawner(CaveScreen caveScreen, boolean[][] ruleCellPositions) {
        this.caveScreen = caveScreen;
        this.ruleCellPositions = ruleCellPositions;
        isCollidable = ruleCellPositions.clone();

        slimeActionSpritesheets = caveScreen.slimeActionSpritesheets;
        openTilePositions = findOpenTilePositions();
    }
    
    public void generateNewMap(boolean[][] ruleCellPositions) {
        this.ruleCellPositions = ruleCellPositions;
        isCollidable = ruleCellPositions.clone();
        openTilePositions = findOpenTilePositions();

        caveScreen.entities.doAfterRender(() -> {
            caveScreen.entities.clear();
            caveScreen.inanimateEntities.clear();
            caveScreen.entities.addInstantly(Application.player);
            spawnEnemies();
            spawnRocks();
        });
    }

    public void spawnEnemies() {
        if (openTilePositions.size() == 0) {
            return;
        }

        int enemyAmount = ExtraMathUtils.randomInt(MIN_ENEMIES, MAX_ENEMIES);
        for (int i = 0; i < enemyAmount; i++) {
            Vector2Int randomTilePosition = openTilePositions.get(ExtraMathUtils.randomInt(openTilePositions.size()));
            Slime slime = new Slime(randomTilePosition.x * Application.TILE_WIDTH, randomTilePosition.y * Application.TILE_HEIGHT, slimeActionSpritesheets);
            caveScreen.entities.addInstantly(slime);
        }
    }

    public void spawnRocks() {
        if (openTilePositions.size() == 0) {
            return;
        }

        ArrayList<Vector2Int> positionsToDelete = new ArrayList<>();
        for (Vector2Int tilePosition : openTilePositions) {
            SpawnerDistribution rockDistribution = SpawnerDistributions.ROCK_FILL;
            if (ExtraMathUtils.randomFloatOneDecimalPlace(100) < rockDistribution.getDistributionByFloor(CaveScreen.floor) * 100f) {
                RockType randomRockType = RockType.getRandomRockType(SpawnerDistributions.getOreDistributionsByFloor(CaveScreen.floor));
                caveScreen.inanimateEntities.addInstantly(new Rock(randomRockType, tilePosition.x * Application.TILE_WIDTH, tilePosition.y * Application.TILE_HEIGHT));
                isCollidable[tilePosition.y][tilePosition.x] = true;
                positionsToDelete.add(tilePosition);
            }
        }
        openTilePositions.removeAll(positionsToDelete);
    }

    private ArrayList<Vector2Int> findOpenTilePositions() {
         ArrayList<Vector2Int> output = new ArrayList<>();
        for (int y = MAP_HEIGHT - 1; y >= 0; y--) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                boolean isNotInWallTile = y + 1 <= MAP_HEIGHT - 1 && !ruleCellPositions[y + 1][x];
                boolean inOpenTile = !ruleCellPositions[y][x] && isNotInWallTile;
                if (inOpenTile) {
                    output.add(new Vector2Int(x, y));
                }
            }
        }
        return output;
    }

    public boolean[][] getIsCollidableGrid() {
        return isCollidable;
    }
}
