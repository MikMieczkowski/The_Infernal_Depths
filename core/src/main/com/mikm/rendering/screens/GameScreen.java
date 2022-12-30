package com.mikm.rendering.screens;


import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mikm.entities.player.Player;
import com.mikm.rendering.Camera;

public abstract class GameScreen extends ScreenAdapter {
    Application application;
    TextureAtlas textureAtlas;
    Camera camera;

    public Stage stage;
    public Player player;
    public OrthogonalTiledMapRenderer tiledMapRenderer;
    public TiledMap tiledMap;

    GameScreen(Application application, TextureAtlas textureAtlas) {
        this.textureAtlas = textureAtlas;
        this.application = application;
        this.player = application.player;

        camera = new Camera(player);
        stage = new Stage(new ScreenViewport(camera.orthographicCamera));
    }

    public abstract int[] getCollidableTiledMapTileLayerIDs();

    abstract void drawAssets();

    @Override
    public void resize (int width, int height) {
        stage.getViewport().update(width%2==0?width : width - 1, height%2==0?height : height -1, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        textureAtlas.dispose();
        tiledMapRenderer.dispose();
        tiledMap.dispose();
    }
}
