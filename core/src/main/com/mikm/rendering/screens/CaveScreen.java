package com.mikm.rendering.screens;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mikm.Vector2Int;
import com.mikm.rendering.tilemap.CaveTilemap;

import java.util.ArrayList;

public class CaveScreen extends GameScreen {

    private final Color caveWallColor = new Color(41/255f, 16/255f, 16/255f, 1);
    CaveTilemap caveTilemap;

    public TextureRegion[][] caveTileset;
    public TextureRegion[][] rockImages;
    public ArrayList<TextureRegion[]> slimeSpritesheet;
    private Music caveSong;

    CaveScreen(Application application, Music caveSong, TextureAtlas textureAtlas) {
        super(application, textureAtlas);

        createImages(textureAtlas);
        createMusic(caveSong);

        createTiledMapRenderer();
        caveTilemap.spawnEnemies();

    }

    private void createImages(TextureAtlas textureAtlas) {
        caveTileset = textureAtlas.findRegion("caveTiles").split(Application.TILE_WIDTH, Application.TILE_HEIGHT);
        rockImages = textureAtlas.findRegion("rocks").split(Application.TILE_WIDTH, Application.TILE_HEIGHT);

        slimeSpritesheet = new ArrayList<>();
        TextureRegion[] slimeSpritesheetSplit = textureAtlas.findRegion("slime").split(Application.TILE_WIDTH, Application.TILE_HEIGHT)[0];
        slimeSpritesheet.add(new TextureRegion[]{slimeSpritesheetSplit[0]});
        slimeSpritesheet.add(new TextureRegion[]{slimeSpritesheetSplit[0], slimeSpritesheetSplit[1]});
        slimeSpritesheet.add(new TextureRegion[]{slimeSpritesheetSplit[2], slimeSpritesheetSplit[3], slimeSpritesheetSplit[4]});
    }

    private void createMusic(Music caveSong) {
        this.caveSong = caveSong;
        if (Application.playMusic) {
            caveSong.play();
            caveSong.setLooping(true);
        }
    }

    @Override
    public int[] getCollidableTiledMapTileLayerIDs() {
        return new int[]{1, 2};
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(caveWallColor);
        application.batch.begin();
        application.batch.disableBlending();
        camera.update();
        application.batch.setProjectionMatrix(camera.orthographicCamera.combined);
        //omitting stage.getBatch().setProjectionMatrix(camera.orthographicCamera.combined);
        tiledMapRenderer.setView(camera.orthographicCamera);
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

    public ArrayList<Vector2Int> getOpenTilePositions() {
        return caveTilemap.getOpenTilePositions();
    }
}
