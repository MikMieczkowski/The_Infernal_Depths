package com.mikm.rendering.screens;

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
import com.mikm.entities.enemies.slimeBoss.SlimeBoss;

public class SlimeBossRoomScreen extends GameScreen {
    private final int MAP_WIDTH = 25, MAP_HEIGHT = 25;
    private boolean[][] collidableGrid;

    SlimeBossRoomScreen() {
        super();

        tiledMap = new TmxMapLoader().load("SlimeBoss.tmx");

        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1);
        collidableGrid = readCollisionTiledmapLayer(2, MAP_WIDTH,MAP_HEIGHT);

        createMusic(Assets.getInstance().getAsset("sound/hubba_bubba.mp3", Music.class));
        addEntity(new SlimeBoss(this, 200, 200));
    }

    @Override
    public boolean[][] isCollidableGrid() {
        return collidableGrid;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(CaveScreen.caveFillColorLevel6);
        super.render(delta);
    }

    @Override
    public Vector2 getInitialPlayerPosition() {
        return new Vector2(96,96);
    }
}
