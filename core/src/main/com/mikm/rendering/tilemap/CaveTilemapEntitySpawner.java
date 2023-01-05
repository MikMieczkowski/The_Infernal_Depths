package com.mikm.rendering.tilemap;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.mikm.ExtraMathUtils;
import com.mikm.Vector2Int;
import com.mikm.entities.enemies.Slime;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.CaveScreen;

import java.util.ArrayList;
import java.util.Arrays;

import static com.mikm.rendering.tilemap.CaveTilemap.mapWidth;
import static com.mikm.rendering.tilemap.CaveTilemap.mapHeight;

public class CaveTilemapEntitySpawner {
    private final CaveScreen caveScreen;

    private final TextureRegion[][] rockImages;
    private final ArrayList<TextureRegion[]> slimeSpritesheet;
    private final boolean[][] ruleCellPositions;
    private final ArrayList<Vector2Int> openTilePositions;

    private final float randomRockSpawnPercent = .5f;
    private final int minEnemies = 90, maxEnemies = 100;

    CaveTilemapEntitySpawner(CaveScreen caveScreen, boolean[][] ruleCellPositions) {
        this.caveScreen = caveScreen;
        this.ruleCellPositions = ruleCellPositions;

        rockImages = caveScreen.rockImages;
        slimeSpritesheet = caveScreen.slimeSpritesheet;
        openTilePositions = findOpenTilePositions();

    }

    public void spawnEnemies() {
        int enemyAmount = ExtraMathUtils.randomInt(minEnemies, maxEnemies + 1);
        for (int i = 0; i < enemyAmount; i++) {
            Vector2Int randomTilePosition = openTilePositions.get(ExtraMathUtils.randomInt(openTilePositions.size()));
            Slime slime = new Slime(slimeSpritesheet, randomTilePosition.x * Application.defaultTileWidth, randomTilePosition.y * Application.defaultTileHeight);
            slime.setScreen(caveScreen);
        }
    }

    public TiledMapTileLayer createRockLayer() {
        TiledMapTileLayer rockLayer = new TiledMapTileLayer(mapWidth, mapHeight, Application.defaultTileWidth, Application.defaultTileHeight);
        TiledMapTileLayer.Cell[] rockCells = new TiledMapTileLayer.Cell[3];

        for (int i = 0; i < 3; i++) {
            rockCells[i] = new TiledMapTileLayer.Cell();
            rockCells[i].setTile(new StaticTiledMapTile(rockImages[0][i]));
        }

        for (Vector2Int tilePosition : openTilePositions) {
            if (ExtraMathUtils.randomFloatOneDecimalPlace(100) < randomRockSpawnPercent) {
                TiledMapTileLayer.Cell randomRockCell = rockCells[ExtraMathUtils.randomInt(3)];
                rockLayer.setCell(tilePosition.x, tilePosition.y, randomRockCell);
            }
        }
        return rockLayer;
    }

    private ArrayList<Vector2Int> findOpenTilePositions() {
         ArrayList<Vector2Int> output = new ArrayList<>();
        for (int y = mapHeight - 1; y >= 0; y--) {
            for (int x = 0; x < mapWidth; x++) {
                boolean inOpenTile = !ruleCellPositions[y][x] && y + 1 <= mapHeight - 1 && !ruleCellPositions[y + 1][x];
                if (inOpenTile) {
                    output.add(new Vector2Int(x, y));
                }
            }
        }
        return output;
    }
}
