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
import com.mikm.entities.enemies.slimeBoss.SlimeBoss;
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

    public ArrayList<Grave> graves = new ArrayList<>();

    SlimeBossRoomScreen() {
        super();

        holePositions = new boolean[getMapHeight()][getMapWidth()];

        tiledMap = new TmxMapLoader().load("SlimeBoss.tmx");

        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1);
        collidableGrid = readCollisionTiledmapLayer(2, getMapWidth(),getMapHeight());

        createMusic(Assets.getInstance().getAsset("sound/hubba_bubba.mp3", Music.class));

    }

    @Override
    public boolean[][] isCollidableGrid() {
        return collidableGrid;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(CaveScreen.caveFillColorLevel6);
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
            //difference 1 from super.render()
            if (Application.getInstance().caveScreen.displayButtonIndicator) {
                Application.batch.draw(GameInput.getTalkButtonImage(), Application.getInstance().caveScreen.buttonIndicatorPosition.x, Application.getInstance().caveScreen.buttonIndicatorPosition.y);
            }
            //difference 2 from super.render()
            if (awarded) {
                Application.batch.draw(holeImg,48,112);
            }
            Application.batch.end();
        } else {
            drawNoUpdate();
        }
        if (slimeBoss.damagedState.dead && !awarded) {
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
    public void onEnter() {
        resetInanimateAndAnimateEntities();
    }

    public boolean[][] getHolePositions() {
        return holePositions;
    }

    private void resetInanimateAndAnimateEntities() {
        entities.removeInstantly(Application.player);
        entities.clear();
        inanimateEntities.clear();
        addEntity(Application.player);
        inanimateEntities.addAll(graves);
        slimeBoss = new SlimeBoss(this, 200, 200);
        addEntity(slimeBoss);
        addInanimateEntity(new Rope(112,48));
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
