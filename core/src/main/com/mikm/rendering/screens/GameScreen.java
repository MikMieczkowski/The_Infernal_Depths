package com.mikm.rendering.screens;


import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mikm._components.CombatComponent;
import com.mikm._systems.*;
import com.mikm.entities.prefabLoader.PrefabInstantiator;
import com.mikm.utils.Assets;
// removed unused import
import com.mikm._components.WorldColliderComponent;
import com.mikm._components.Transform;
import com.mikm.entities.animation.SingleAnimation;
import com.mikm.entities.animation.SuperAnimation;
import com.mikm.utils.debug.DebugRenderer;
import com.mikm.entities.inanimateEntities.particles.ParticleTypes;
import com.mikm.rendering.Camera;
import com.mikm.rendering.cave.RockType;
import com.mikm.rendering.sound.SoundEffects;

// removed unused imports

public abstract class GameScreen extends ScreenAdapter {
    public static ScreenViewport viewport;
    Music song;
    public static Camera camera;

    public OrthogonalTiledMapRenderer tiledMapRenderer;
    public TiledMap tiledMap;
    public Engine engine;


    public Entity player;
    public Transform playerTransform;
    public WorldColliderComponent playerCollider;


    private final TextureRegion hpBar, hpBarBottom;
    private final TextureRegion[][] health;
    private final TextureRegion pauseMenu;
    private final TextureRegion musicIconOn;
    private final TextureRegion musicIconOff;
    private Rectangle musicIconBounds = new Rectangle();
    private float healthAnimationTimer, healthAnimationFrameDuration = .1f;

    private String GRASS_BREAK_SOUND_EFFECT = "grassBreak.ogg";
    private String POT_BREAK_SOUND_EFFECT = "potBreak.ogg";
    private String BONES_BREAK_SOUND_EFFECT = "boneBreak.ogg";
    private String WOOD_BREAK_SOUND_EFFECT = "woodBreak.ogg";

    private final float MASTER_SONG_MUL = .75f;

    private boolean renderCamera = true, renderUI = true;

    GameScreen() {
        hpBar = Assets.getInstance().getTextureRegion("hpBar", 16, 80);
        hpBarBottom = Assets.getInstance().getTextureRegion("hpBarBottom", 16, 80);
        health = Assets.getInstance().getSplitTextureRegion("health", 16, 8);
        pauseMenu = Assets.getInstance().getTextureRegion("controls", 350, 300);
        musicIconOn = Assets.getInstance().getSplitTextureRegion("musicIcons", 32, 32)[0][0]; // simple placeholder texture
        musicIconOff = Assets.getInstance().getSplitTextureRegion("musicIcons", 32, 32)[0][1]; // simple placeholder texture

        engine = new Engine();
        engine.addSystem(new RoutineSystem());
        engine.addSystem(new AnimationSystem());
        engine.addSystem(new WorldCollisionMovementSystem());
        engine.addSystem(new EffectsSystem());
        engine.addSystem(new RenderingSystem());
        engine.addSystem(new PlayerTriggerSystem());
        engine.addSystem(new CombatSystem());


        player = PrefabInstantiator.addEntity("player", this,
                getInitialPlayerPosition().x, getInitialPlayerPosition().y);
        PrefabInstantiator.addPlayerWeapon(this);
        CombatComponent.MAPPER.get(player).hp = CombatComponent.MAPPER.get(player).MAX_HP;

        camera = new Camera();
        viewport = new ScreenViewport(Camera.orthographicCamera);
        viewport.setUnitsPerPixel(Camera.VIEWPORT_ZOOM);
        Camera.setPositionDirectlyToPlayerPosition();
//        Set<Component> componentSet = new HashSet<>(Arrays.asList(
//            new RoutineListComponent(),
//            new Transform(80*16, 80*16),
//            new SpriteComponent(Assets.testTexture),
//            new ColliderComponent()
//        ));

//        for (Component c : componentSet) {
//            player.add(c);
//        }
//        PrefabLoader.getInstance().addPrefab("player", componentSet);
//        engine.addEntity(player);
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

    void readAndCreateDestructiblesTiledmapLayer(int layer, TextureRegion floorUnder, boolean shadows) {
        TextureRegion[][] particleImgs = Assets.getInstance().getSplitTextureRegion("particles",8,8);
        TiledMapTileLayer l = (TiledMapTileLayer)tiledMap.getLayers().get(layer);

        for (int i = 0; i < getMapWidth(); i++) {
            for (int j = 0; j < getMapHeight(); j++) {
                TiledMapTileLayer.Cell cell = l.getCell(i,j);
                if (cell == null) {
                    continue;
                }
                MapProperties props = cell.getTile().getProperties();
                if (!props.containsKey("destructible")) {
                    continue;
                }
                if (!props.containsKey("particleType")) {
                    throw new RuntimeException("Tile in Tiled should have property type if it has property destructible");
                }
                String s = (String)props.get("particleType");
                String soundEffect;
                ParticleTypes particleType;
                if (s.equals("grass")) {
                    soundEffect = GRASS_BREAK_SOUND_EFFECT;
                    particleType = ParticleTypes.getDestructibleParameters(particleImgs[2][0]);
                } else if (s.equals("pot")) {
                    soundEffect = POT_BREAK_SOUND_EFFECT;
                    particleType = ParticleTypes.getDestructibleParameters(particleImgs[0][3]);
                } else if (s.equals("bone")) {
                    soundEffect = POT_BREAK_SOUND_EFFECT;
                    particleType = ParticleTypes.getDestructibleParameters(particleImgs[2][1]);
                } else if (s.equals("wood")) {
                    soundEffect = WOOD_BREAK_SOUND_EFFECT;
                    particleType = ParticleTypes.getDestructibleParameters(particleImgs[0][3]);
                } else {
                    throw new RuntimeException("unknown particleType property " + s + " in Tiled for screen " + this.getClass().getSimpleName());
                }

                boolean animated = false;
                SuperAnimation animation = null;
                if (cell.getTile() instanceof AnimatedTiledMapTile) {
                    AnimatedTiledMapTile animTile = (AnimatedTiledMapTile)cell.getTile();
                    TextureRegion[] t = new TextureRegion[animTile.getFrameTiles().length];
                    int k = 0;
                    for (StaticTiledMapTile tile : animTile.getFrameTiles()) {
                        t[k++] = tile.getTextureRegion();
                    }
                    animated = true;
                    animation = new SingleAnimation(t, 1/(animTile.getAnimationIntervals()[0]/1000f), Animation.PlayMode.LOOP);
                }

                //TODO add back
//                Entity d = createEntity();
//                if (animated) {
//                    d = new Destructible(animation, particleType, soundEffect, i * 16, j * 16);
//                } else {
//                    d = new Destructible(cell.getTile().getTextureRegion(), particleType, soundEffect, i * 16, j * 16);
//                }
//                d.width = cell.getTile().getTextureRegion().getRegionWidth();
//                d.height = cell.getTile().getTextureRegion().getRegionHeight();
//                d.xScale = cell.getFlipHorizontally() ? -1 : 1;
//                d.yScale = cell.getFlipVertically() ? -1 : 1;
//                d.hasShadow = shadows;
//                addInanimateEntity(d);

                cell.setRotation(TiledMapTileLayer.Cell.ROTATE_0);
                cell.setFlipHorizontally(false);
                cell.setFlipVertically(false);
                cell.setTile(new StaticTiledMapTile(floorUnder));
            }
        }
    }


    public abstract boolean[][] isCollidableGrid();


    // ---- Rendering

    public void lockCameraAt(int x, int y) {
        Camera.x = x;
        Camera.y = y;
        Camera.orthographicCamera.position.set(Camera.x, Camera.y, 0);
        Camera.orthographicCamera.update();
    }

    public void setRenderCamera(boolean b) {
        renderCamera = b;
    }

    public void setRenderUI(boolean b) {
        renderUI = b;
    }

    @Override
    public void render(float delta) {
        preSetup();
        engine.update(delta);
        postSetup();
    }

    @Deprecated
    private void preSetup() {
        Application.batch.begin();
        if (renderCamera) {
            camera.update();
        }
        Application.batch.setProjectionMatrix(Camera.orthographicCamera.combined);
        tiledMapRenderer.setView(Camera.orthographicCamera);
        tiledMapRenderer.render();
        drawAssetsPreEntities();
    }

    @Deprecated
    private void postSetup() {
        drawAssetsPostEntities();
        DebugRenderer.getInstance().update();
        Camera.updateOrthographicCamera();
        //TODO use a shader to do lighting

        handleSongTransition(true);
        if (renderUI) {
            renderUI();
        }
        Application.batch.end();
    }

    protected void drawAssetsPreEntities() {

    }


    protected void drawAssetsPostEntities() {

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
        for (int i = 0; i < Application.getInstance().getPlayerCombatComponent().hp + 1; i++) {
            drawComponentOnEdge(health[(Application.getInstance().getPlayerCombatComponent().hp - i) % 10][f], 6, 1, -4, 1 + i * 8);
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

    //--- END rendering

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

    public com.badlogic.ashley.core.Entity createEntity() {
        Entity entity = engine.createEntity();
        entity.add(new Transform());
        return entity;
    }

    public com.badlogic.ashley.core.Entity createEntity(int x, int y) {
        Entity entity = engine.createEntity();
        entity.add(new Transform(x, y));
        return entity;
    }

    public void removeEntity(com.badlogic.ashley.core.Entity entity) {
        engine.removeEntity(entity);
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
