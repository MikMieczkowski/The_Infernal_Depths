package com.mikm.rendering.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mikm.entities.NPC;
import com.mikm.rendering.Camera;

public class TownScreen extends GameScreen {
    TownScreen(Application application, TextureAtlas textureAtlas) {
        super(application, textureAtlas);
        tiledMap = new TmxMapLoader().load("Overworld.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1);
        tiledMapRenderer.setView(Camera.orthographicCamera);

        addInanimateEntity(new NPC(Application.player.entityActionSpritesheets.standing.list.get(0)[0], 50, 50));
    }

    @Override
    public boolean[][] getIsCollidableGrid() {
        return new boolean[100][100];
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        application.batch.begin();
        camera.update();
        application.batch.setProjectionMatrix(Camera.orthographicCamera.combined);
        tiledMapRenderer.setView(Camera.orthographicCamera);
        drawAssets();
        Application.font.getData().setScale(1f);
        Application.font.draw(application.batch, String.valueOf(Application.player.money), 50, 50);
        application.batch.end();
    }

    @Override
    void drawAssets() {
        super.drawAssets();
    }
}
