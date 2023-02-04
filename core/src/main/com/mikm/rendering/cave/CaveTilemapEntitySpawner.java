package com.mikm.rendering.cave;

import com.mikm.ExtraMathUtils;
import com.mikm.Vector2Int;
import com.mikm.entities.animation.EntityActionSpritesheets;
import com.mikm.entities.enemies.Slime;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.CaveScreen;

import java.util.ArrayList;

public class CaveTilemapEntitySpawner {
    private final CaveScreen caveScreen;

    private final EntityActionSpritesheets slimeActionSpritesheets;

    private boolean[][] ruleCellPositions;
    private ArrayList<Vector2Int> openTilePositions;

    private final int MIN_ENEMIES = 90, MAX_ENEMIES = 100;

    CaveTilemapEntitySpawner(CaveScreen caveScreen) {
        this.caveScreen = caveScreen;

        slimeActionSpritesheets = caveScreen.slimeActionSpritesheets;
    }
    
    public void generateNewMap(boolean[][] ruleCellPositions, ArrayList<Vector2Int> openTilePositions) {
        this.ruleCellPositions = ruleCellPositions;
        this.openTilePositions = openTilePositions;

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
            Vector2Int randomTilePosition = openTilePositions.get(ExtraMathUtils.randomInt(openTilePositions.size()-1));
            Slime slime = new Slime(randomTilePosition.x * Application.TILE_WIDTH, randomTilePosition.y * Application.TILE_HEIGHT, slimeActionSpritesheets);
            caveScreen.addEntityInstantly(slime);
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
                ruleCellPositions[tilePosition.y][tilePosition.x] = true;
                positionsToDelete.add(tilePosition);
            }
        }
        openTilePositions.removeAll(positionsToDelete);
    }
}
