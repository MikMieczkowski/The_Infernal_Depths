package com.mikm.rendering.screens;


import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mikm.entities.Entity;
import com.mikm.entities.RemovableArray;
import com.mikm.entities.player.Player;
import com.mikm.rendering.Camera;

public abstract class GameScreen extends ScreenAdapter {
    public ScreenViewport viewport;

    Application application;
    TextureAtlas textureAtlas;
    public Camera camera;

    public RemovableArray<Entity> entities;
    public Player player;
    public OrthogonalTiledMapRenderer tiledMapRenderer;
    public TiledMap tiledMap;
    public ShapeRenderer debugShapeRenderer = new ShapeRenderer();

    public static TextureRegion shadowImage;

    GameScreen(Application application, TextureAtlas textureAtlas) {
        this.textureAtlas = textureAtlas;
        this.application = application;
        this.player = application.player;
        shadowImage = textureAtlas.findRegion("shadow").split(Application.TILE_WIDTH,Application.TILE_HEIGHT)[0][0];

        camera = new Camera(player);
        viewport = new ScreenViewport(Camera.orthographicCamera);
        viewport.setUnitsPerPixel(Camera.VIEWPORT_ZOOM);
        entities = new RemovableArray<>();
    }

    public abstract boolean[][] getCollidableTilePositions();

    abstract void drawAssets();

    @Override
    public void resize (int width, int height) {
        int realWidth = width%2==0?width : width - 1;
        int realHeight = height%2==0?height : height -1;
        viewport.update(realWidth, realHeight, true);
    }

    @Override
    public void dispose() {
        tiledMapRenderer.dispose();
        tiledMap.dispose();
    }
}
