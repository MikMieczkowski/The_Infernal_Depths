package com.mikm.rendering.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mikm.Assets;
import com.mikm.entities.enemies.moti.Moti;
import com.mikm.rendering.Camera;

public class MotiScreen extends GameScreen {
    private final int MAP_WIDTH = 30, MAP_HEIGHT = 30;
    private boolean[][] collidableGrid;
    private Moti moti;
    private float nextRoomTimer = 0;
    private float NEXT_ROOM_WAIT_TIME = 3;
    private boolean gameCompleted = false;

    MotiScreen() {
        super();

        tiledMap = new TmxMapLoader().load("Moti.tmx");

        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1);
        collidableGrid = readCollisionTiledmapLayer(2, MAP_WIDTH,MAP_HEIGHT);

        createMusic(Assets.getInstance().getAsset("sound/webbedSong.mp3", Music.class));
        moti = new Moti(this, 50, 50);
        addEntity(moti);
    }

    @Override
    public boolean[][] isCollidableGrid() {
        return collidableGrid;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(CaveScreen.caveFillColorLevel6);
        if (!gameCompleted) {
            super.render(delta);
            if (moti.damagedState.dead) {
                nextRoomTimer += Gdx.graphics.getDeltaTime();
                if (nextRoomTimer > NEXT_ROOM_WAIT_TIME) {
                    nextRoomTimer = 0;
                    gameCompleted = true;
                }
            }
        } else {
            ScreenUtils.clear(Color.BLACK);
            Camera.x = 79;
            Camera.y = 66;
            Camera.orthographicCamera.position.set(Camera.x, Camera.y, 0);
            Camera.orthographicCamera.update();
            float x = Camera.orthographicCamera.position.x;
            float y = Camera.orthographicCamera.position.y;
            float w = Camera.VIEWPORT_ZOOM* Gdx.graphics.getWidth();
            float h = Camera.VIEWPORT_ZOOM* Gdx.graphics.getHeight();
            float imgW = 200;
            float imgH = 30;
            x-= imgW/2;
            y-= imgH/2;
            Application.batch.begin();
            Application.batch.setProjectionMatrix(Camera.orthographicCamera.combined);
            Assets.font.draw(Application.batch, "You win! Thank you for playing this silly game that I made.", x, y);
            Camera.orthographicCamera.update();
            Application.batch.end();
        }
    }

    @Override
    public Vector2 getInitialPlayerPosition() {
        return new Vector2(96,96);
    }
}
