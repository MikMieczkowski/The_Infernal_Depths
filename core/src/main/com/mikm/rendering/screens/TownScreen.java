package com.mikm.rendering.screens;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mikm.DeltaTime;
import com.mikm.entities.NPC;
import com.mikm.rendering.Camera;

public class TownScreen extends GameScreen {

    TownScreen(Application application, Music townSong, TextureAtlas textureAtlas) {
        super(application, textureAtlas);
        tiledMap = new TmxMapLoader().load("Overworld.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1);
        tiledMapRenderer.setView(Camera.orthographicCamera);
        createMusic(townSong);

        addInanimateEntity(new NPC(Application.player.entityActionSpritesheets.standing.list.get(0)[0], 50, 50));
    }


    @Override
    public void render(float delta) {
        camera.update();
        ScreenUtils.clear(Color.BLACK);
        Application.batch.setProjectionMatrix(Camera.orthographicCamera.combined);
        Application.batch.begin();
        tiledMapRenderer.setView(Camera.orthographicCamera);
        drawAssets();
        Camera.updateOrthographicCamera();
        Application.font.draw(Application.batch, String.valueOf(DeltaTime.deltaTime()), 50, 50);
        Application.batch.end();
    }

    @Override
    public boolean[][] isWallAt() {
        return new boolean[100][100];
    }

    @Override
    void drawAssets() {
        super.drawAssets();
    }
}
