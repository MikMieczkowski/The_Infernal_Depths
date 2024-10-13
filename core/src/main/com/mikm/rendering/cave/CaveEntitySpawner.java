package com.mikm.rendering.cave;

import com.mikm.RandomUtils;
import com.mikm.Vector2Int;
import com.mikm.debug.DebugRenderer;
import com.mikm.entities.Entity;
import com.mikm.entities.InanimateEntity;
import com.mikm.entities.Rope;
import com.mikm.entities.enemies.Bat;
import com.mikm.entities.enemies.Slime;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.CaveScreen;

import java.util.ArrayList;

public class CaveEntitySpawner {
    private final CaveScreen caveScreen;

    private boolean[][] ruleCellPositions;
    private boolean[][] collidablePositions;
    private ArrayList<Vector2Int> openTilePositions;


    public CaveEntitySpawner(CaveScreen caveScreen) {
        this.caveScreen = caveScreen;
    }
    
    public void generateNewEnemies(CaveTilemapCreator tilemap) {
        this.ruleCellPositions = tilemap.ruleCellPositions;
        this.collidablePositions = tilemap.collidablePositions;
        this.openTilePositions = tilemap.openTiles;

        resetInanimateAndAnimateEntities();

        spawnRocks();
        spawnEnemies();
    }

    public void activate(CaveFloorMemento memento) {
        resetInanimateAndAnimateEntities();
        caveScreen.inanimateEntities.addInstantly(new Rope(memento.ropePosition.x+8, memento.ropePosition.y+8));
        for (InanimateEntity i : memento.inanimateEntities) {
            caveScreen.addInanimateEntity(i);
        }
        for (Entity e : memento.enemies) {
            e.hp = e.getMaxHp();
            e.damagedState.dead = false;
            caveScreen.addEntity(e);
        }
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

        int enemyAmount = (int) SpawnProbabilityConstants.ENEMY_AMOUNT.getProbabilityByFloor(CaveScreen.floor-1);
        for (int i = 0; i < enemyAmount; i++) {
            Vector2Int randomTilePosition = openTilePositions.get(RandomUtils.getInt(openTilePositions.size()-1));
            Slime slime = new Slime(randomTilePosition.x * Application.TILE_WIDTH, randomTilePosition.y * Application.TILE_HEIGHT);
            caveScreen.addEntityInstantly(slime);
            randomTilePosition = openTilePositions.get(RandomUtils.getInt(openTilePositions.size()-1));
            Bat bat = new Bat(randomTilePosition.x * Application.TILE_WIDTH, randomTilePosition.y * Application.TILE_HEIGHT);
            caveScreen.addEntityInstantly(bat);
        }
    }

    public void spawnRocks() {
        if (openTilePositions.isEmpty()) {
            return;
        }

        ArrayList<Vector2Int> positionsToDelete = new ArrayList<>();
        for (Vector2Int tilePosition : openTilePositions) {
            SpawnProbability rockDistribution = SpawnProbabilityConstants.ROCK_FILL;
            if (RandomUtils.getFloatRoundedToTenths(100) < rockDistribution.getProbabilityByFloor(CaveScreen.floor) * 100f) {
                RockType randomRockType = RockType.getRandomRockType(SpawnProbabilityConstants.getOreDistributionsByFloor(CaveScreen.floor));
                caveScreen.inanimateEntities.addInstantly(new Rock(tilePosition.x * Application.TILE_WIDTH, tilePosition.y * Application.TILE_HEIGHT, randomRockType, CaveScreen.getRecolorLevel()));
                collidablePositions[tilePosition.y][tilePosition.x] = true;
                positionsToDelete.add(tilePosition);
            }
        }
        openTilePositions.removeAll(positionsToDelete);
    }
}
