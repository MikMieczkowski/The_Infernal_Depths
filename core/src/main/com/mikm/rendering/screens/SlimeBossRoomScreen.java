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

    SlimeBossRoomScreen(Application application, CaveScreen caveScreen, TextureAtlas textureAtlas) {
        super(application, textureAtlas);
        TextureRegion floorImage = caveScreen.caveTilesetRecolors.get(0)[2][4];
        TiledMapTileLayer.Cell floorCell = new TiledMapTileLayer.Cell();
        floorCell.setTile(new StaticTiledMapTile(floorImage));
        TiledMapTileLayer floorLayer = new TiledMapTileLayer(100, 100, Application.TILE_WIDTH, Application.TILE_HEIGHT);
        for (int x = 0; x < 100; x++) {
            for (int y = 0; y < 100; y++) {
                floorLayer.setCell(x, y, floorCell);
            }
        }
        tiledMap = new TiledMap();
        tiledMap.getLayers().add(floorLayer);

        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1);

        TextureRegion slimeBossImage = textureAtlas.findRegion("slimeBoss").split(32, 32)[0][0];
        EntityActionSpritesheets sparseActionSpritesheet = new EntityActionSpritesheets();
        sparseActionSpritesheet.hit = slimeBossImage;
        addInanimateEntity(new SlimeBoss(200, 200, slimeBossImage, sparseActionSpritesheet));
    }

    @Override
    public boolean[][] getIsCollidableGrid() {
        return new boolean[50][50];
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(CaveScreen.caveFillColorLevel1);
        super.render(delta);
    }
}
