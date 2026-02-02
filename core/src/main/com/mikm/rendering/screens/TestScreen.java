package com.mikm.rendering.screens;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.mikm.entities.prefabLoader.PrefabInstantiator;
import com.mikm.utils.Assets;
import com.mikm.utils.DeltaTime;

public class TestScreen extends GameScreen {
    private TextureRegion lock;
    private int x = 80*16;
    public float rotation;
    public TestScreen() {
        tiledMap = new TmxMapLoader().load("tiled/TestScreen.tmx");

        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1);

        Entity test1 = PrefabInstantiator.addEntity("rat", this, x-2*16, x - 2*16);

        lock = Assets.getInstance().getTextureRegion("lock");

    }
    @Override
    public boolean[][] isCollidableGrid() {
        return new boolean[160][160];
    }

    @Override
    protected void drawAssetsPostEntities() {
        super.drawAssetsPostEntities();
        rotation += DeltaTime.deltaTimeMultiplier();
        //Application.batch.draw(lock, Application.playerOLD.lockedIn.getHitbox().x-lock.getRegionWidth()/2f, Application.playerOLD.lockedIn.getHitbox().y-lock.getRegionHeight()/2f,
        //        lock.getRegionWidth()/2, lock.getRegionHeight()/2, lock.getRegionWidth(), lock.getRegionHeight(), 1, 1, rotation);
    }

    @Override
    public int getMapWidth() {
        return 160;
    }

    @Override
    public int getMapHeight() {
        return 160;
    }

    @Override
    public Vector2 getInitialPlayerPosition() {
        return new Vector2(x, x);
    }
}
