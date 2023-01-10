package com.mikm.rendering.screens;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mikm.Vector2Int;
import com.mikm.entities.RemovableArray;
import com.mikm.entities.animation.ActionSpritesheetsAllDirections;
import com.mikm.entities.animation.AnimationsAlphabeticalIndex;
import com.mikm.entities.animation.EntityActionSpritesheets;
import com.mikm.rendering.Camera;
import com.mikm.rendering.TextureAtlasUtils;
import com.mikm.rendering.tilemap.CaveTilemap;
import com.mikm.rendering.tilemap.Rock;

import java.util.ArrayList;

public class CaveScreen extends GameScreen {
    public final RemovableArray<Rock> rocks = new RemovableArray<>();

    private final Color caveWallColor = new Color(41/255f, 16/255f, 16/255f, 1);
    CaveTilemap caveTilemap;

    public TextureRegion[][] caveTileset;
    public TextureRegion[][] rockImages;
    public EntityActionSpritesheets slimeActionSpritesheets;
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

        slimeActionSpritesheets = new EntityActionSpritesheets();
        ArrayList<TextureRegion[]> rawSlimeSpritesheets = TextureAtlasUtils.findSplitTextureRegionsStartingWith("Slime", textureAtlas, Application.TILE_WIDTH, Application.TILE_HEIGHT);
        slimeActionSpritesheets.hit = ActionSpritesheetsAllDirections.createFromSpritesheetRange(rawSlimeSpritesheets, AnimationsAlphabeticalIndex.ENTITY_HIT_STARTING_INDEX);
        slimeActionSpritesheets.standing = ActionSpritesheetsAllDirections.createFromSpritesheetRange(rawSlimeSpritesheets, AnimationsAlphabeticalIndex.ENTITY_WALK_STARTING_INDEX, true);
        slimeActionSpritesheets.walking = ActionSpritesheetsAllDirections.createFromSpritesheetRange(rawSlimeSpritesheets, AnimationsAlphabeticalIndex.ENTITY_WALK_STARTING_INDEX);
    }

    private void createMusic(Music caveSong) {
        this.caveSong = caveSong;
        if (Application.playMusic) {
            caveSong.play();
            caveSong.setLooping(true);
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(caveWallColor);
        application.batch.begin();
        //application.batch.disableBlending();
        camera.update();
        application.batch.setProjectionMatrix(Camera.orthographicCamera.combined);
        tiledMapRenderer.setView(Camera.orthographicCamera);
        drawAssets();
        application.batch.end();
    }

    @Override
    void drawAssets() {
        tiledMapRenderer.render();
        rocks.render(application.batch);
        entities.render(application.batch);
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
        tiledMapRenderer.setView(Camera.orthographicCamera);
    }

    public ArrayList<Vector2Int> getOpenTilePositions() {
        return caveTilemap.getOpenTilePositions();
    }

    @Override
    public boolean[][] getCollidableTilePositions() {
        return caveTilemap.getCollidableTilePositions();
    }
}
