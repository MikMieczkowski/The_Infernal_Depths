package com.mikm.rendering.cave;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mikm._components.*;
import com.mikm.entities.prefabLoader.EntityData;
import com.mikm.utils.ExtraMathUtils;
import com.mikm.utils.RandomUtils;
import com.mikm.Vector2Int;
import com.mikm.entities.prefabLoader.EntityYAMLReader;
import com.mikm.entities.prefabLoader.PrefabInstantiator;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.CaveScreen;
import com.mikm.utils.debug.DebugRenderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CaveEntitySpawner {
    private final CaveScreen caveScreen;

    private boolean[][] ruleCellPositions;
    private ArrayList<Vector2Int> openTilePositions;


    public CaveEntitySpawner(CaveScreen caveScreen) {
        this.caveScreen = caveScreen;
    }
    
    public void load(CaveTilemapCreator tilemap) {
        this.ruleCellPositions = tilemap.ruleCellPositions;
        this.openTilePositions = tilemap.openTiles;

        loadRocks();
        loadEnemies();
    }

    public void activate(CaveFloorMemento memento) {
        resetInanimateAndAnimateEntities();
        caveScreen.caveTilemapCreator.rockCollidablePositions = new boolean[caveScreen.caveTilemapCreator.MAP_HEIGHT][caveScreen.caveTilemapCreator.MAP_WIDTH];

        Vector2Int ropePosition = new Vector2Int(memento.ropePosition.x+8, memento.ropePosition.y+8);
        PrefabInstantiator.addEntity("rope", caveScreen, ropePosition.x, ropePosition.y);
        caveScreen.currentRopePosition = ropePosition;

        //Add all the entities in memento.entities to the cavescreen

        for (EntityData entityData : memento.enemies) {
            if (entityData.prefabName.equals("player") || entityData.prefabName.equals("rope")) {
                continue;
            }
            PrefabInstantiator.addEntity(entityData.prefabName, caveScreen, entityData.pos);
            //CombatComponent.MAPPER.get(entity).hp = CombatComponent.MAPPER.get(entity).MAX_HP;
        }


        for (EntityData entityData : memento.rocks) {
            PrefabInstantiator.addRock(caveScreen, entityData.pos.x, entityData.pos.y, entityData.rockType);
            Vector2Int tilePos = ExtraMathUtils.toTileCoordinates(entityData.pos);
            caveScreen.caveTilemapCreator.rockCollidablePositions[tilePos.y][tilePos.x] = true;
            caveScreen.caveTilemapCreator.collidablePositions[tilePos.y][tilePos.x] = true;
        }

        // Restore graves
        for (EntityData entityData : memento.graves) {
            Entity grave = PrefabInstantiator.addEntity("grave", caveScreen, entityData.pos);
            GraveComponent graveComponent = GraveComponent.MAPPER.get(grave);
            if (graveComponent != null && entityData.ores != null) {
                graveComponent.ores = entityData.ores;
            }
        }
    }

    private void resetInanimateAndAnimateEntities() {
        ImmutableArray<Entity> entities = caveScreen.engine.getEntities();
        for (int i = entities.size() - 1; i >= 0; i--) {
            Entity e = entities.get(i);
            String entityName = Transform.MAPPER.get(e).ENTITY_NAME;
            if (!entityName.equals("player") && !entityName.equals("playerWeapon")) {
                caveScreen.engine.removeEntity(e);
            }
        }
    }

    private void loadEnemies() {
        if (openTilePositions.isEmpty()) {
            return;
        }

        Map<String, SpawnProbability> spawnProbabilities = new HashMap<>();
        //find spawnProbabilities
        for (String name : EntityYAMLReader.getEntitiesInYamlFolder()) {
            if (name.equals("player")) {
                continue;
            }
            Entity entity = PrefabInstantiator.addEntity(name, Application.getInstance().caveScreen);
            SpawnComponent spawnComponent = SpawnComponent.MAPPER.get(entity);
            Transform transform = Transform.MAPPER.get(entity);
            if (spawnComponent != null) {
                spawnProbabilities.put(transform.ENTITY_NAME, spawnComponent.spawnProbability);
            }
            Application.getInstance().caveScreen.removeEntity(entity);
        }

        //spawn entities
        for (Map.Entry<String, SpawnProbability> entry : spawnProbabilities.entrySet()) {
            int times = (int)(entry.getValue().getProbabilityByFloor(CaveScreen.floor) * openTilePositions.size());
            for (int i = 0; i < times; i++) {
                Vector2Int randomTilePosition = openTilePositions.get(RandomUtils.getInt(openTilePositions.size()-1));
                //PrefabInstantiator.addEntity(entry.getKey(), Application.getInstance().caveScreen,
                //        randomTilePosition.x * Application.TILE_WIDTH, randomTilePosition.y * Application.TILE_HEIGHT);

                EntityData entityData = new EntityData(new Vector2( randomTilePosition.x * Application.TILE_WIDTH, randomTilePosition.y * Application.TILE_HEIGHT),
                        entry.getKey());
                caveScreen.caveFloorMementos[CaveScreen.floor - 1].enemies.add(entityData);

            }
        }
    }

    public void loadRocks() {
        if (openTilePositions.isEmpty()) {
            return;
        }

        ArrayList<Vector2Int> positionsToDelete = new ArrayList<>();
        for (Vector2Int tilePosition : openTilePositions) {
            SpawnProbability rockDistribution = SpawnProbabilityConstants.ROCK_FILL;
            if (RandomUtils.getFloatRoundedToTenths(100) < rockDistribution.getProbabilityByFloor(CaveScreen.floor) * 100f) {
                RockType randomRockType = RockType.getRandomRockType(SpawnProbabilityConstants.getOreDistributionsByFloor(CaveScreen.floor));

                EntityData rockData = new EntityData(new Vector2(tilePosition.x * Application.TILE_WIDTH, tilePosition.y * Application.TILE_HEIGHT),
                        "destructible", randomRockType);
                caveScreen.caveFloorMementos[CaveScreen.floor - 1].rocks.add(rockData);
                positionsToDelete.add(tilePosition);
            }
        }
        openTilePositions.removeAll(positionsToDelete);
    }
}
