package com.mikm.rendering.tilemap;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.tilemap.ruleCell.RuleCell;
import com.mikm.rendering.tilemap.ruleCell.RuleCellTiledMapTileLayer;

import java.util.Arrays;
import java.util.Random;

public class CaveLevelGenerator {
    public static final int mapWidth = 80, mapHeight = 60;
    private final int randomFillPercent = 50;
    private final long seed = 21;

    private final RuleCell ruleCell;
    private final TextureRegion[] wallImages;
    private final TextureRegion floorImage;

    private final Random random;
    private boolean[][] ruleCellPositions;

    public CaveLevelGenerator(RuleCell ruleCell) {
        this.ruleCell = ruleCell;
        wallImages = Arrays.copyOfRange(ruleCell.spritesheet[2], 0, 3 + 1);
        floorImage = ruleCell.spritesheet[2][4];
        random = new Random();
    }

    public TiledMap createTiledMap() {
        RuleCellTiledMapTileLayer ruleCellLayer = createRuleCellTiledMapTileLayer();
        TiledMapTileLayer uncollidableLayer = createUncollidableLayer();

        return createMapFromLayers(ruleCellLayer, uncollidableLayer);
    }

    private TiledMap createMapFromLayers(RuleCellTiledMapTileLayer ruleCellLayer, TiledMapTileLayer uncollidableLayer) {
        TiledMap tiledMap = new TiledMap();
        MapLayers mapLayers = tiledMap.getLayers();
        mapLayers.add(uncollidableLayer);
        mapLayers.add(ruleCellLayer);
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
        TiledMapTileLayer.Cell wallCell = new TiledMapTileLayer.Cell();
        StaticTiledMapTile wallTile = new StaticTiledMapTile(wallImages[1]);
        wallCell.setTile(wallTile);
        boolean isOutOfBounds = (y+1 > mapHeight - 1);
        if (isOutOfBounds) {
            return;
        }
        if (ruleCellPositions[y+1][x]) {
            uncollidableLayer.setCell(x, y, wallCell);
        }
    }
}