package com.mikm.rendering.cave;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.mikm.Vector2Int;
import com.mikm.rendering.cave.ruleCell.RuleCell;
import com.mikm.rendering.cave.ruleCell.RuleCellMetadata;
import com.mikm.rendering.cave.ruleCell.RuleCellMetadataReader;
import com.mikm.rendering.cave.ruleCell.RuleCellTiledMapTileLayer;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.CaveScreen;

import java.util.ArrayList;

public class CaveTilemap {
    public static final int MAP_WIDTH = 130, MAP_HEIGHT = 130;
    final static int FILL_CELL_PERCENT_CHANCE = 52;

    private TextureRegion[] wallImages;
    private TextureRegion floorImage;

    private boolean[][] ruleCellPositions;
    private boolean useWallCell1 = false;

    private RuleCell ruleCell;
    private final CaveTilemapEntitySpawner spawner;
    private final RuleCellPositionGenerator ruleCellPositionGenerator;

    private final RuleCellMetadata ruleCellMetadata;
    public TiledMap tiledMap;
    private RuleCellTiledMapTileLayer ruleCellTiledMapTileLayer;
    private TiledMapTileLayer floorAndWallLayer;
    private final ArrayList<Vector2Int> wallPositions = new ArrayList<>();
    private TiledMapTileLayer.Cell floorCell;
    private final CaveScreen caveScreen;
    private TiledMapTileLayer.Cell[] wallCellTypes;

    public CaveTilemap(CaveScreen caveScreen) {
        this.caveScreen = caveScreen;

        ruleCellMetadata = readMetadata();
        ruleCell = new RuleCell(caveScreen.caveTilesetRecolors.get(0), ruleCellMetadata);
        createImages(0);

        ruleCellPositionGenerator = new RuleCellPositionGenerator();
        ruleCellPositions = ruleCellPositionGenerator.createRuleCellPositions();
        createTiledMap();

        spawner = new CaveTilemapEntitySpawner(caveScreen, ruleCellPositions);
        spawner.spawnRocks();
        spawner.spawnEnemies();
    }


    private void createTiledMap() {
        ruleCellTiledMapTileLayer = new RuleCellTiledMapTileLayer(MAP_WIDTH, MAP_HEIGHT, Application.TILE_WIDTH, Application.TILE_HEIGHT);
        fillRuleCellLayerFromRuleCellPositions();

        floorAndWallLayer = new TiledMapTileLayer(MAP_WIDTH, MAP_HEIGHT, Application.TILE_WIDTH, Application.TILE_HEIGHT);
        fillInFloors();
        fillInWalls();
        createMapFromLayers();
    }

    private RuleCellMetadata readMetadata() {
        RuleCellMetadataReader metadataReader = new RuleCellMetadataReader();
        return metadataReader.createMetadataFromFile("images/caveTiles.meta.txt");
    }

    private void createImages(int color) {
        wallImages = new TextureRegion[5];
        System.arraycopy(caveScreen.caveTilesetRecolors.get(color)[2], 0, wallImages, 0, 4);
        wallImages[4] = caveScreen.caveTilesetRecolors.get(color)[1][2];
        floorImage = ruleCell.spritesheet[2][4];
    }

    private void createMapFromLayers() {
        tiledMap = new TiledMap();
        MapLayers mapLayers = tiledMap.getLayers();
        mapLayers.add(floorAndWallLayer);
        mapLayers.add(ruleCellTiledMapTileLayer);
    }

    private void fillRuleCellLayerFromRuleCellPositions() {
        for (int y = MAP_HEIGHT - 1; y >= 0; y--) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                if (ruleCellPositions[y][x]) {
                    ruleCellTiledMapTileLayer.setRuleCell(x, y, ruleCell);
                }
            }
        }
        ruleCellTiledMapTileLayer.updateRuleCells();
    }

    private void fillInFloors() {
        floorCell = new TiledMapTileLayer.Cell();
        StaticTiledMapTile floorTile = new StaticTiledMapTile(floorImage);
        floorCell.setTile(floorTile);
        for (int y = MAP_HEIGHT - 1; y >= 0; y--) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                floorAndWallLayer.setCell(x, y, floorCell);
            }
        }
    }

    private void fillInWalls() {
        wallCellTypes = new TiledMapTileLayer.Cell[5];
        for (int i = 0; i < 5; i++) {
            wallCellTypes[i] = new TiledMapTileLayer.Cell();
            wallCellTypes[i].setTile(new StaticTiledMapTile(wallImages[i]));
        }
        for (int y = MAP_HEIGHT - 1; y >= 0; y--) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                checkIfShouldFillWallAt(y, x, wallCellTypes);
            }
        }
    }

    private void checkIfShouldFillWallAt(int y, int x, TiledMapTileLayer.Cell[] wallCellTypes) {
        boolean isOutOfBounds = (y+1 > MAP_HEIGHT - 1);
        if (isOutOfBounds) {
            return;
        }
        if (ruleCellPositions[y+1][x]&&!ruleCellPositions[y][x]) {
            wallPositions.add(new Vector2Int(x, y));
            floorAndWallLayer.setCell(x, y, getCorrectWallCell(y, x, wallCellTypes));
        }
    }

    private TiledMapTileLayer.Cell getCorrectWallCell(int y, int x, TiledMapTileLayer.Cell[] wallCellTypes) {
        if (x - 1 < 0 || !ruleCellPositions[y+1][x-1]) {
            if (x+1 > MAP_WIDTH - 1 || !ruleCellPositions[y+1][x+1]) {
                return wallCellTypes[4];
            }
            return wallCellTypes[0];
        }
        if (x+1 > MAP_WIDTH - 1 || !ruleCellPositions[y+1][x+1]) {
            return wallCellTypes[3];
        }
        useWallCell1 = !useWallCell1;
        if (useWallCell1) {
            return wallCellTypes[1];
        } else {
            return wallCellTypes[2];
        }
    }



    public void generateNewMap() {
        boolean levelIs1MoreThanAMultipleOf5 = (CaveScreen.floor + 1) %5 == 1;
        if (levelIs1MoreThanAMultipleOf5) {
            recolorImagesAndCells();
        }
        clearRuleCellsAndWalls();
        ruleCellPositions = ruleCellPositionGenerator.createRuleCellPositions();
        fillRuleCellLayerFromRuleCellPositions();
        fillInWalls();
        spawner.generateNewMap(ruleCellPositions);
    }

    private void recolorImagesAndCells() {
        int recolorLevel = (CaveScreen.floor+1)/5;
        ruleCell = new RuleCell(caveScreen.caveTilesetRecolors.get(recolorLevel), ruleCellMetadata);
        createImages(recolorLevel);
        StaticTiledMapTile floorTile = new StaticTiledMapTile(floorImage);
        floorCell.setTile(floorTile);
        for (int i = 0; i < 5; i++) {
            wallCellTypes[i].setTile(new StaticTiledMapTile(wallImages[i]));
        }
    }

    private void clearRuleCellsAndWalls() {
        for (int y = MAP_HEIGHT - 1; y >= 0; y--) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                ruleCellTiledMapTileLayer.setCell(x, y, null);
            }
        }
        ruleCellTiledMapTileLayer.ruleCells = new RuleCell[MAP_HEIGHT][MAP_WIDTH];

        for (Vector2Int wallPosition : wallPositions) {
            floorAndWallLayer.setCell(wallPosition.x, wallPosition.y, floorCell);
        }
        wallPositions.clear();
    }

    public ArrayList<Vector2Int> getOpenTilePositionsArray() {
        return spawner.openTilePositions;
    }

    public boolean[][] getIsCollidableGrid() {
        return spawner.getIsCollidableGrid();
    }
}