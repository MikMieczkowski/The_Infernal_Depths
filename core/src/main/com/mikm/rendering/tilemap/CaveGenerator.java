package com.mikm.rendering.tilemap;

import com.mikm.rendering.tilemap.ruleCell.RuleCell;
import com.mikm.rendering.tilemap.ruleCell.RuleCellTiledMapTileLayer;

import java.util.Random;

public class CaveGenerator {
    private final int mapWidth = 80, mapHeight = 60;
    private final int randomFillPercent = 50;
    private final long seed = 21;
    private Random random;
    boolean[][] isCellAt;

    public CaveGenerator() {
        random = new Random();
        isCellAt = new boolean[mapHeight][mapWidth];
    }

    public RuleCellTiledMapTileLayer generateMap(RuleCell ruleCell) {
        fillMapRandomly();
        for (int i = 0; i < 5; i++)
        {
            smoothMap();
        }
        return createRuleCellTiledMapTileLayer(ruleCell);
    }



    private void fillMapRandomly() {
        for (int y = mapHeight - 1; y >= 0; y--) {
            for (int x = 0; x < mapWidth; x++) {
                if (x == 0 || x == mapWidth - 1 || y == 0 || y == mapHeight - 1) {
                    isCellAt[y][x] = true;
                } else {
                    isCellAt[y][x] = (random.nextInt(100) < randomFillPercent);
                }
            }
        }
    }

    private void smoothMap() {
        for (int y = mapHeight - 1; y >= 0; y--) {
            for (int x = 0; x < mapWidth; x++) {
                int neighboringWallCount = getNeighboringCellCount(y, x);
                if (neighboringWallCount > 4) {
                    isCellAt[y][x] = true;
                } else if (neighboringWallCount < 4){
                    isCellAt[y][x] = false;
                }
            }
        }
    }

    private RuleCellTiledMapTileLayer createRuleCellTiledMapTileLayer(RuleCell ruleCell) {
        RuleCellTiledMapTileLayer tileLayer = new RuleCellTiledMapTileLayer(mapWidth, mapHeight, 16, 16);
        for (int y = mapHeight - 1; y >= 0; y--) {
            for (int x = 0; x < mapWidth; x++) {
                if (isCellAt[y][x]) {
                    tileLayer.setRuleCell(x, y, ruleCell);
                }
            }
        }
        tileLayer.updateRuleCells();
        return tileLayer;
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
                if (isCellAt[y + i][x + j]) {
                    count++;
                }
            }
        }
        return count;
    }
}