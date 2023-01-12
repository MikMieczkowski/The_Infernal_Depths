package com.mikm.rendering.tilemap;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
    private final boolean[][] isCollidable;
    public final ArrayList<Vector2Int> openTilePositions;

    private final float ROCK_PERCENT_CHANCE = 8f;
    private final int MIN_ENEMIES = 90, MAX_ENEMIES = 100;

    CaveTilemapEntitySpawner(CaveScreen caveScreen, boolean[][] ruleCellPositions) {
        this.caveScreen = caveScreen;
        this.ruleCellPositions = ruleCellPositions;
        isCollidable = ruleCellPositions.clone();

        rockImages = caveScreen.rockImages;
        slimeActionSpritesheets = caveScreen.slimeActionSpritesheets;
        openTilePositions = findOpenTilePositions();

    }

    public void spawnEnemies() {
        if (openTilePositions.size() == 0) {
            return;
        }

        int enemyAmount = ExtraMathUtils.randomInt(MIN_ENEMIES, MAX_ENEMIES);
        for (int i = 0; i < enemyAmount; i++) {
            Vector2Int randomTilePosition = openTilePositions.get(ExtraMathUtils.randomInt(openTilePositions.size()));
            Slime slime = new Slime(randomTilePosition.x * Application.TILE_WIDTH, randomTilePosition.y * Application.TILE_HEIGHT, slimeActionSpritesheets);
            caveScreen.addEntity(slime);
        }
    }

    public void createRockLayer() {
        if (openTilePositions.size() == 0) {
            return;
        }


        ArrayList<Vector2Int> positionsToDelete = new ArrayList<>();
        for (Vector2Int tilePosition : openTilePositions) {
            if (ExtraMathUtils.randomFloatOneDecimalPlace(100) < ROCK_PERCENT_CHANCE) {
                float[] rockTypeChances = new float[]{.95f, .045f, .005f};
                RockType randomRockType = RockType.getRandomRockType(rockTypeChances);
                caveScreen.inanimateEntities.add(new Rock(randomRockType, tilePosition.x * Application.TILE_WIDTH, tilePosition.y * Application.TILE_HEIGHT));
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

    public boolean[][] getCollidableTilePositions() {
        return isCollidable;
    }
}
