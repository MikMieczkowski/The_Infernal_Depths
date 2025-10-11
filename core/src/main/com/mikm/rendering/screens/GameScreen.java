package com.mikm.rendering.screens;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mikm.Assets;
// removed unused import
import com.mikm.entityLoader.EntityLoader;
import com.mikm.debug.DebugRenderer;
import com.mikm.entities.*;
import com.mikm.entities.inanimateEntities.Destructible;
import com.mikm.entities.inanimateEntities.InanimateEntity;
import com.mikm.entities.inanimateEntities.Shadow;
import com.mikm.entities.inanimateEntities.particles.ParticleTypes;
import com.mikm.rendering.Camera;
import com.mikm.rendering.cave.RockType;
import com.mikm.rendering.sound.SoundEffects;

// removed unused imports

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
    private final TextureRegion musicIconOn;
    private final TextureRegion musicIconOff;
    private Rectangle musicIconBounds = new Rectangle();
    private float healthAnimationTimer, healthAnimationFrameDuration = .1f;

    private String GRASS_BREAK_SOUND_EFFECT = "grassBreak.ogg";
    private String POT_BREAK_SOUND_EFFECT = "potBreak.ogg";

    private final float MASTER_SONG_MUL = .75f;


    GameScreen() {
        camera = new Camera();
        viewport = new ScreenViewport(Camera.orthographicCamera);
        viewport.setUnitsPerPixel(Camera.VIEWPORT_ZOOM);
        hpBar = Assets.getInstance().getTextureRegion("hpBar", 16, 80);
        hpBarBottom = Assets.getInstance().getTextureRegion("hpBarBottom", 16, 80);
        health = Assets.getInstance().getSplitTextureRegion("health", 16, 8);
        pauseMenu = Assets.getInstance().getTextureRegion("controls", 350, 300);
        musicIconOn = Assets.getInstance().getSplitTextureRegion("musicIcons", 32, 32)[0][0]; // simple placeholder texture
        musicIconOff = Assets.getInstance().getSplitTextureRegion("musicIcons", 32, 32)[0][1]; // simple placeholder texture

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
                    addInanimateEntity(new Destructible(imgs[2], ParticleTypes.getDestructibleParameters(particleImgs[2][0]), GRASS_BREAK_SOUND_EFFECT, i*16,j*16));
                } else if (l.getCell(i, j).getTile().getId() == DOUBLEPOT) {
                    addInanimateEntity(new Destructible(imgs[1], ParticleTypes.getDestructibleParameters(particleImgs[0][3]), POT_BREAK_SOUND_EFFECT, i*16,j*16));
                } else if (l.getCell(i, j).getTile().getId() == POT) {
                    addInanimateEntity(new Destructible(imgs[0], ParticleTypes.getDestructibleParameters(particleImgs[0][3]), POT_BREAK_SOUND_EFFECT, i*16,j*16));
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
            Camera.updateOrthographicCamera();
            //TODO use a shader to do lighting

            handleSongTransition(true);
            renderUI();
            Application.batch.end();
        } else {
            drawNoUpdate();
        }
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
        handleSongTransition(true);
        renderUI();
        Application.batch.end();
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

        //ChatGPT being a far better coder than i and fixing my ui spaghetti

        // Draw music toggle icon in bottom-right corner of pause menu area
        float iconScale = 1f;
        TextureRegion icon = Application.musicOn ? musicIconOn : musicIconOff;
        // Position relative to screen bottom-right
        float x = Camera.orthographicCamera.position.x + (Camera.VIEWPORT_ZOOM * Gdx.graphics.getWidth())/2 - icon.getRegionWidth()*iconScale - 16;
        float y = Camera.orthographicCamera.position.y - (Camera.VIEWPORT_ZOOM * Gdx.graphics.getHeight())/2 + 16;
        Application.batch.draw(icon, x, y, icon.getRegionWidth()*iconScale, icon.getRegionHeight()*iconScale);
        musicIconBounds.set(x, y, icon.getRegionWidth()*iconScale, icon.getRegionHeight()*iconScale);

        // Handle click/tap
        if (Gdx.input.justTouched()) {
            Vector2 mouse = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            // convert to world coords
            Vector3 world3 = Camera.orthographicCamera.unproject(new Vector3(mouse.x, mouse.y, 0));
            Vector2 world = new Vector2(world3.x, world3.y);
            if (musicIconBounds.contains(world)) {
                Application.musicOn = !Application.musicOn;
                if (Application.musicOn) {
                    playSong(null);
                } else {
                    stopSong(null);
                }
            }
        }
    }

    public void drawComponentOnEdge(TextureRegion image, int position, float mul, int xOffset, int yOffset) {
        drawComponentOnEdge(image, position, mul, xOffset, yOffset, 0);
    }

    //position: 0 top left, 1 top, 2 top right,
    //3 center left, 4 center, 5 center right,
    //6 bottom left, 7 bottom, 8 bottom right
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



    public void drawOther() {

    }

    private TransitionState transitioningSong;
    private enum TransitionState {
        NONE, TURNING_ON, TURNING_OFF
    }
    //goes from 0 to MAX_SONG_TRANSITION_TIME with 0 being fully off and MAX_SONG_TRANSITION_TIME being fully on
    float songTransitionSlider = 0;
    private final float MAX_SONG_TRANSITION_TIME = 1;
    private GameScreen otherScreen;

    protected void handleSongTransition(boolean enterOther) {
        if (enterOther && otherScreen != null) {
            otherScreen.handleSongTransition(false);
        }
        if (transitioningSong == TransitionState.NONE) {
            return;
        }

        if (songTransitionSlider > MAX_SONG_TRANSITION_TIME) {
            transitioningSong = TransitionState.NONE;
            return;
        }
        float vol = songTransitionSlider / MAX_SONG_TRANSITION_TIME;
        if (transitioningSong == TransitionState.TURNING_ON) {
            songTransitionSlider += Gdx.graphics.getDeltaTime();
            songTransitionSlider = MathUtils.clamp(songTransitionSlider, 0, 1);
            if (Application.musicOn && song != null) {
                song.setVolume(MASTER_SONG_MUL * SoundEffects.SFX_VOLUME * vol);
            }
        } else {
            songTransitionSlider -= Gdx.graphics.getDeltaTime();
            songTransitionSlider = MathUtils.clamp(songTransitionSlider, 0, 1);
            if (song != null) {
                song.setVolume(vol);
            }
        }
    }

    public void playSong(GameScreen oldScreen) {
        transitioningSong = TransitionState.TURNING_ON;
        otherScreen = oldScreen;
    }

    public void stopSong(GameScreen newScreen) {
        transitioningSong = TransitionState.TURNING_OFF;
        otherScreen = newScreen;
    }

    void createMusic(Music song) {
        this.song = song;
        song.setVolume(MASTER_SONG_MUL * SoundEffects.SFX_VOLUME);
        song.setLooping(true);
        song.play();
        song.setVolume(0);
    }

    public void onEnter() {

    }

    public void onExit() {

    }

    void drawAssets() {
        tiledMapRenderer.render();
        if (!Application.getInstance().paused) {
            inanimateEntities.render(Application.batch);
            entities.render(Application.batch);
        }
    }

    public void addPlayer() {
        addEntity(Application.player);
    }
    public Entity addEntity(String entityName, int x, int y) {
        Entity entity = EntityLoader.create(entityName);
        entity.x = x;
        entity.y = y;
        addEntity(entity);
        return entity;
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
        if (entity.hasShadow()) {
            Shadow shadow = new Shadow(entity);
            entity.shadow = shadow;
            inanimateEntities.add(shadow);
        }
    }


    public void addEntityInstantly(String entityName, int x, int y) {
        Entity entity = EntityLoader.create(entityName);
        entity.x = x;
        entity.y = y;
        addEntityInstantly(entity);
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
