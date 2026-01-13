package com.mikm.rendering.screens;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mikm.utils.Assets;
import com.mikm._components.CombatComponent;
import com.mikm.entities.prefabLoader.PrefabInstantiator;
import com.mikm.rendering.cave.RockType;
import com.mikm.input.GameInput;

public class SlimeBossRoomScreen extends GameScreen {

    private TextureRegion holeImg = Assets.getInstance().getSplitTextureRegion("circularHole")[0][1];
    private boolean[][] holePositions;
    private boolean awarded = false;
    private boolean[][] collidableGrid;
    private Entity slimeBoss;
    private float nextRoomTimer = 0;
    private float NEXT_ROOM_WAIT_TIME = 3;
    public static boolean slimeBossDefeated = false;


    SlimeBossRoomScreen() {
        super();

        holePositions = new boolean[getMapHeight()][getMapWidth()];

        tiledMap = new TmxMapLoader().load("tiled/SlimeBoss.tmx");

        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1);
        collidableGrid = readCollisionTiledmapLayer(3, getMapWidth(),getMapHeight());

        createMusic(Assets.getInstance().getAsset("sound/hubba_bubba.mp3", Music.class));
        readAndCreateDestructiblesTiledmapLayer(2, Assets.getInstance().getTextureRegion("caveFloor"), false);
        PrefabInstantiator.addEntity("rope", this);
    }

    @Override
    public boolean[][] isCollidableGrid() {
        return collidableGrid;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(CaveScreen.caveFillColorLevel6);
        super.render(delta);

        if (CombatComponent.MAPPER.get(slimeBoss).dead && !awarded) {
            slimeBossDefeated = true;
            RockType.get(1).increaseOreAmount(3);
            RockType.get(2).increaseOreAmount(3);
            RockType.get(3).increaseOreAmount(3);
            song.stop();
            holePositions[7][3] = true;
            awarded = true;
        }
        
    }

    @Override
    protected void drawAssetsPostEntities() {
        if (Application.getInstance().caveScreen.displayButtonIndicator) {
            Application.batch.draw(GameInput.getTalkButtonImage(), Application.getInstance().caveScreen.buttonIndicatorPosition.x, Application.getInstance().caveScreen.buttonIndicatorPosition.y);
        }
        if (awarded) {
            Application.batch.draw(holeImg,48,112);
        }
    }

    @Override
    public void onEnter() {
        resetSlimeBoss();
        song.play();
    }

    public boolean[][] getHolePositions() {
        return holePositions;
    }

    private void resetSlimeBoss() {
        if (slimeBoss != null) {
            removeEntity(slimeBoss);
        }
        slimeBoss = PrefabInstantiator.addEntity("slimeBoss", Application.getInstance().slimeBossRoomScreen, 200, 200);
    }

    @Override
    public Vector2 getInitialPlayerPosition() {
        return new Vector2(96,96);
    }

    @Override
    public int getMapWidth() {
        return 25;
    }

    @Override
    public int getMapHeight() {
        return 25;
    }
}
