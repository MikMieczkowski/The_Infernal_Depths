package com.mikm.rendering.screens;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mikm._components.CombatComponent;
import com.mikm.utils.Assets;
import com.mikm.entities.actions.DamagedAction;
import com.mikm.entities.prefabLoader.PrefabInstantiator;
import com.mikm.rendering.Camera;

public class MotiScreen extends GameScreen {
    private boolean[][] collidableGrid;
    private float nextRoomTimer = 0;
    private float NEXT_ROOM_WAIT_TIME = 3;
    private boolean gameCompleted = false;
    //TODO add back
    //public ArrayList<Grave> graves = new ArrayList<>();
    private Entity moti;

    MotiScreen() {
        super();

        tiledMap = new TmxMapLoader().load("tiled/Moti.tmx");

        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1);
        collidableGrid = readCollisionTiledmapLayer(2, getMapWidth(),getMapHeight());

        createMusic(Assets.getInstance().getAsset("sound/webbedSong.mp3", Music.class));
        moti = PrefabInstantiator.addEntity("moti", this, 50, 50);
    }

    @Override
    public boolean[][] isCollidableGrid() {
        return collidableGrid;
    }

    @Override
    public void render(float delta) {
        if (gameCompleted) {
            renderWinScreen();
            return;
        }
        ScreenUtils.clear(CaveScreen.caveFillColorLevel6);
        super.render(delta);
        if (CombatComponent.MAPPER.get(moti).dead) {
            nextRoomTimer += Gdx.graphics.getDeltaTime();
            if (nextRoomTimer > NEXT_ROOM_WAIT_TIME) {
                nextRoomTimer = 0;
                gameCompleted = true;
            }
        }
    }

    private void renderWinScreen() {
        ScreenUtils.clear(Color.BLACK);
        super.lockCameraAt(79, 66);
        float x = Camera.orthographicCamera.position.x;
        float y = Camera.orthographicCamera.position.y;
        float w = Camera.VIEWPORT_ZOOM* Gdx.graphics.getWidth();
        float h = Camera.VIEWPORT_ZOOM* Gdx.graphics.getHeight();
        float imgW = w-10;
        float imgH = 0;
        x-= imgW/2;
        y-= imgH/2;
        Application.batch.begin();
        Application.batch.setProjectionMatrix(Camera.orthographicCamera.combined);
        Assets.font.draw(Application.batch, "You win! Thank you for playing this silly game that I made.", x, y);
        Camera.orthographicCamera.update();
        Application.batch.end();
    }

    @Override
    public void onEnter() {
        resetMoti();
    }

    private void resetMoti() {
        removeEntity(moti);
        //TODO add moti prefab
        moti = PrefabInstantiator.addEntity("moti", Application.getInstance().motiScreen,
                getMapWidth() * Application.TILE_WIDTH /2 +48, getMapHeight() * Application.TILE_HEIGHT /2 + 48);
    }

    @Override
    public Vector2 getInitialPlayerPosition() {
        return new Vector2(96,96);
    }

    @Override
    public int getMapWidth() {
        return 30;
    }

    @Override
    public int getMapHeight() {
        return 30;
    }
}
