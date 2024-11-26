package com.mikm.rendering.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.esotericsoftware.kryo.io.KryoBufferUnderflowException;
import com.mikm.Assets;
import com.mikm.ExtraMathUtils;
import com.mikm.Vector2Int;
import com.mikm.entities.Grave;
import com.mikm.entities.InanimateEntity;
import com.mikm.entities.Rope;
import com.mikm.entities.enemies.slimeBoss.SlimeBoss;
import com.mikm.input.GameInput;
import com.mikm.rendering.Camera;
import com.mikm.rendering.cave.CaveEntitySpawner;
import com.mikm.rendering.cave.CaveFloorMemento;
import com.mikm.rendering.cave.CaveTilemapCreator;
import com.mikm.serialization.Serializer;

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
    public CaveFloorMemento[] caveFloorMementos;

    public boolean displayButtonIndicator = false;
    public Vector2 buttonIndicatorPosition;
    public Vector2Int currentRopePosition = Vector2Int.ZERO;
    private TextureRegion ropePointer = Assets.getInstance().getTextureRegion("ropePointer");
    private final int POINTER_RADIUS = 80;

    CaveScreen() {
        super();
        createImages();
        createMusic(Assets.getInstance().getAsset("sound/caveTheme.mp3", Music.class));
        createTiledMapRenderer();
        caveFloorMementos = new CaveFloorMemento[10];
        for (int i = 0; i < 10; i++) {
            try {
                caveFloorMementos[i] = Serializer.getInstance().read(CaveFloorMemento.class, i);
            } catch (KryoBufferUnderflowException e) {
                caveFloorMementos[i] = null;
            }
        }
        spawner= new CaveEntitySpawner(this);
    }

    public void decreaseFloor() {
        entities.doAfterRender(() -> {
            updateCurrentMemento();
            floor--;
            Application.player.startInvincibilityFrames();
            handleScreenChange(-1);
            if (floor % 5 == 0) {
                return;
            }
            loadFloor(floor);
        });
    }

    public void increaseFloor() {
        entities.doAfterRender(() -> {
            updateCurrentMemento();
            floor++;
            Application.player.startInvincibilityFrames();
            handleScreenChange(1);
            if (floor % 5 == 0) {
                return;
            }
            if (caveFloorMementos[floor - 1] == null) {
                generateNewFloor();
                Vector2Int position = putPlayerInOpenTile();
                Vector2Int ropePosition = caveTilemapCreator.getSpawnablePosition();
                ropePosition = new Vector2Int(ropePosition.x + 8, ropePosition.y+8);
                currentRopePosition = ropePosition;
                inanimateEntities.addInstantly(new Rope(ropePosition.x, ropePosition.y));
                CaveFloorMemento memento = CaveFloorMemento.create(position, ropePosition, caveTilemapCreator.ruleCellPositions, caveTilemapCreator.holePositions, inanimateEntities, entities);
                caveFloorMementos[floor - 1] = memento;
            } else {
                loadFloor(floor);
            }
        });
    }

    public void updateCurrentMemento() {
        if (CaveScreen.floor % 5 != 0) {
            caveFloorMementos[CaveScreen.floor - 1] = CaveFloorMemento.create(caveFloorMementos[CaveScreen.floor - 1].spawnPosition, caveFloorMementos[CaveScreen.floor - 1].ropePosition, caveFloorMementos[CaveScreen.floor - 1].ruleCellPositions, caveFloorMementos[CaveScreen.floor - 1].holePositions, inanimateEntities, caveFloorMementos[CaveScreen.floor - 1].enemies);

        }
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
        super.render(delta);
    }

    @Override
    public void renderUI() {
        if (displayButtonIndicator) {
            Application.batch.draw(GameInput.getTalkButtonImage(), buttonIndicatorPosition.x, buttonIndicatorPosition.y);
        }
        super.renderUI();
        drawPointer(currentRopePosition.x, currentRopePosition.y);
        if (floor %5!= 0) {
            for (int i = 0; i < caveFloorMementos[floor - 1].graves.size(); i++) {
                InanimateEntity g = caveFloorMementos[floor - 1].graves.get(i);
                drawPointer(g.x, g.y);
            }
        }
    }

    private void drawPointer(float x, float y) {
        float distanceToRope = ExtraMathUtils.distance(Camera.getCenterOfScreenWorldCoordinates().x, Camera.getCenterOfScreenWorldCoordinates().y, x, y);
        if (distanceToRope > Gdx.graphics.getWidth() * Camera.VIEWPORT_ZOOM / 2) {
            float angle = MathUtils.atan2(y - Camera.getCenterOfScreenWorldCoordinates().y, x - Camera.getCenterOfScreenWorldCoordinates().x);
            float screenMultiplier = Gdx.graphics.getWidth() / 1440f;
            drawComponentOnEdge(ropePointer, 4, 1, screenMultiplier * POINTER_RADIUS * MathUtils.cos(angle), screenMultiplier * POINTER_RADIUS * MathUtils.sin(angle), MathUtils.radDeg * angle);
        }
    }


    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public Vector2 getInitialPlayerPosition() {
        //should not have effect on player position
        return new Vector2(0,0);
    }

    private void generateNewFloor() {
        caveTilemapCreator.generateNewMap();
        spawner= new CaveEntitySpawner(this);
        spawner.generateNewEnemies(caveTilemapCreator);
    }

    private void loadFloor(int floor) {
        if (floor <= 0) {
            Application.getInstance().setGameScreen(Application.getInstance().townScreen);
            return;
        }
        CaveFloorMemento currentMemento = caveFloorMementos[floor-1];
        activate(currentMemento);
        Application.player.x = currentMemento.spawnPosition.x;
        Application.player.y = currentMemento.spawnPosition.y;
    }

    private void handleScreenChange(int i) {
        if (floor <= 0) {
            Application.getInstance().setGameScreen(Application.getInstance().townScreen);
        } else if (Application.getInstance().currentScreen != Application.getInstance().caveScreen) {
            Application.getInstance().setGameScreen(Application.getInstance().caveScreen);
        } else if (floor == 5) {
            if (!SlimeBoss.defeated) {
                Application.getInstance().setGameScreen(Application.getInstance().slimeBossRoomScreen);
            } else {
                floor += i;
                Application.getInstance().setGameScreen(Application.getInstance().caveScreen);
            }
        } else if (floor == 10) {
            Application.getInstance().setGameScreen(Application.getInstance().motiScreen);
        }
    }

    private void activate(CaveFloorMemento memento) {
        caveTilemapCreator.activate(memento);
        spawner.activate(memento);
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
    public boolean[][] isCollidableGrid() {
        return caveTilemapCreator.getIsCollidableGrid();
    }

    public boolean[][] getHolePositionsToCheck() {
        return caveTilemapCreator.holePositionsToCheckGrid;
    }

    public static int getRecolorLevel() {
        return floor/5;
    }

    @Override
    public int getMapWidth() {
        return 80;
    }

    @Override
    public int getMapHeight() {
        return 80;
    }
}
