package com.mikm.rendering.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mikm.Assets;
import com.mikm.entities.Entity;
import com.mikm.entities.Grave;
import com.mikm.entities.enemies.slimeBoss.SlimeBoss;
import com.mikm.rendering.cave.RockType;

import java.util.ArrayList;

public class SlimeBossRoomScreen extends GameScreen {
    private final int MAP_WIDTH = 25, MAP_HEIGHT = 25;
    private boolean[][] collidableGrid;
    private Entity slimeBoss;
    private float nextRoomTimer = 0;
    private float NEXT_ROOM_WAIT_TIME = 3;

    public ArrayList<Grave> graves = new ArrayList<>();

    SlimeBossRoomScreen() {
        super();

        tiledMap = new TmxMapLoader().load("SlimeBoss.tmx");

        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1);
        collidableGrid = readCollisionTiledmapLayer(2, MAP_WIDTH,MAP_HEIGHT);

        createMusic(Assets.getInstance().getAsset("sound/hubba_bubba.mp3", Music.class));
    }

    @Override
    public boolean[][] isCollidableGrid() {
        return collidableGrid;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(CaveScreen.caveFillColorLevel6);
        super.render(delta);
        if (slimeBoss.damagedState.dead) {
            nextRoomTimer+= Gdx.graphics.getDeltaTime();
            if (nextRoomTimer>NEXT_ROOM_WAIT_TIME) {
                nextRoomTimer = 0;
                entities.doAfterRender(()-> {
                    RockType.get(1).increaseOreAmount(3);
                    RockType.get(2).increaseOreAmount(3);
                    RockType.get(3).increaseOreAmount(3);
                    Application.getInstance().setGameScreen(Application.getInstance().townScreen);
                    CaveScreen.floor = 0;
                });
            }
        }
    }

    @Override
    public void onEnter() {
        resetInanimateAndAnimateEntities();
    }

    private void resetInanimateAndAnimateEntities() {
        entities.removeInstantly(Application.player);
        entities.clear();
        inanimateEntities.clear();
        entities.addInstantly(Application.player);
        inanimateEntities.addAll(graves);
        slimeBoss = new SlimeBoss(this, 200, 200);
        entities.addInstantly(slimeBoss);
        addPlayerShadow();
    }

    @Override
    public Vector2 getInitialPlayerPosition() {
        return new Vector2(96,96);
    }
}
