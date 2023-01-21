package com.mikm.rendering.screens;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mikm.Vector2Int;
import com.mikm.entities.animation.ActionSpritesheetsAllDirections;
import com.mikm.entities.animation.AnimationsAlphabeticalIndex;
import com.mikm.entities.animation.EntityActionSpritesheets;
import com.mikm.rendering.Camera;
import com.mikm.rendering.TextureAtlasUtils;
import com.mikm.rendering.cave.CaveTilemap;

import java.util.ArrayList;

public class CaveScreen extends GameScreen {

    public static final Color caveFillColorLevel1 = new Color(41/255f, 16/255f, 16/255f, 1);
    public static final Color caveFillColorLevel6 = new Color(20/255f, 19/255f, 39/255f, 1);
    private final Color[] caveFillColors = new Color[]{caveFillColorLevel1, caveFillColorLevel6};

    CaveTilemap caveTilemap;

    public ArrayList<TextureRegion[][]> caveTilesetRecolors = new ArrayList<>();
    public static TextureRegion[][] rockImages;
    public static TextureRegion[] oreImages;
    public static TextureRegion[] holeImages;

    public EntityActionSpritesheets slimeActionSpritesheets;
    private Music caveSong;

    public static int floor = 0;

    CaveScreen(Application application, Music caveSong, TextureAtlas textureAtlas) {
        super(application, textureAtlas);

        createImages(textureAtlas);
        createMusic(caveSong);

        createTiledMapRenderer();
    }

    public void increaseFloor() {
        if (Application.currentScreen != application.caveScreen) {
            application.setGameScreen(application.caveScreen);
        } else if (floor == 4) {
            application.setGameScreen(application.slimeBossRoomScreen);
            Application.player.x = 100;
            Application.player.y = 100;
            return;
        }
        floor++;
        caveTilemap.generateNewMap();
        application.putPlayerInOpenTile();
    }

    private void createImages(TextureAtlas textureAtlas) {
        caveTilesetRecolors.add(textureAtlas.findRegion("caveTiles").split(Application.TILE_WIDTH, Application.TILE_HEIGHT));
        caveTilesetRecolors.add(textureAtlas.findRegion("caveTilesLevel5").split(Application.TILE_WIDTH, Application.TILE_HEIGHT));
        rockImages = textureAtlas.findRegion("rocks").split(Application.TILE_WIDTH, Application.TILE_HEIGHT);
        oreImages = textureAtlas.findRegion("ores").split(Application.TILE_WIDTH, Application.TILE_HEIGHT)[0];
        holeImages = textureAtlas.findRegion("holes").split(Application.TILE_WIDTH, Application.TILE_HEIGHT)[0];

        slimeActionSpritesheets = new EntityActionSpritesheets();
        ArrayList<TextureRegion[]> rawSlimeSpritesheets = TextureAtlasUtils.findSplitTextureRegionsStartingWith("Slime", textureAtlas, Application.TILE_WIDTH, Application.TILE_HEIGHT);
        slimeActionSpritesheets.hit = rawSlimeSpritesheets.get(0)[0];
        slimeActionSpritesheets.standing = ActionSpritesheetsAllDirections.createFromSpritesheetRange(rawSlimeSpritesheets, AnimationsAlphabeticalIndex.ENTITY_WALK_STARTING_INDEX, true);
        slimeActionSpritesheets.walking = ActionSpritesheetsAllDirections.createFromSpritesheetRange(rawSlimeSpritesheets, AnimationsAlphabeticalIndex.ENTITY_WALK_STARTING_INDEX);
    }

    private void createMusic(Music caveSong) {
        this.caveSong = caveSong;
        if (Application.PLAY_MUSIC) {
            caveSong.play();
            caveSong.setLooping(true);
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(caveFillColors[CaveScreen.getRecolorLevel()]);
        if (!Application.timestop) {
            super.render(delta);
        } else {
            drawNoUpdate();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        caveSong.dispose();
    }

    private void createTiledMapRenderer() {
        caveTilemap = new CaveTilemap(this);
        tiledMap = caveTilemap.tiledMap;

        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1);
        tiledMapRenderer.setView(Camera.orthographicCamera);
    }

    public ArrayList<Vector2Int> getOpenTilePositionsArray() {
        return caveTilemap.getOpenTilePositionsArray();
    }

    @Override
    public boolean[][] getIsCollidableGrid() {
        return caveTilemap.getIsCollidableGrid();
    }

    public static int getRecolorLevel() {
        return floor/5;
    }
}
