package com.mikm.rendering;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mikm.entities.player.Player;
import com.mikm.rendering.tilemap.DynamicCell;
import com.mikm.rendering.tilemap.DynamicCellMetadata;
import com.mikm.rendering.tilemap.DynamicCellMetadataReader;
import com.mikm.rendering.tilemap.DynamicTiledMapTileLayer;

public class CaveScreen extends Screen {
    private Player player;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private TiledMap tiledMap;
    Texture caveTileset;
    CaveScreen(Application application, AssetManager assetManager) {
        super(application, assetManager);

        caveTileset = assetManager.get("images/caveTiles.png", Texture.class);
        TextureRegion temporaryImage = new TextureRegion(caveTileset, 0, 0, 16, 16);

        createTiledMapRenderer(caveTileset);


        player = new Player(150, 150, temporaryImage);
        stage.addActor(player.group);

    }

    @Override
    public void render(float delta) {
        application.batch.begin();
        ScreenUtils.clear(Color.DARK_GRAY);
        drawAssets();
        application.batch.end();
    }

    private void drawAssets() {
        stage.draw();
        tiledMapRenderer.render();
    }

    @Override
    public void dispose() {
        super.dispose();
        tiledMapRenderer.dispose();
        tiledMap.dispose();
    }

    private void createTiledMapRenderer(Texture caveTileset) {
        tiledMap = new TiledMap();
        MapLayers mapLayers = tiledMap.getLayers();
        DynamicTiledMapTileLayer tiledMapTileLayer = new DynamicTiledMapTileLayer(16, 16, 16, 16);


        //DynamicCell dynamicCaveCell = createCaveDynamicCell();
//        for (int x = 0; x < 16; x++) {
//            for (int y = 0; y < 16; y++) {
//                tiledMapTileLayer.setDynamicCell(x, y, dynamicCaveCell);
//            }
//        }

        mapLayers.add(tiledMapTileLayer);
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 2);
        tiledMapRenderer.setView(camera);
    }

    private DynamicCell createCaveDynamicCell() {
        DynamicCellMetadata metadata = DynamicCellMetadataReader.createMetadataFromFile("images/caveTiles.meta.txt");
        DynamicCell dynamicCell = new DynamicCell(caveTileset, metadata);
        return dynamicCell;
    }
}
