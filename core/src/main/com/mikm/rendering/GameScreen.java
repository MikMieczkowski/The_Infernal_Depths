package com.mikm.rendering;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mikm.entities.player.Player;

public abstract class GameScreen extends ScreenAdapter {
    Application application;
    Stage stage;
    AssetManager assetManager;
    OrthographicCamera camera;

    Player player;
    OrthogonalTiledMapRenderer tiledMapRenderer;
    TiledMap tiledMap;

    GameScreen(Application application, AssetManager assetManager) {
        this.assetManager = assetManager;
        this.application = application;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.zoom = .5f;
        stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera));
    }
    
    void drawAssets() {
        tiledMapRenderer.render();
        stage.draw();
    }

    @Override
    public void resize (int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        assetManager.dispose();
        tiledMapRenderer.dispose();
        tiledMap.dispose();
    }
}
