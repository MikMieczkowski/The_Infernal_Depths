package com.mikm.rendering.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mikm.rendering.tilemap.CaveLevelGenerator;

public class CaveScreen extends GameScreen {

    private final Color caveWallColor = new Color(41/255f, 16/255f, 16/255f, 1);
    public final TextureRegion[][] caveTileset;
    public final TextureRegion[][] rockImages;

    CaveLevelGenerator caveLevelGenerator;

    CaveScreen(Application application, TextureAtlas textureAtlas) {
        super(application, textureAtlas);

        caveTileset = textureAtlas.findRegion("caveTiles").split(Application.defaultTileWidth, Application.defaultTileHeight);
        rockImages = textureAtlas.findRegion("rocks").split(Application.defaultTileWidth, Application.defaultTileHeight);

        createTiledMapRenderer();
        stage.addActor(player.group);
    }

    @Override
    public int[] getCollidableTiledMapTileLayerIDs() {
        return new int[]{1, 2};
    }

    @Override
    public void render(float delta) {
        application.batch.begin();
        ScreenUtils.clear(caveWallColor);
        tiledMapRenderer.setView(camera.orthographicCamera);
        camera.update();
        drawAssets();
        application.batch.end();
    }

    @Override
    void drawAssets() {
        tiledMapRenderer.render();
        stage.draw();
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    private void createTiledMapRenderer() {
        caveLevelGenerator = new CaveLevelGenerator(this);
        tiledMap = caveLevelGenerator.createTiledMap();
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1);
    }

    public boolean canPlayerSpawnAt(int x, int y) {
        return caveLevelGenerator.playerCanSpawnAt(x, y);
    }
}
