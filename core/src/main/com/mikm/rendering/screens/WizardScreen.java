package com.mikm.rendering.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mikm.Assets;
import com.mikm.debug.DebugRenderer;
import com.mikm.entities.Door;
import com.mikm.entities.HealingEffect;
import com.mikm.entities.player.Player;
import com.mikm.input.GameInput;
import com.mikm.input.InputRaw;
import com.mikm.rendering.Camera;
import com.mikm.rendering.SoundEffects;
import com.mikm.rendering.cave.RockType;

public class WizardScreen extends GameScreen{
    private Color BG_COLOR = CaveScreen.caveFillColorLevel6;
    boolean[][] collidableGrid;
    private HealingEffect lastHealingEffect = null;
    private float soundEffectTimer = 0;
    private final float TIME_UNTIL_SOUND_EFFECT = 1.5f;

    WizardScreen() {
        super();
        collidableGrid = new boolean[4][4];
        tiledMap = new TmxMapLoader().load("wizardRoom.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1);
        addInanimateEntity(new Door(32, 16, 1));
        removeInanimateEntity(Application.player.shadow);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(BG_COLOR);
        Camera.x = 40;
        Camera.y = 50;
        Camera.orthographicCamera.position.set(Camera.x, Camera.y, 0);
        Camera.orthographicCamera.update();
        Application.batch.begin();
        Application.batch.setProjectionMatrix(Camera.orthographicCamera.combined);
        tiledMapRenderer.setView(Camera.orthographicCamera);
        drawAssets();
        DebugRenderer.getInstance().update();
        Camera.renderLighting(Application.batch);
        Camera.orthographicCamera.update();
        renderUI();
        Application.batch.end();
        soundEffectTimer+= Gdx.graphics.getDeltaTime();
        if (soundEffectTimer > TIME_UNTIL_SOUND_EFFECT) {
            if (Application.player.hp != Application.player.getMaxHp()) {
                SoundEffects.play(SoundEffects.reward);
                Application.player.hp = Application.player.getMaxHp();
            }
        }
    }


    @Override
    public boolean[][] isCollidableGrid() {
        return collidableGrid;
    }

    @Override
    public void onEnter() {
        soundEffectTimer = 0;
        Camera.VIEWPORT_ZOOM = .2f;
        viewport.setUnitsPerPixel(Camera.VIEWPORT_ZOOM);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        tiledMapRenderer.setView(Camera.orthographicCamera);
        lastHealingEffect = new HealingEffect(16, 16);
        addEntity(lastHealingEffect);
    }

    @Override
    public void onExit() {
        lastHealingEffect.die();
        Camera.VIEWPORT_ZOOM = Camera.DEFAULT_VIEWPORT_ZOOM;
        viewport.setUnitsPerPixel(Camera.VIEWPORT_ZOOM);
        Application.player.x = 278;
        Application.player.y = 420;
        Camera.setPositionDirectlyToPlayerPosition();
    }

    @Override
    public Vector2 getInitialPlayerPosition() {
        return new Vector2(32, 32);
    }
}
