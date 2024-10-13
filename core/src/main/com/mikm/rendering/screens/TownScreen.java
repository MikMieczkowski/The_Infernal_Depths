package com.mikm.rendering.screens;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mikm.Assets;
import com.mikm.entities.Door;
import com.mikm.entities.enemies.Slime;
import com.mikm.rendering.Camera;

public class TownScreen extends GameScreen {

    private static TextureRegion stallroof = Assets.getInstance().getTextureRegion("stallroof", 80, 48);
    private static TextureRegion blacksmithroof = Assets.getInstance().getTextureRegion("blacksmithRoof", 80, 96);
    private static TextureRegion tree = Assets.getInstance().getTextureRegion("treetop", 48, 48);
    private static TextureRegion roof = Assets.getInstance().getTextureRegion("houseRoof", 64,96);
    private boolean[][] collidableGrid;
    private boolean[][] holePositions;

    private final int MAP_WIDTH = 60, MAP_HEIGHT = 50;
    TownScreen() {
        super();
        tiledMap = new TmxMapLoader().load("Overworld.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1);
        tiledMapRenderer.setView(Camera.orthographicCamera);
        createMusic(Assets.getInstance().getAsset("sound/townTheme.mp3", Music.class));
        collidableGrid = readCollisionTiledmapLayer(2, MAP_WIDTH, MAP_HEIGHT);
        holePositions = readCollisionTiledmapLayer(3, MAP_WIDTH, MAP_HEIGHT);

        int offset = 15*16;
        addInanimateEntity(new Door(offset+3*16, offset+13*16, 4));
        addInanimateEntity(new Door(offset+9*16, offset+8*16, 3));
        //addInanimateEntity(new NPC(Assets.testTexture, 50, 50));
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        super.render(delta);
    }

    @Override
    public boolean[][] isCollidableGrid() {
        return collidableGrid;
    }

    @Override
    void drawAssets() {
        super.drawAssets();
        int offset = 15*16;
        Application.batch.setShader(null);
        Application.batch.draw(blacksmithroof, offset+6*16, offset+8*16);
        Application.batch.draw(tree, offset+5*16, offset+14*16);
        Application.batch.draw(tree, offset+-1*16,offset+ 11*16);
        Application.batch.draw(tree, offset+1*16, offset+3*16);
        Application.batch.draw(tree, offset+0*16, offset+7*16);
        Application.batch.draw(tree, offset+23*16,offset+ 7*16);
        Application.batch.draw(tree, offset+21*16,offset+ 16*16);
        Application.batch.draw(roof, offset+12*16,offset+ 9*16);
        Application.batch.draw(roof, offset+16*16,offset+ 9*16);

    }


    public boolean[][] getHolePositions() {
        return holePositions;
    }

    @Override
    public Vector2 getInitialPlayerPosition() {
        return new Vector2(320,290);
    }
}
