package com.mikm.rendering.tilemap;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.CaveScreen;
import com.mikm.rendering.tilemap.ruleCell.RuleCell;
import com.mikm.rendering.tilemap.ruleCell.RuleCellMetadata;
import com.mikm.rendering.tilemap.ruleCell.RuleCellMetadataReader;
import com.mikm.rendering.tilemap.ruleCell.RuleCellTiledMapTileLayer;

import java.util.Arrays;
import java.util.Random;

public class CaveLevelGenerator {
    public static final int mapWidth = 80, mapHeight = 60;
    private final int randomFillPercent = 50;
    //must be rounded to tenths
    private final float randomRockSpawnPercent = .5f;
    private final long seed = 21;

    private final RuleCell ruleCell;
    private final TextureRegion[] wallImages;
    private final TextureRegion floorImage;
    private final TextureRegion[][] rockImages;

    private final Random random;
    private boolean[][] ruleCellPositions;

    private boolean useWallCell1 = false;


    public CaveLevelGenerator(CaveScreen caveScreen) {
        ruleCell = createCaveRuleCell(caveScreen.caveTileset);
        wallImages = Arrays.copyOfRange(caveScreen.caveTileset[2], 0, 3 + 1);
        floorImage = ruleCell.spritesheet[2][4];
        rockImages = caveScreen.rockImages;
        random = new Random();
    }

    private RuleCell createCaveRuleCell(TextureRegion[][] caveTileset) {
        RuleCellMetadataReader metadataReader = new RuleCellMetadataReader();
        RuleCellMetadata metadata = metadataReader.createMetadataFromFile("images/caveTiles.meta.txt");
        return new RuleCell(caveTileset, metadata);
    }

    public TiledMap createTiledMap() {
        RuleCellTiledMapTileLayer ruleCellLayer = createRuleCellTiledMapTileLayer();
        TiledMapTileLayer uncollidableLayer = createUncollidableLayer();
        TiledMapTileLayer rockLayer = createRockLayer();

        return createMapFromLayers(ruleCellLayer, uncollidableLayer, rockLayer);
    }

    private TiledMap createMapFromLayers(RuleCellTiledMapTileLayer ruleCellLayer, TiledMapTileLayer uncollidableLayer, TiledMapTileLayer rockLayer) {
        TiledMap tiledMap = new TiledMap();
        MapLayers mapLayers = tiledMap.getLayers();
        mapLayers.add(uncollidableLayer);
        mapLayers.add(ruleCellLayer);
        mapLayers.add(rockLayer);
        return tiledMap;
    }

    private RuleCellTiledMapTileLayer createRuleCellTiledMapTileLayer() {
        ruleCellPositions = new boolean[mapHeight][mapWidth];

        fillRuleCellPositionsRandomly();
        for (int i = 0; i < 5; i++) {
            smoothRuleCellPositions();
        }
        return createRuleCellLayerFromRuleCellPositions();
    }

    private void fillRuleCellPositionsRandomly() {
        for (int y = mapHeight - 1; y >= 0; y--) {
            for (int x = 0; x < mapWidth; x++) {
                if (x == 0 || x == mapWidth - 1 || y == 0 || y == mapHeight - 1) {
                    ruleCellPositions[y][x] = true;
                } else {
                    ruleCellPositions[y][x] = (random.nextInt(100) < randomFillPercent);
                }
            }
        }
    }

    private void smoothRuleCellPositions() {
        for (int y = mapHeight - 1; y >= 0; y--) {
            for (int x = 0; x < mapWidth; x++) {
                int neighboringWallCount = getNeighboringCellCount(y, x);
                if (neighboringWallCount > 4) {
                    ruleCellPositions[y][x] = true;
                } else if (neighboringWallCount < 4){
                    ruleCellPositions[y][x] = false;
                }
            }
        }
    }

    private int getNeighboringCellCount(int y, int x) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                boolean outOfBounds = (x+j < 0 || x+j > mapWidth - 1 || y+i < 0 || y+i > mapHeight - 1);
                if (outOfBounds) {
                    count++;
                    continue;
                }
                if (ruleCellPositions[y + i][x + j]) {
                    count++;
                }
            }
        }
        return count;
    }

    private RuleCellTiledMapTileLayer createRuleCellLayerFromRuleCellPositions() {
        RuleCellTiledMapTileLayer ruleCellLayer = new RuleCellTiledMapTileLayer(mapWidth, mapHeight, Application.defaultTileWidth, Application.defaultTileHeight);
        for (int y = mapHeight - 1; y >= 0; y--) {
            for (int x = 0; x < mapWidth; x++) {
                if (ruleCellPositions[y][x]) {
                    ruleCellLayer.setRuleCell(x, y, ruleCell);
                }
            }
        }
        ruleCellLayer.updateRuleCells();
        return ruleCellLayer;
    }

    private TiledMapTileLayer createUncollidableLayer() {
        TiledMapTileLayer uncollidableLayer = new TiledMapTileLayer(mapWidth, mapHeight, Application.defaultTileWidth, Application.defaultTileHeight);
        TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
        StaticTiledMapTile tile = new StaticTiledMapTile(floorImage);
        cell.setTile(tile);
        for (int y = mapHeight - 1; y >= 0; y--) {
            for (int x = 0; x < mapWidth; x++) {
                uncollidableLayer.setCell(x, y, cell);
                fillUncollidableLayerWithWallsAt(y, x, uncollidableLayer);
            }
        }
        return uncollidableLayer;
    }

    private void fillUncollidableLayerWithWallsAt(int y, int x, TiledMapTileLayer uncollidableLayer) {
        TiledMapTileLayer.Cell[] wallCell = new TiledMapTileLayer.Cell[4];
        for (int i = 0; i < 4; i++) {
            wallCell[i] = new TiledMapTileLayer.Cell();
            wallCell[i].setTile(new StaticTiledMapTile(wallImages[i]));
        }
        boolean isOutOfBounds = (y+1 > mapHeight - 1);
        if (isOutOfBounds) {
            return;
        }
        if (ruleCellPositions[y+1][x]) {
            uncollidableLayer.setCell(x, y, getCorrectWallCell(y, x, wallCell));
        }
    }

    private TiledMapTileLayer.Cell getCorrectWallCell(int y, int x, TiledMapTileLayer.Cell[] wallCell) {
        if (x - 1 < 0 || !ruleCellPositions[y+1][x-1]) {
            if (x+1 > mapWidth - 1 || !ruleCellPositions[y+1][x+1]) {
                return wallCell[2];
            }
            return wallCell[0];
        }
        if (x+1 > mapWidth - 1 || !ruleCellPositions[y+1][x+1]) {
            return wallCell[3];
        }
        useWallCell1 = !useWallCell1;
        if (useWallCell1) {
            return wallCell[1];
        } else {
            return wallCell[2];
        }
    }

    private TiledMapTileLayer createRockLayer() {
        TiledMapTileLayer rockLayer = new TiledMapTileLayer(mapWidth, mapHeight, Application.defaultTileWidth, Application.defaultTileHeight);
        TiledMapTileLayer.Cell[] rockCells = new TiledMapTileLayer.Cell[3];

        for (int i = 0; i < 3; i++) {
            rockCells[i] = new TiledMapTileLayer.Cell();
            rockCells[i].setTile(new StaticTiledMapTile(rockImages[0][i]));
        }

        for (int y = mapHeight - 1; y >= 0; y--) {
            for (int x = 0; x < mapWidth; x++) {
                boolean inOpenTile = !ruleCellPositions[y][x] && y + 1 <= mapHeight - 1 && !ruleCellPositions[y+1][x];
                if (inOpenTile) {
                    if (random.nextInt(1000) < 10 * randomRockSpawnPercent) {
                        TiledMapTileLayer.Cell randomRockCell = rockCells[random.nextInt(3)];
                        rockLayer.setCell(x, y, randomRockCell);
                    }
                }
            }
        }
        return rockLayer;
    }
}