package com.mikm.rendering.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mikm.debug.DebugRenderer;
import com.mikm.entities.inanimateEntities.Door;
import com.mikm.rendering.Camera;
import com.mikm.rendering.sound.SoundEffects;

public class WizardScreen extends GameScreen{
    private Color BG_COLOR = CaveScreen.caveFillColorLevel6;
    boolean[][] collidableGrid;
    //private HealingEffect lastHealingEffect = null;
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
        super.lockCameraAt(40, 50);
        super.setRenderCamera(false);
        super.render(delta);
        soundEffectTimer+= Gdx.graphics.getDeltaTime();
        //if (soundEffectTimer > TIME_UNTIL_SOUND_EFFECT) {
        //    if (Application.player.hp != Application.player.MAX_HP) {
        //        SoundEffects.play(SoundEffects.reward);
        //        Application.player.hp = Application.player.getMaxHp();
        //    }
        //}
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
        //lastHealingEffect = new HealingEffect(16, 16);
        //addEntity(lastHealingEffect);
    }

    @Override
    public void onExit() {
        //lastHealingEffect.die();
        Camera.VIEWPORT_ZOOM = Camera.DEFAULT_VIEWPORT_ZOOM;
        viewport.setUnitsPerPixel(Camera.VIEWPORT_ZOOM);
        Application.player.x = 278;
        Application.player.y = 420;
        Camera.setPositionDirectlyToPlayerPosition();
    }

    @Override
    public int getMapWidth() {
        return 4;
    }

    @Override
    public int getMapHeight() {
        return 4;
    }

    @Override
    public Vector2 getInitialPlayerPosition() {
        return new Vector2(32, 32);
    }
}
