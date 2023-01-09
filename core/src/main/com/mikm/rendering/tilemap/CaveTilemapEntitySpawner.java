package com.mikm.rendering.tilemap;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.mikm.ExtraMathUtils;
import com.mikm.Vector2Int;
import com.mikm.entities.animation.EntityActionSpritesheets;
import com.mikm.entities.enemies.Slime;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.CaveScreen;

import java.util.ArrayList;

import static com.mikm.rendering.tilemap.CaveTilemap.MAP_WIDTH;
import static com.mikm.rendering.tilemap.CaveTilemap.MAP_HEIGHT;

public class CaveTilemapEntitySpawner {
    private final CaveScreen caveScreen;

    private final TextureRegion[][] rockImages;
    private final EntityActionSpritesheets slimeActionSpritesheets;
    private final boolean[][] ruleCellPositions;
    public final ArrayList<Vector2Int> openTilePositions;

    private final float ROCK_PERCENT_CHANCE = 1f;
    private final int MIN_ENEMIES = 90, MAX_ENEMIES = 100;

    CaveTilemapEntitySpawner(CaveScreen caveScreen, boolean[][] ruleCellPositions) {
        this.caveScreen = caveScreen;
        this.ruleCellPositions = ruleCellPositions;

        rockImages = caveScreen.rockImages;
        slimeActionSpritesheets = caveScreen.slimeActionSpritesheets;
        openTilePositions = findOpenTilePositions();

    }

    public void spawnEnemies() {
        if (openTilePositions.size() == 0) {
            return;
        }

        int enemyAmount = ExtraMathUtils.randomInt(MIN_ENEMIES, MAX_ENEMIES + 1);
        for (int i = 0; i < enemyAmount; i++) {
            Vector2Int randomTilePosition = openTilePositions.get(ExtraMathUtils.randomInt(openTilePositions.size()));
            Slime slime = new Slime(randomTilePosition.x * Application.TILE_WIDTH, randomTilePosition.y * Application.TILE_HEIGHT, slimeActionSpritesheets);
            slime.setScreen(caveScreen);
        }
    }

    public TiledMapTileLayer createRockLayer() {
        TiledMapTileLayer rockLayer = new TiledMapTileLayer(MAP_WIDTH, MAP_HEIGHT, Application.TILE_WIDTH, Application.TILE_HEIGHT);
        if (openTilePositions.size() == 0) {
            return rockLayer;
        }

        TiledMapTileLayer.Cell[] rockCells = new TiledMapTileLayer.Cell[3];
        for (int i = 0; i < 3; i++) {
            rockCells[i] = new TiledMapTileLayer.Cell();
            rockCells[i].setTile(new StaticTiledMapTile(rockImages[0][i]));
        }

        ArrayList<Vector2Int> positionsToDelete = new ArrayList<>();
        for (Vector2Int tilePosition : openTilePositions) {
            if (ExtraMathUtils.randomFloatOneDecimalPlace(100) < ROCK_PERCENT_CHANCE) {
                TiledMapTileLayer.Cell randomRockCell = rockCells[ExtraMathUtils.randomInt(3)];
                rockLayer.setCell(tilePosition.x, tilePosition.y, randomRockCell);
                positionsToDelete.add(tilePosition);
            }
        }
        openTilePositions.removeAll(positionsToDelete);
        return rockLayer;
    }

    private ArrayList<Vector2Int> findOpenTilePositions() {
         ArrayList<Vector2Int> output = new ArrayList<>();
        for (int y = MAP_HEIGHT - 1; y >= 0; y--) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                boolean inOpenTile = !ruleCellPositions[y][x] && y + 1 <= MAP_HEIGHT - 1 && !ruleCellPositions[y + 1][x];
                if (inOpenTile) {
                    output.add(new Vector2Int(x, y));
                }
            }
        }
        return output;
    }
}
