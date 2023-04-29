package com.mikm.rendering.screens;


import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mikm.debug.DebugRenderer;
import com.mikm.entities.Entity;
import com.mikm.entities.InanimateEntity;
import com.mikm.entities.RemovableArray;
import com.mikm.entities.Shadow;
import com.mikm.rendering.Camera;

public abstract class GameScreen extends ScreenAdapter {
    public static ScreenViewport viewport;

    Application application;
    TextureAtlas textureAtlas;
    Music song;
    public static Camera camera;

    public RemovableArray<Entity> entities;
    public final RemovableArray<InanimateEntity> inanimateEntities = new RemovableArray<>(InanimateEntity.class);

    public OrthogonalTiledMapRenderer tiledMapRenderer;
    public TiledMap tiledMap;

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

        entities = new RemovableArray<>(Entity.class);
        entities.add(Application.player);
        addPlayerShadow();
    }

    public void addPlayerShadow() {
        Shadow playerShadow = new Shadow(Application.player);
        Application.player.shadow = playerShadow;
        inanimateEntities.add(playerShadow);
    }

    public abstract boolean[][] isWallAt();

    @Override
    public void render(float delta) {
        camera.update();
        Application.batch.begin();
        Application.batch.setProjectionMatrix(Camera.orthographicCamera.combined);
        tiledMapRenderer.setView(Camera.orthographicCamera);
        drawAssets();
        DebugRenderer.getInstance().update();
        Camera.renderLighting(Application.batch);
        Camera.updateOrthographicCamera();
        Application.batch.end();
    }

    public void drawNoUpdate() {
        Application.batch.begin();
        tiledMapRenderer.render();
        inanimateEntities.draw(Application.batch);
        entities.draw(Application.batch);
        Camera.renderLighting(Application.batch);
        Application.batch.end();
    }

    public void playSong() {
        if (Application.PLAY_MUSIC) {
            song.play();
        }
    }

    public void stopSong() {
        if (Application.PLAY_MUSIC) {
            song.stop();
        }
    }

    void createMusic(Music song) {
        this.song = song;
        song.setLooping(true);
    }

    void drawAssets() {
        tiledMapRenderer.render();
        inanimateEntities.render(Application.batch);
        entities.render(Application.batch);
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
        if (song != null) {
            song.dispose();
        }
    }
}
