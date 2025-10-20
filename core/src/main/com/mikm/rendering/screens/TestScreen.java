package com.mikm.rendering.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.mikm.Assets;
import com.mikm.DeltaTime;
import com.mikm.entities.Entity;
import com.mikm.rendering.cave.RockType;

public class TestScreen extends GameScreen {
    private TextureRegion lock;
    private int x = 80*16;
    public float rotation;
    public TestScreen() {
        tiledMap = new TmxMapLoader().load("TestScreen.tmx");

        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1);

        Entity test1 = addEntity("rat", x-2*16, x - 2*16);

        Application.player.equippedWeapon = Application.player.weaponInstances.swords[Application.player.swordLevel];
        Application.player.swordLevel++;
        Application.player.currentHeldItem = Application.player.equippedWeapon;
        Application.player.lockedIn = test1;
        lock = Assets.getInstance().getTextureRegion("lock");
    }
    @Override
    public boolean[][] isCollidableGrid() {
        return new boolean[160][160];
    }

    @Override
    protected void drawAssetsPostEntities() {
        super.drawAssetsPostEntities();
        rotation += DeltaTime.deltaTime();
        Application.batch.draw(lock, Application.player.lockedIn.getHitbox().x-lock.getRegionWidth()/2f, Application.player.lockedIn.getHitbox().y-lock.getRegionHeight()/2f,
                lock.getRegionWidth()/2, lock.getRegionHeight()/2, lock.getRegionWidth(), lock.getRegionHeight(), 1, 1, rotation);
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
