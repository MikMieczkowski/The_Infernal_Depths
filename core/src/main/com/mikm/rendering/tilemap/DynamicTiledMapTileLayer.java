package com.mikm.rendering.tilemap;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

public class DynamicTiledMapTileLayer extends TiledMapTileLayer {
    DynamicCell[][] dynamicCells;

    public DynamicTiledMapTileLayer(int width, int height, int tileWidth, int tileHeight) {
        super(width, height, tileWidth, tileHeight);
        dynamicCells = new DynamicCell[width][height];
    }

    public void setDynamicCell(int x, int y, DynamicCell dynamicCell) {
        dynamicCells[x][y] = dynamicCell;
        Cell outputStaticCell = new Cell();
        //This is where you use the DynamicCellMetadata info to check for neighboring tiles

        super.setCell(x, y, outputStaticCell);
    }
}
