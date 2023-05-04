package com.mikm.rendering.screens;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mikm.Assets;
import com.mikm.entities.enemies.slimeBoss.SlimeBoss;

public class SlimeBossRoomScreen extends GameScreen {
    private final int MAP_WIDTH = 30, MAP_HEIGHT = 30;

    SlimeBossRoomScreen(Application application) {
        super(application);
        TextureRegion floorImage = Application.caveScreen.caveTilesetRecolors.get(0)[2][4];
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

        createMusic(Assets.getInstance().getAsset("sound/hubba_bubba.mp3", Music.class));
        addEntity(new SlimeBoss(this, 200, 200));
    }

    @Override
    public boolean[][] isWallAt() {
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
