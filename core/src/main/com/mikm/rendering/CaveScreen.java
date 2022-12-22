package com.mikm.rendering;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mikm.Vector2Int;
import com.mikm.entities.player.Player;
import com.mikm.rendering.tilemap.CaveGenerator;
import com.mikm.rendering.tilemap.ruleCell.RuleCell;
import com.mikm.rendering.tilemap.ruleCell.RuleCellMetadata;
import com.mikm.rendering.tilemap.ruleCell.RuleCellMetadataReader;
import com.mikm.rendering.tilemap.ruleCell.RuleCellTiledMapTileLayer;

public class CaveScreen extends Screen {
    private Player player;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private TiledMap tiledMap;
    TextureRegion[][] caveTileset;
    CaveScreen(Application application, AssetManager assetManager) {
        super(application, assetManager);

        Texture caveTilesetSpritesheet = assetManager.get("images/caveTiles.png", Texture.class);
        caveTileset = TextureRegion.split(caveTilesetSpritesheet, 16, 16);
        TextureRegion temporaryImage = caveTileset[2][4];

        createTiledMapRenderer();


        player = new Player(1000, 1000, temporaryImage);
        stage.addActor(player.group);

    }

    @Override
    public void render(float delta) {
        application.batch.begin();
        camera.position.set(new Vector3(player.x, player.y, 0));
        camera.update();
        tiledMapRenderer.setView(camera);
        ScreenUtils.clear(Color.DARK_GRAY);
        drawAssets();
        application.batch.end();
    }

    private void drawAssets() {
        stage.draw();
        tiledMapRenderer.render();
    }

    @Override
    public void dispose() {
        super.dispose();
        tiledMapRenderer.dispose();
        tiledMap.dispose();
    }

    private void createTiledMapRenderer() {
        tiledMap = new TiledMap();
        MapLayers mapLayers = tiledMap.getLayers();

        CaveGenerator caveGenerator = new CaveGenerator();
        RuleCell ruleCell = createCaveRuleCell();
        RuleCellTiledMapTileLayer tiledMapTileLayer = caveGenerator.generateMap(ruleCell);

        mapLayers.add(tiledMapTileLayer);
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 2);
        tiledMapRenderer.setView(camera);
    }

    private RuleCell createCaveRuleCell() {
        RuleCellMetadataReader metadataReader = new RuleCellMetadataReader();
        RuleCellMetadata metadata = metadataReader.createMetadataFromFile("images/caveTiles.meta.txt");
        return new RuleCell(caveTileset, metadata);
    }
}
