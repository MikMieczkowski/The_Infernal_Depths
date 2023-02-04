package com.mikm.rendering.screens;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mikm.entities.animation.EntityActionSpritesheets;
import com.mikm.entities.enemies.slimeBoss.SlimeBoss;

public class SlimeBossRoomScreen extends GameScreen {
    private final int MAP_WIDTH = 30, MAP_HEIGHT = 30;

    SlimeBossRoomScreen(Application application, CaveScreen caveScreen, TextureAtlas textureAtlas) {
        super(application, textureAtlas);
        TextureRegion floorImage = caveScreen.caveTilesetRecolors.get(0)[2][4];
        TiledMapTileLayer.Cell floorCell = new TiledMapTileLayer.Cell();
        floorCell.setTile(new StaticTiledMapTile(floorImage));
        TiledMapTileLayer floorLayer = new TiledMapTileLayer(MAP_WIDTH, MAP_HEIGHT, Application.TILE_WIDTH, Application.TILE_HEIGHT);
        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                floorLayer.setCell(x, y, floorCell);
            }
        }
        tiledMap = new TiledMap();
        tiledMap.getLayers().add(floorLayer);

        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1);

        TextureRegion slimeBossImage = textureAtlas.findRegion("slimeBoss").split(32, 32)[0][0];
        EntityActionSpritesheets sparseActionSpritesheet = new EntityActionSpritesheets();
        sparseActionSpritesheet.hit = slimeBossImage;
        addEntity(new SlimeBoss(this, 200, 200, slimeBossImage, sparseActionSpritesheet));
    }

    @Override
    public boolean[][] getIsCollidableGrid() {
        return new boolean[MAP_WIDTH][MAP_HEIGHT];
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(CaveScreen.caveFillColorLevel1);
        if (!Application.timestop) {
            super.render(delta);
        } else {
            drawNoUpdate();
        }
    }
}
