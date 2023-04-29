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
import com.mikm.rendering.cave.CaveEntitySpawner;
import com.mikm.rendering.cave.CaveTilemapCreator;
import com.mikm.rendering.cave.CaveFloorMemento;

import java.util.ArrayList;

public class CaveScreen extends GameScreen {

    public static final Color caveFillColorLevel1 = new Color(41/255f, 16/255f, 16/255f, 1);
    public static final Color caveFillColorLevel6 = new Color(20/255f, 19/255f, 39/255f, 1);
    private final Color[] caveFillColors = new Color[]{caveFillColorLevel1, caveFillColorLevel6};
    public static TextureRegion[][] rockImages;
    public static TextureRegion[] oreImages;
    public static int floor = 0;
    public static final int LAST_FLOOR = 14;
    public static final int FLOORS_PER_LEVEL = 5;

    public ArrayList<TextureRegion[][]> caveTilesetRecolors = new ArrayList<>();
    public TextureRegion[][] holeSpritesheet;
    public EntityActionSpritesheets slimeActionSpritesheets;


    public CaveTilemapCreator caveTilemapCreator;
    private CaveEntitySpawner spawner;
    //5,10,15 are always null.
    public CaveFloorMemento[] caveFloorMementos = new CaveFloorMemento[15];

    CaveScreen(Application application, Music caveSong, TextureAtlas textureAtlas) {
        super(application, textureAtlas);
        createImages(textureAtlas);
        createMusic(caveSong);
        createTiledMapRenderer();
    }

    public void decreaseFloor() {
        floor--;
        handleScreenChange();
        if (floor % 5 == 0) {
            return;
        }

        Application.player.x = caveFloorMementos[floor].spawnPosition.x;
        Application.player.y = caveFloorMementos[floor].spawnPosition.y;
    }

    public void increaseFloor() {
        floor++;
        handleScreenChange();
        if (floor % 5 == 0) {
            return;
        }

        if (caveFloorMementos[floor] == null) {
            generateNewFloor();
            Vector2Int position = putPlayerInOpenTile();
            CaveFloorMemento memento = CaveFloorMemento.create(position, caveTilemapCreator.ruleCellPositions, caveTilemapCreator.holePositions, inanimateEntities, entities);
            caveFloorMementos[floor] = memento;
        } else {
            CaveFloorMemento currentMemento = caveFloorMementos[floor];
            Application.player.x = currentMemento.spawnPosition.x;
            Application.player.y = currentMemento.spawnPosition.y;
        }
    }

    private void handleScreenChange() {
        if (Application.currentScreen != Application.caveScreen) {
            application.setGameScreen(Application.caveScreen);
        } else if (floor == 5) {
            application.setGameScreen(application.slimeBossRoomScreen);
            Application.player.x = 100;
            Application.player.y = 100;
        }
    }

    public void generateNewFloor() {
        caveTilemapCreator.generateNewMap();
        spawner= new CaveEntitySpawner(this);
        spawner.generateNewEnemies(caveTilemapCreator);
    }

    public void activate(CaveFloorMemento memento) {
        caveTilemapCreator.activate(memento);
        spawner.activate(memento);
    }

    public Vector2Int putPlayerInOpenTile() {
        Vector2Int playerPosition = caveTilemapCreator.getSpawnablePosition();
        Application.player.x = playerPosition.x;
        Application.player.y = playerPosition.y;
        Camera.setPositionDirectlyToPlayerPosition();
        return playerPosition;
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

    private void createTiledMapRenderer() {
        caveTilemapCreator = new CaveTilemapCreator(this);
        tiledMap = caveTilemapCreator.tiledMap;

        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1);
        tiledMapRenderer.setView(Camera.orthographicCamera);
    }

    @Override
    public boolean[][] isWallAt() {
        return caveTilemapCreator.getIsCollidableGrid();
    }

    public boolean[][] getHolePositionsToCheck() {
        return caveTilemapCreator.holePositionsToCheckGrid;
    }

    public static int getRecolorLevel() {
        return floor/5;
    }
}
