package com.mikm.rendering.screens;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mikm.Assets;
import com.mikm.Vector2Int;
import com.mikm.rendering.Camera;
import com.mikm.rendering.cave.CaveEntitySpawner;
import com.mikm.rendering.cave.CaveFloorMemento;
import com.mikm.rendering.cave.CaveTilemapCreator;

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

    public CaveTilemapCreator caveTilemapCreator;
    private CaveEntitySpawner spawner;
    //5,10,15 are always null.
    public CaveFloorMemento[] caveFloorMementos = new CaveFloorMemento[15];

    CaveScreen(Application application) {
        super(application);
        createImages();
        createMusic(Assets.getInstance().getAsset("sound/caveTheme.mp3", Music.class));
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
            //activate memento?
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

    private void createImages() {
        caveTilesetRecolors.add(Assets.getInstance().getSplitTextureRegion("caveTiles"));
        caveTilesetRecolors.add(Assets.getInstance().getSplitTextureRegion("caveTilesLevel5"));
        rockImages = Assets.getInstance().getSplitTextureRegion("rocks");
        oreImages = Assets.getInstance().getSplitTextureRegion("ores")[0];
        holeSpritesheet = Assets.getInstance().getSplitTextureRegion("holes");
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
