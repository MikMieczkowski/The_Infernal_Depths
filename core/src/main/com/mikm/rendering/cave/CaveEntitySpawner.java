package com.mikm.rendering.cave;

import com.mikm.RandomUtils;
import com.mikm.Vector2Int;
import com.mikm.entities.Entity;
import com.mikm.entityLoader.EntityLoader;
import com.mikm.entities.inanimateEntities.InanimateEntity;
import com.mikm.entities.inanimateEntities.Rope;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.CaveScreen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CaveEntitySpawner {
    private final CaveScreen caveScreen;

    private boolean[][] ruleCellPositions;
    private ArrayList<Vector2Int> openTilePositions;


    public CaveEntitySpawner(CaveScreen caveScreen) {
        this.caveScreen = caveScreen;
    }
    
    public void spawn(CaveTilemapCreator tilemap) {
        this.ruleCellPositions = tilemap.ruleCellPositions;
        this.openTilePositions = tilemap.openTiles;

        resetInanimateAndAnimateEntities();

        spawnRocks();
        spawnEnemies();
    }

    public void activate(CaveFloorMemento memento) {
        resetInanimateAndAnimateEntities();
        Vector2Int ropePosition = new Vector2Int(memento.ropePosition.x+8, memento.ropePosition.y+8);
        caveScreen.inanimateEntities.addInstantly(new Rope(ropePosition.x, ropePosition.y));
        caveScreen.currentRopePosition = ropePosition;

        for (InanimateEntity i : memento.inanimateEntities) {
            caveScreen.addInanimateEntity(i);
        }
        for (InanimateEntity i : memento.graves) {
            caveScreen.addInanimateEntity(i);
        }
        for (Entity e : memento.enemies) {
            e.hp = e.MAX_HP;
            e.damagedAction.dead = false;
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
        if (openTilePositions.isEmpty()) {
            return;
        }

        Map<String, SpawnProbability> entityData = new HashMap<>();
        for (String name : EntityLoader.getEntitiesInYamlFolder()) {
            if (name.equals("player")) {
                continue;
            }
            Entity entity = EntityLoader.create(name);
            if (entity.spawnProbability != null) {
                entityData.put(entity.NAME, entity.spawnProbability);
            }
        }

        for (Map.Entry<String, SpawnProbability> entry : entityData.entrySet()) {
            int times = (int)(entry.getValue().getProbabilityByFloor(CaveScreen.floor) * openTilePositions.size());
            for (int i = 0; i < times; i++) {
                Entity entity = EntityLoader.create(entry.getKey());
                //spawn entity
                Vector2Int randomTilePosition = openTilePositions.get(RandomUtils.getInt(openTilePositions.size()-1));
                entity.x = randomTilePosition.x * Application.TILE_WIDTH;
                entity.y = randomTilePosition.y * Application.TILE_HEIGHT;
                caveScreen.addEntityInstantly(entity);
            }
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
                caveScreen.caveTilemapCreator.rockCollidablePositions[tilePosition.y][tilePosition.x] = true;
                caveScreen.caveTilemapCreator.collidablePositions[tilePosition.y][tilePosition.x] = true;
                positionsToDelete.add(tilePosition);
            }
        }
        openTilePositions.removeAll(positionsToDelete);
    }
}
