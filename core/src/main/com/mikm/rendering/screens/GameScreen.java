package com.mikm.rendering.screens;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mikm.Assets;
import com.mikm.DeltaTime;
import com.mikm.debug.DebugRenderer;
import com.mikm.entities.*;
import com.mikm.entities.particles.ParticleTypes;
import com.mikm.rendering.Camera;
import com.mikm.rendering.SoundEffects;
import com.mikm.rendering.cave.RockType;

import java.awt.*;

public abstract class GameScreen extends ScreenAdapter {
    public static ScreenViewport viewport;
    Music song;
    public static Camera camera;

    public RemovableArray<Entity> entities;
    public final RemovableArray<InanimateEntity> inanimateEntities = new RemovableArray<>();

    public OrthogonalTiledMapRenderer tiledMapRenderer;
    public TiledMap tiledMap;


    private final TextureRegion hpBar, hpBarBottom;
    private final TextureRegion[][] health;
    private final TextureRegion pauseMenu;
    private float healthAnimationTimer, healthAnimationFrameDuration = .1f;



    GameScreen() {
        camera = new Camera();
        viewport = new ScreenViewport(Camera.orthographicCamera);
        viewport.setUnitsPerPixel(Camera.VIEWPORT_ZOOM);
        hpBar = Assets.getInstance().getTextureRegion("hpBar", 16, 80);
        hpBarBottom = Assets.getInstance().getTextureRegion("hpBarBottom", 16, 80);
        health = Assets.getInstance().getSplitTextureRegion("health", 16, 8);
        pauseMenu = Assets.getInstance().getTextureRegion("controls", 350, 300);

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

    void readAndCreateDestructiblesTiledmapLayer(int layer, int w, int h) {
        int GRASS = 24;
        int DOUBLEPOT = 20;
        int POT = 18;
        TextureRegion[] imgs = Assets.getInstance().getSplitTextureRegion("destructibles")[0];
        TextureRegion grass = Assets.getInstance().getTextureRegion("grass");
        TextureRegion[][] particleImgs = Assets.getInstance().getSplitTextureRegion("particles",8,8);
        TiledMapTileLayer l = (TiledMapTileLayer)tiledMap.getLayers().get(layer);
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (l.getCell(i,j) == null) {
                    continue;
                }
                if (l.getCell(i, j).getTile().getId() == GRASS) {
                    addInanimateEntity(new Destructible(imgs[2], ParticleTypes.getDestructibleParameters(particleImgs[2][0]), SoundEffects.grassBreak, i*16,j*16));
                } else if (l.getCell(i, j).getTile().getId() == DOUBLEPOT) {
                    addInanimateEntity(new Destructible(imgs[1], ParticleTypes.getDestructibleParameters(particleImgs[0][3]), SoundEffects.potBreak, i*16,j*16));
                } else if (l.getCell(i, j).getTile().getId() == POT) {
                    addInanimateEntity(new Destructible(imgs[0], ParticleTypes.getDestructibleParameters(particleImgs[0][3]), SoundEffects.potBreak, i*16,j*16));
                } else {
                    continue;
                }
                l.getCell(i,j).setTile(new StaticTiledMapTile(grass));
            }
        }
    }

    public abstract boolean[][] isCollidableGrid();

    @Override
    public void render(float delta) {
        if (!Application.getInstance().timestop && !Application.getInstance().paused) {
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
        if (Application.getInstance().paused) {
            renderPauseMenu();
        }
        for (int i = 1; i < Assets.particleImages[0].length; i++) {
            if (RockType.get(i).getOreAmount() != 0) {
                int offset = 15;
                drawComponentOnEdge(Assets.particleImages[1][i], 6, 2, -5+offset, (i - 1) * 17 + 5);
                int ores = MathUtils.clamp(RockType.get(i).getOreAmount(), 0, 99);
                if (ores / 10 != 0) {
                    offset+=4;
                    drawComponentOnEdge(Assets.numbers[ores / 10], 6, 1, -11+offset, (i - 1) * 17 + 3);
                }
                drawComponentOnEdge(Assets.numbers[ores % 10], 6, 1, -3+offset, (i - 1) * 17 + 3);
            }
        }
        drawComponentOnEdge(hpBar, 6, 1, -4, 1);
        healthAnimationTimer += Gdx.graphics.getDeltaTime();
        int f = (int) (healthAnimationTimer / healthAnimationFrameDuration);
        if (f >= health[0].length) {
            healthAnimationTimer = 0;
            f = 0;
        }
        for (int i = 0; i < Application.player.hp + 1; i++) {
            drawComponentOnEdge(health[(Application.player.hp - i) % 10][f], 6, 1, -4, 1 + i * 8);
        }
        drawComponentOnEdge(hpBarBottom, 6, 1, -4, 1);
    }

    private void renderPauseMenu() {
        ScreenUtils.clear(0, 0, 0f, 1);
        drawComponentOnEdge(pauseMenu, 4, .8f, 40, -20);
    }

    public void drawComponentOnEdge(TextureRegion image, int position, float mul, int xOffset, int yOffset) {
        drawComponentOnEdge(image, position, mul, xOffset, yOffset, 0);
    }

    public void drawComponentOnEdge(TextureRegion image, int position, float mul, float xOffset, float yOffset, float rotation) {
        float x = Camera.orthographicCamera.position.x;
        float y = Camera.orthographicCamera.position.y;
        float w = Camera.VIEWPORT_ZOOM* Gdx.graphics.getWidth();
        float h = Camera.VIEWPORT_ZOOM* Gdx.graphics.getHeight();
        float imgW = image.getRegionWidth()*mul;
        float imgH = image.getRegionHeight()*mul;
        boolean centerX=false, centerY = false;
        boolean left=false,down=false;
        if (3 <= position && position < 6) {
            centerY = true;
        } else if (position >= 6){
            down = true;
        }

        if (position%3 ==0) {
            left = true;
        } else if (position%3 ==1) {
            centerX = true;
        }
        if (!centerY) {
            if (down) {
                y -= h / 2;
            } else {
                y += h / 2 - imgH;
            }
        } else {
            y-= imgH/2;
        }
        if (!centerX) {
            if (left) {
                x -= w / 2;
            } else {
                x += w / 2 - imgW;
            }
        } else {
            x-= imgW/2;
        }

        Application.batch.draw(image, x + xOffset, y + yOffset, imgW/2, imgW/2, imgW, imgH, 1, 1, rotation);
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
        drawOther();
        renderUI();
        Application.batch.end();
    }

    public void drawOther() {

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

    public abstract int getMapWidth();

    public abstract int getMapHeight();

    public abstract Vector2 getInitialPlayerPosition();
}
