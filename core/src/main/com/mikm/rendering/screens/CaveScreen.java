package com.mikm.rendering.screens;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mikm.rendering.tilemap.CaveLevelGenerator;
import com.mikm.rendering.tilemap.ruleCell.RuleCell;
import com.mikm.rendering.tilemap.ruleCell.RuleCellMetadata;
import com.mikm.rendering.tilemap.ruleCell.RuleCellMetadataReader;

public class CaveScreen extends GameScreen {

    private final Color caveWallColor = new Color(41/255f, 16/255f, 16/255f, 1);
    public final TextureRegion[][] caveTileset;
    public final TextureRegion[][] rockImages;

    CaveScreen(Application application, AssetManager assetManager) {
        super(application, assetManager);

        Texture caveTilesetSpritesheet = assetManager.get("images/caveTiles.png", Texture.class);
        caveTileset = TextureRegion.split(caveTilesetSpritesheet, Application.defaultTileWidth, Application.defaultTileHeight);
        TextureRegion temporaryImage = caveTileset[2][1];

        Texture rockSpritesheet = assetManager.get("images/rocks.png", Texture.class);
        rockImages = TextureRegion.split(rockSpritesheet, Application.defaultTileWidth, Application.defaultTileHeight);

        createTiledMapRenderer();

        stage.addActor(player.group);
    }

    @Override
    public int[] getCollidableTiledMapTileLayerIDs() {
        return new int[]{1, 2};
    }

    @Override
    public void render(float delta) {
        application.batch.begin();
        ScreenUtils.clear(caveWallColor);
        camera.update();
        tiledMapRenderer.setView(camera.orthographicCamera);
        drawAssets();
        application.batch.end();
    }

    @Override
    void drawAssets() {
        tiledMapRenderer.render();
        stage.draw();
        application.batch.draw(player.img, player.x, player.y);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    private void createTiledMapRenderer() {
        CaveLevelGenerator caveLevelGenerator = new CaveLevelGenerator(this);
        tiledMap = caveLevelGenerator.createTiledMap();
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1);
    }


}
