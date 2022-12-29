package com.mikm.rendering.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mikm.rendering.tilemap.CaveTilemap;

public class CaveScreen extends GameScreen {

    private final Color caveWallColor = new Color(41/255f, 16/255f, 16/255f, 1);
    public final TextureRegion[][] caveTileset;
    public final TextureRegion[][] rockImages;
    private final Music caveSong;

    CaveTilemap caveTilemap;

    CaveScreen(Application application, TextureAtlas textureAtlas) {
        super(application, textureAtlas);

        caveTileset = textureAtlas.findRegion("caveTiles").split(Application.defaultTileWidth, Application.defaultTileHeight);
        rockImages = textureAtlas.findRegion("rocks").split(Application.defaultTileWidth, Application.defaultTileHeight);
        caveSong = Gdx.audio.newMusic(Gdx.files.internal("sound/caveTheme.mp3"));
        caveSong.play();
        caveSong.setLooping(true);

        createTiledMapRenderer();
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
        caveSong.dispose();
    }

    private void createTiledMapRenderer() {
        caveTilemap = new CaveTilemap(this);
        tiledMap = caveTilemap.createTiledMap();
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1);
    }
}
