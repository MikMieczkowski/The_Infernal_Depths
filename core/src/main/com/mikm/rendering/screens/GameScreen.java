package com.mikm.rendering.screens;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mikm.Assets;
import com.mikm.DeltaTime;
import com.mikm.debug.DebugRenderer;
import com.mikm.entities.Entity;
import com.mikm.entities.InanimateEntity;
import com.mikm.entities.RemovableArray;
import com.mikm.entities.Shadow;
import com.mikm.rendering.Camera;
import com.mikm.rendering.cave.RockType;

public abstract class GameScreen extends ScreenAdapter {
    public static ScreenViewport viewport;
    Music song;
    public static Camera camera;

    public RemovableArray<Entity> entities;
    public final RemovableArray<InanimateEntity> inanimateEntities = new RemovableArray<>();

    public OrthogonalTiledMapRenderer tiledMapRenderer;
    public TiledMap tiledMap;


    private TextureRegion hpBar, hpBarBottom;
    private TextureRegion[][] health;
    private float healthAnimationTimer, healthAnimationFrameDuration = .1f;



    GameScreen() {
        camera = new Camera();
        viewport = new ScreenViewport(Camera.orthographicCamera);
        viewport.setUnitsPerPixel(Camera.VIEWPORT_ZOOM);
        hpBar = Assets.getInstance().getTextureRegion("hpBar", 16, 80);
        hpBarBottom = Assets.getInstance().getTextureRegion("hpBarBottom", 16, 80);
        health = Assets.getInstance().getSplitTextureRegion("health", 16, 8);

        entities = new RemovableArray<>();
        entities.add(Application.player);
        addPlayerShadow();
    }

    public void addPlayerShadow() {
        Shadow playerShadow = new Shadow(Application.player);
        Application.player.shadow = playerShadow;
        inanimateEntities.add(playerShadow);
    }

    boolean[][] readCollisionTiledmapLayer(int layer, int w, int h) {
        boolean[][] output = new boolean[h][w];
        TiledMapTileLayer l = (TiledMapTileLayer)tiledMap.getLayers().get(layer);
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (l.getCell(i, j) != null) {
                    output[j][i] = true;
                }
            }
        }
        return output;
    }

    public abstract boolean[][] isCollidableGrid();

    @Override
    public void render(float delta) {
        if (!Application.getInstance().timestop) {
            camera.update();
            Application.batch.begin();
            Application.batch.setProjectionMatrix(Camera.orthographicCamera.combined);
            tiledMapRenderer.setView(Camera.orthographicCamera);
            drawAssets();
            DebugRenderer.getInstance().update();
            Camera.renderLighting(Application.batch);
            Camera.updateOrthographicCamera();
            renderUI();
            Application.batch.end();
        } else {
            drawNoUpdate();
        }
    }

    public void renderUI() {
        Application.batch.setShader(null);
        for (int i = 1; i < Assets.particleImages[0].length; i++) {
            if (RockType.get(i).oreAmount != 0) {
                drawComponentOnEdge(Assets.particleImages[1][i], false, false, true, 2, -5, (i-1)*17+5);
                drawComponentOnEdge(Assets.numbers[MathUtils.clamp(RockType.get(i).oreAmount, 0, 9)], false, false, true, 1, -3, (i - 1) * 17 + 3);
            }
        }
        drawComponentOnEdge(hpBar, false, true, true, 1, -4, 1);
        healthAnimationTimer += Gdx.graphics.getDeltaTime();
        int f = (int)(healthAnimationTimer/healthAnimationFrameDuration);
        if (f >= health[0].length) {
            healthAnimationTimer = 0;
            f=0;
        }
        for (int i = 0; i < Application.player.hp+1; i++) {
            drawComponentOnEdge(health[(Application.player.hp-i)%10][f], false, true, true, 1, -4, 1+i*8);
        }
        drawComponentOnEdge(hpBarBottom, false, true, true, 1, -4, 1);
    }

    public void drawComponentOnEdge(TextureRegion image, boolean centerY, boolean left, boolean down, int mul, int xOffset, int yOffset) {
        float x = Camera.orthographicCamera.position.x;
        float y = Camera.orthographicCamera.position.y;
        float w = Camera.VIEWPORT_ZOOM* Gdx.graphics.getWidth();
        float h = Camera.VIEWPORT_ZOOM* Gdx.graphics.getHeight();
        float imgW = image.getRegionWidth()*mul;
        float imgH = image.getRegionHeight()*mul;
        if (!centerY) {
            if (down) {
                y -= h / 2;
            } else {
                y += h / 2 - imgH;
            }
        } else {
            y-= imgH/2;
        }

        if (left) {
            x -= w / 2;
        } else {
            x += w / 2 - imgW;
        }

        Application.batch.draw(image, x + xOffset, y + yOffset, imgW, imgH);
    }


    public void drawNoUpdate() {
        camera.update();
        Application.batch.begin();
        Application.batch.setProjectionMatrix(Camera.orthographicCamera.combined);
        tiledMapRenderer.setView(Camera.orthographicCamera);
        tiledMapRenderer.render();
        inanimateEntities.draw(Application.batch);
        entities.draw(Application.batch);
        Camera.renderLighting(Application.batch);
        Camera.updateOrthographicCamera();
        renderUI();
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
        song.setVolume(.75f);
        song.setLooping(true);
    }

    public void onEnter() {

    }

    public void onExit() {

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

    public abstract Vector2 getInitialPlayerPosition();
}
