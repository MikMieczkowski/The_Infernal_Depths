package com.mikm.rendering.cave.ruleCell;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.mikm.Vector2Int;

import java.util.Map;

public class RuleCellTiledMapTileLayer extends TiledMapTileLayer {
    public RuleCell[][] ruleCells;

    public RuleCellTiledMapTileLayer(int width, int height, int tileWidth, int tileHeight) {
        super(width, height, tileWidth, tileHeight);
        ruleCells = new RuleCell[height][width];
    }

    public void setRuleCell(int x, int y, RuleCell ruleCell) {
        ruleCells[y][x] = ruleCell;
    }

    //Will not handle cases where one ruleset has 'either' and the other has a more specific rule, and the ruleset is otherwise identical.
    public void updateRuleCells() {
        for (int y = getHeight() - 1; y >= 0; y--) {
            for (int x = 0; x < getWidth(); x++) {
                if (ruleCells[y][x] != null) {
                    updateCellAt(y, x);
                }
            }
        }
    }

    private void updateCellAt(int y, int x) {
        RuleCellMetadata metadata = ruleCells[y][x].metadata;
        TileRuleset currentComparisons = createCurrentComparisonsAt(y, x);

        for (Map.Entry<Vector2Int, TileRuleset[]> entry : metadata.tilesMetadata.entrySet()) {
            Vector2Int positionInImage = entry.getKey();
            TileRuleset[] tileRulesets = entry.getValue();
            for (TileRuleset tileRuleset : tileRulesets) {
                if (currentComparisons.fits(tileRuleset)) {
                    setCellAtXYtoMatchingImage(y, x, positionInImage, tileRuleset);
                    return;
                }
            }
        }
    }

    private TileRuleset createCurrentComparisonsAt(int y, int x) {
        CellPresence[][] outputArray = new CellPresence[3][3];
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) {
                    outputArray[i+1][j+1] = CellPresence.Either;
                    continue;
                }
                boolean outOfBounds = (x+j < 0 || x+j > getWidth() - 1 || y+i < 0 || y+i > getHeight() - 1);
                if (outOfBounds) {
                    outputArray[i+1][j+1] = CellPresence.Full;
                    continue;
                }
                //using == for address comparison
                if (ruleCells[y + i][x + j] == ruleCells[y][x]) {
                    outputArray[i+1][j+1] = CellPresence.Full;
                } else {
                    outputArray[i+1][j+1] = CellPresence.Empty;
                }
            }
        }
        return new TileRuleset(outputArray);
    }

    private void setCellAtXYtoMatchingImage(int y, int x, Vector2Int positionInImage, TileRuleset tileRuleset) {
        TextureRegion textureRegion = ruleCells[y][x].spritesheet[positionInImage.y][positionInImage.x];

        StaticTiledMapTile staticTiledMapTile = new StaticTiledMapTile(textureRegion);
        Cell staticCell = createStaticCell(tileRuleset);

        staticCell.setTile(staticTiledMapTile);
        //libgdx tileMapRenderer uses mathematical coordinates instead of java array coordinates
        super.setCell(x, y, staticCell);
    }

    private Cell createStaticCell(TileRuleset tileRuleset) {
        Cell staticCell = new Cell();
        //Align our rotation numbers with libgdx's by subtracting it from 4
        staticCell.setRotation(4 - tileRuleset.rotation);
        staticCell.setFlipHorizontally(tileRuleset.flippedHorizontally);
        staticCell.setFlipVertically(tileRuleset.flippedVertically);
        return staticCell;
    }
}
