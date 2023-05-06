package com.mikm.rendering.cave;

import com.badlogic.gdx.math.Vector2;
import com.mikm.ExtraMathUtils;
import com.mikm.RandomUtils;
import com.mikm.Vector2Int;
import com.mikm.entities.Rope;
import com.mikm.entities.enemies.Slime;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.CaveScreen;

import java.util.ArrayList;

public class CaveEntitySpawner {
    private final CaveScreen caveScreen;

    private boolean[][] ruleCellPositions;
    private boolean[][] collidablePositions;
    private ArrayList<Vector2Int> openTilePositions;

    private final int MIN_ENEMIES = 90, MAX_ENEMIES = 100;

    public CaveEntitySpawner(CaveScreen caveScreen) {
        this.caveScreen = caveScreen;
    }
    
    public void generateNewEnemies(CaveTilemapCreator tilemap) {
        this.ruleCellPositions = tilemap.ruleCellPositions;
        this.collidablePositions = tilemap.collidablePositions;
        this.openTilePositions = tilemap.openTiles;

        resetInanimateAndAnimateEntities();

        Vector2 playerTileCoordinates = ExtraMathUtils.toTileCoordinates(Application.player.getCenteredPosition().x, Application.player.getCenteredPosition().y);
        caveScreen.inanimateEntities.addInstantly(new Rope(playerTileCoordinates.x * Application.TILE_WIDTH, playerTileCoordinates.y * Application.TILE_HEIGHT));

        spawnEnemies();
        spawnRocks();
    }

    public void activate(CaveFloorMemento memento) {
        resetInanimateAndAnimateEntities();
        caveScreen.inanimateEntities.addInstantly(new Rope(memento.spawnPosition.x+8, memento.spawnPosition.y+8));
        caveScreen.inanimateEntities.addAll(memento.inanimateEntities);
        caveScreen.entities.addAll(memento.enemies);
    }

    private void resetInanimateAndAnimateEntities() {
        caveScreen.entities.removeInstantly(Application.player);
        caveScreen.entities.clear();
        caveScreen.inanimateEntities.clear();
        caveScreen.entities.addInstantly(Application.player);
        caveScreen.addPlayerShadow();
    }

    private void spawnEnemies() {
        if (openTilePositions.size() == 0) {
            return;
        }

        int enemyAmount = RandomUtils.getInt(MIN_ENEMIES, MAX_ENEMIES);
        for (int i = 0; i < enemyAmount; i++) {
            Vector2Int randomTilePosition = openTilePositions.get(RandomUtils.getInt(openTilePositions.size()-1));
            Slime slime = new Slime(randomTilePosition.x * Application.TILE_WIDTH, randomTilePosition.y * Application.TILE_HEIGHT);
            caveScreen.addEntityInstantly(slime);
        }
    }

    public void spawnRocks() {
        if (openTilePositions.size() == 0) {
            return;
        }

        ArrayList<Vector2Int> positionsToDelete = new ArrayList<>();
        for (Vector2Int tilePosition : openTilePositions) {
            SpawnProbability rockDistribution = SpawnProbabilityConstants.ROCK_FILL;
            if (RandomUtils.getFloatRoundedToTenths(100) < rockDistribution.getProbabilityByFloor(CaveScreen.floor) * 100f) {
                RockType randomRockType = RockType.getRandomRockType(SpawnProbabilityConstants.getOreDistributionsByFloor(CaveScreen.floor));
                caveScreen.inanimateEntities.addInstantly(new Rock(tilePosition.x * Application.TILE_WIDTH, tilePosition.y * Application.TILE_HEIGHT, randomRockType));
                collidablePositions[tilePosition.y][tilePosition.x] = true;
                positionsToDelete.add(tilePosition);
            }
        }
        openTilePositions.removeAll(positionsToDelete);
    }
}
