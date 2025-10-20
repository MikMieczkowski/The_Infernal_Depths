package com.mikm.rendering.screens;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mikm.Assets;
import com.mikm.entities.Entity;
import com.mikm.entities.inanimateEntities.Rope;
import com.mikm.entities.inanimateEntities.Grave;
import com.mikm.rendering.cave.RockType;
import com.mikm.input.GameInput;
import com.mikm.rendering.Camera;
import com.mikm.debug.DebugRenderer;

import java.util.ArrayList;

public class SlimeBossRoomScreen extends GameScreen {

    private TextureRegion holeImg = Assets.getInstance().getSplitTextureRegion("circularHole")[0][1];
    private boolean[][] holePositions;
    private boolean awarded = false;
    private boolean[][] collidableGrid;
    private Entity slimeBoss;
    private float nextRoomTimer = 0;
    private float NEXT_ROOM_WAIT_TIME = 3;
    public static boolean slimeBossDefeated = false;

    public ArrayList<Grave> graves = new ArrayList<>();

    SlimeBossRoomScreen() {
        super();

        holePositions = new boolean[getMapHeight()][getMapWidth()];

        tiledMap = new TmxMapLoader().load("SlimeBoss.tmx");

        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1);
        collidableGrid = readCollisionTiledmapLayer(3, getMapWidth(),getMapHeight());

        createMusic(Assets.getInstance().getAsset("sound/hubba_bubba.mp3", Music.class));
        readAndCreateDestructiblesTiledmapLayer(2, Assets.getInstance().getTextureRegion("caveFloor"), false);
        addInanimateEntity(new Rope(112,48));
    }

    @Override
    public boolean[][] isCollidableGrid() {
        return collidableGrid;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(CaveScreen.caveFillColorLevel6);
        super.render(delta);
        if (slimeBoss.damagedAction.dead && !awarded) {
            slimeBossDefeated = true;
            entities.doAfterRender(()-> {
                RockType.get(1).increaseOreAmount(3);
                RockType.get(2).increaseOreAmount(3);
                RockType.get(3).increaseOreAmount(3);
                song.stop();
                holePositions[7][3] = true;
            });
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
        resetInanimateAndAnimateEntities();
        song.play();
        System.out.println(entities);
    }

    public boolean[][] getHolePositions() {
        return holePositions;
    }

    private void resetInanimateAndAnimateEntities() {
        for (Entity entity : entities) {
            if (entity != Application.player) {
                removeEntity(entity);
            }
        }
        //inanimateEntities.clear();
        //inanimateEntities.addAll(graves);
        slimeBoss = addEntity("slimeBoss", 200, 200);
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
