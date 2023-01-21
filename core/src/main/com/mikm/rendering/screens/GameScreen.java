package com.mikm.rendering.screens;


import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mikm.entities.RemovableArray;
import com.mikm.entities.Entity;
import com.mikm.entities.InanimateEntity;
import com.mikm.entities.Shadow;
import com.mikm.rendering.Camera;

public abstract class GameScreen extends ScreenAdapter {
    public static ScreenViewport viewport;

    Application application;
    TextureAtlas textureAtlas;
    public static Camera camera;

    public RemovableArray<Entity> entities;
    public final RemovableArray<InanimateEntity> inanimateEntities = new RemovableArray<>();

    public OrthogonalTiledMapRenderer tiledMapRenderer;
    public TiledMap tiledMap;
    public ShapeRenderer debugShapeRenderer = new ShapeRenderer();

    public static TextureRegion shadowImage;
    public static TextureRegion[][] particleImages;

    GameScreen(Application application, TextureAtlas textureAtlas) {
        this.textureAtlas = textureAtlas;
        this.application = application;
        shadowImage = textureAtlas.findRegion("shadow").split(Application.TILE_WIDTH,Application.TILE_HEIGHT)[0][0];
        particleImages = textureAtlas.findRegion("particles").split(8,8);
        camera = new Camera();
        viewport = new ScreenViewport(Camera.orthographicCamera);
        viewport.setUnitsPerPixel(Camera.VIEWPORT_ZOOM);

        entities = new RemovableArray<>();
        entities.add(Application.player);
        Shadow playerShadow = new Shadow(Application.player);
        Application.player.shadow = playerShadow;
        inanimateEntities.add(playerShadow);
    }

    public abstract boolean[][] getIsCollidableGrid();

    @Override
    public void render(float delta) {
        application.batch.begin();
        camera.update();
        application.batch.setProjectionMatrix(Camera.orthographicCamera.combined);
        tiledMapRenderer.setView(Camera.orthographicCamera);
        drawAssets();
        Camera.renderLighting(application.batch);
        application.batch.end();
    }

    public void drawNoUpdate() {
        application.batch.begin();
        tiledMapRenderer.render();
        inanimateEntities.draw(application.batch);
        entities.draw(application.batch);
        Camera.renderLighting(application.batch);
        application.batch.end();
    }

    void drawAssets() {
        tiledMapRenderer.render();
        inanimateEntities.render(application.batch);
        entities.render(application.batch);
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
        if (entity.hasShadow()) {
            Shadow shadow = new Shadow(entity);
            entity.shadow = shadow;
            inanimateEntities.add(shadow);
        }
    }

    public void addEntityInstantly(Entity entity) {
        entities.addInstantly(entity);
        if (entity.hasShadow()) {
            Shadow shadow = new Shadow(entity);
            entity.shadow = shadow;
            inanimateEntities.addInstantly(shadow);
        }
    }

    public void addInanimateEntity(InanimateEntity entity) {
        inanimateEntities.add(entity);
        if (entity.hasShadow()) {
            Shadow shadow = new Shadow(entity);
            entity.shadow = shadow;
            inanimateEntities.add(shadow);
        }
    }

    public void addInanimateEntityInstantly(InanimateEntity entity) {
        inanimateEntities.addInstantly(entity);
        if (entity.hasShadow()) {
            Shadow shadow = new Shadow(entity);
            entity.shadow = shadow;
            inanimateEntities.addInstantly(shadow);
        }
    }

    public void removeEntity(Entity entity) {
        entities.remove(entity);
        inanimateEntities.remove(entity.shadow);
    }

    public void removeInanimateEntity(InanimateEntity entity) {
        inanimateEntities.remove(entity);
        inanimateEntities.remove(entity.shadow);
    }

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
