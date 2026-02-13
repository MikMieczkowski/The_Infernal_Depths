package com.mikm.rendering.screens;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.esotericsoftware.kryo.io.KryoBufferUnderflowException;
import com.mikm._components.*;
import com.mikm._components.GraveComponent;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm.utils.Assets;
import com.mikm.utils.ExtraMathUtils;
import com.mikm.Vector2Int;
import com.mikm.input.GameInput;
import com.mikm.rendering.Camera;
import com.mikm.rendering.cave.CaveEntitySpawner;
import com.mikm.rendering.cave.CaveFloorMemento;
import com.mikm.rendering.cave.CaveTilemapCreator;
import com.mikm.rendering.sound.SoundEffects;
import com.mikm.serialization.Serializer;
import com.mikm.utils.debug.DebugRenderer;

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

    public Vector2Int currentRopePosition = Vector2Int.ZERO;
    private TextureRegion pointer = Assets.getInstance().getTextureRegion("ropePointer");
    private TextureRegion ropeImg = Assets.getInstance().getTextureRegion("rope");
    private TextureRegion graveImg = Assets.getInstance().getTextureRegion("grave");
    private final int POINTER_RADIUS = 80;

    CaveScreen() {
        super();
        createImages();
        createMusic(Assets.getInstance().getAsset("sound/caveTheme.mp3", Music.class));
        createTiledMapRenderer();
    }

    public void init() {
        caveFloorMementos = new CaveFloorMemento[10];
        for (int i = 0; i < 10; i++) {
            try {
                caveFloorMementos[i] = Serializer.getInstance().read(CaveFloorMemento.class, i);
            } catch (KryoBufferUnderflowException e) {
                caveFloorMementos[i] = null;
            }
        }
        spawner= new CaveEntitySpawner(this);

        //Load all 9 floors and store into mementos
        for (int i = 1; i <= CaveScreen.LAST_FLOOR - 5; i++) {
            CaveScreen.floor = i;
            caveTilemapCreator.generateNewMap();
            //TODO memento refactor - move to CaveFloorMemento class
            Vector2Int position = caveTilemapCreator.getSpawnablePosition();
            Vector2Int ropePosition = caveTilemapCreator.getSpawnablePosition();
            ropePosition = new Vector2Int(ropePosition.x + 8, ropePosition.y+8);
            //currentRopePosition = ropePosition;

            //create memento
            CaveFloorMemento memento = CaveFloorMemento.create(position, ropePosition, caveTilemapCreator.ruleCellPositions, caveTilemapCreator.holePositions);
            assert memento != null;
            caveFloorMementos[floor - 1] = memento;

            //move outside of loop?
            spawner = new CaveEntitySpawner(this);
            spawner.load(caveTilemapCreator);
        }
        CaveScreen.floor = 0;
    }

    public void decreaseFloor() {
        changeFloor(false);
    }

    public void increaseFloor() {
        changeFloor(true);
    }

    private void changeFloor(boolean up) {
        int i = up ? 1 : -1;
        if (!up && CaveScreen.floor <= 0) {
            return;
        }
        //TODO test this
        Application.getInstance().getPlayerCombatComponent().startInvincibilityFrames();
        floor += i;
        handleScreenChange(i);
        if (floor % 5 == 0) {
            return;
        }
        CaveFloorMemento currentMemento = caveFloorMementos[floor-1];
        activate(currentMemento);
    }


    public void updateCurrentMemento() {
        //only thing that needs to be updated is grave entities and rock entities
        if (CaveScreen.floor % 5 != 0) {
            caveFloorMementos[CaveScreen.floor - 1].updateRocks(engine.getEntitiesFor(Family.all(RockComponent.class).get()));
            caveFloorMementos[CaveScreen.floor - 1].updateGraves(engine.getEntitiesFor(Family.all(GraveComponent.class).get()));
        }
    }

    public Vector2Int putPlayerInOpenTile() {
        Vector2Int playerPosition = caveTilemapCreator.getSpawnablePosition();
        Transform transform = Application.getInstance().getPlayerTransform();
        transform.x = playerPosition.x;
        transform.y = playerPosition.y;
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
        if (Application.getInstance().paused) {
            super.renderUI();
            return;
        }
        super.renderUI();
        drawPointer(currentRopePosition.x, currentRopePosition.y, false);
        if (floor % 5 != 0 && caveFloorMementos[floor - 1] != null) {
            for (com.mikm.entities.prefabLoader.EntityData g : caveFloorMementos[floor - 1].graves) {
                drawPointer(g.pos.x, g.pos.y, true);
            }
        }
    }

    private void drawPointer(float x, float y, boolean grave) {
        TextureRegion img = ropeImg;
        if (grave) {
            img = graveImg;
        }

        float distanceToRope = ExtraMathUtils.distance(Camera.getCenterOfScreenWorldCoordinates().x, Camera.getCenterOfScreenWorldCoordinates().y, x, y);
        if (distanceToRope > Gdx.graphics.getWidth() * Camera.VIEWPORT_ZOOM / 2) {
            float angle = MathUtils.atan2(y - Camera.getCenterOfScreenWorldCoordinates().y, x - Camera.getCenterOfScreenWorldCoordinates().x);
            float screenMultiplier = Gdx.graphics.getWidth() / 1440f;
            drawComponentOnEdge(pointer, 4, 1, screenMultiplier * POINTER_RADIUS * MathUtils.cos(angle), screenMultiplier * POINTER_RADIUS * MathUtils.sin(angle), MathUtils.radDeg * angle);
            int imgRadius = POINTER_RADIUS - 5;
            drawComponentOnEdge(img, 4, 1, screenMultiplier * imgRadius * MathUtils.cos(angle), screenMultiplier * imgRadius * MathUtils.sin(angle), 0);
        
        }
    }

    @Override
    public void onEnter() {
        super.onEnter();
        SoundEffects.stopLoop(BlacksmithScreen.FIRE_AMBIENCE);
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


    private void handleScreenChange(int i) {
        if (floor <= 0) {
            Application.getInstance().setGameScreen(Application.getInstance().townScreen);
        } else if (Application.getInstance().currentScreen != Application.getInstance().caveScreen) {
            Application.getInstance().setGameScreen(Application.getInstance().caveScreen);
        } else if (floor == 5) {
            if (!SlimeBossRoomScreen.slimeBossDefeated) {
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
        System.out.println("floor: " + CaveScreen.floor);
        System.out.println("enemies: " + memento.enemies.size());
        System.out.println("rocks: " + memento.rocks.size());
        caveTilemapCreator.activate(memento);
        spawner.activate(memento);

        CaveFloorMemento currentMemento = caveFloorMementos[floor-1];
        Transform transform = Application.getInstance().getPlayerTransform();
        transform.x = currentMemento.spawnPosition.x;
        transform.y = currentMemento.spawnPosition.y;
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
        return caveTilemapCreator.collidablePositions;
    }

    public boolean[][] getHolePositionsToCheck() {
        return caveTilemapCreator.holePositionsToCheckGrid;
    }

    @Override
    public boolean[][] getHolePositions() {
        return getHolePositionsToCheck();
    }

    public static int getRecolorLevel() {
        return floor/5;
    }

    @Override
    public int getMapWidth() {
        return 60;
    }

    @Override
    public int getMapHeight() {
        return 60;
    }
}
