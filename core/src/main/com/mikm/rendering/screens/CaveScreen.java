package com.mikm.rendering.screens;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mikm.ExtraMathUtils;
import com.mikm.Vector2Int;
import com.mikm.entities.Rope;
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
    public TextureRegion[][] holeSpritesheet;

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
        increaseFloor(1);
    }

    public void increaseFloor(int increment) {
        if (increment > 0) {
            Vector2 playerTileCoordinates = ExtraMathUtils.toTileCoordinates(Application.player.x, Application.player.y);
            addInanimateEntity(new Rope(playerTileCoordinates.x * Application.TILE_WIDTH, playerTileCoordinates.y * Application.TILE_HEIGHT));
        }
        if (Application.currentScreen != Application.caveScreen) {
            application.setGameScreen(Application.caveScreen);
        } else if (floor + increment == 5) {
            application.setGameScreen(application.slimeBossRoomScreen);
            Application.player.x = 100;
            Application.player.y = 100;
            return;
        }
        floor += increment;
        caveTilemap.generateNewMap();

        //application.putPlayerInOpenTile();
    }

    private void createImages(TextureAtlas textureAtlas) {
        caveTilesetRecolors.add(textureAtlas.findRegion("caveTiles").split(Application.TILE_WIDTH, Application.TILE_HEIGHT));
        caveTilesetRecolors.add(textureAtlas.findRegion("caveTilesLevel5").split(Application.TILE_WIDTH, Application.TILE_HEIGHT));
        rockImages = textureAtlas.findRegion("rocks").split(Application.TILE_WIDTH, Application.TILE_HEIGHT);
        oreImages = textureAtlas.findRegion("ores").split(Application.TILE_WIDTH, Application.TILE_HEIGHT)[0];
        holeSpritesheet = textureAtlas.findRegion("holes").split(Application.TILE_WIDTH, Application.TILE_HEIGHT);

        slimeActionSpritesheets = new EntityActionSpritesheets();
        ArrayList<TextureRegion[]> rawSlimeSpritesheets = TextureAtlasUtils.findSplitTextureRegionsStartingWith("Slime", textureAtlas, Application.TILE_WIDTH, Application.TILE_HEIGHT);
        slimeActionSpritesheets.hit = rawSlimeSpritesheets.get(0)[0];
        slimeActionSpritesheets.standing = ActionSpritesheetsAllDirections.createFromSpritesheetRange(rawSlimeSpritesheets, AnimationsAlphabeticalIndex.ENTITY_WALK_STARTING_INDEX, true);
        slimeActionSpritesheets.walking = ActionSpritesheetsAllDirections.createFromSpritesheetRange(rawSlimeSpritesheets, AnimationsAlphabeticalIndex.ENTITY_WALK_STARTING_INDEX);
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
        return caveTilemap.openTiles;
    }

    public void generateNewMap() {
        caveTilemap.generateNewMap();
    }

    @Override
    public boolean[][] getIsCollidableGrid() {
        return caveTilemap.getIsCollidableGrid();
    }

    public boolean[][] getHolePositionsGrid() {
        return caveTilemap.holePositionsGrid;
    }

    public static int getRecolorLevel() {
        return floor/5;
    }
}
