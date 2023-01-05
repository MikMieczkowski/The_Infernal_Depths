package com.mikm.rendering.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.mikm.ExtraMathUtils;
import com.mikm.Vector2Int;
import com.mikm.entities.enemies.Rat;
import com.mikm.entities.player.Player;
import com.mikm.entities.player.weapons.WeaponInstances;
import com.mikm.input.InputAxis;
import com.mikm.rendering.SpritesheetUtils;

import java.util.ArrayList;

public class Application extends Game {
	public static final int TILE_WIDTH = 16, TILE_HEIGHT = 16;
	SpriteBatch batch;
	private CaveScreen caveScreen;
	public Player player;
	public static TextureRegion testTexture;
	private AssetManager assetManager;
	private TextureAtlas textureAtlas;

	public static final boolean playMusic = false;

	public static ShaderProgram fillColorShader;

	@Override
	public void create() {
		batch = new SpriteBatch();

		fillColorShader = new ShaderProgram(batch.getShader().getVertexShaderSource(), Gdx.files.internal("images/fillColor.frag").readString());
		if (!fillColorShader.isCompiled()){
			throw new RuntimeException(fillColorShader.getLog());
		}

		assetManager = createAssetManager();
		textureAtlas = assetManager.get("images/The Infernal Depths.atlas", TextureAtlas.class);
		testTexture = textureAtlas.findRegion("sand").split(TILE_WIDTH, TILE_HEIGHT)[0][0];
		InputAxis.checkForControllers();

		createPlayerAndCaveScreen(textureAtlas);
		setScreen(caveScreen);
	}

	@Override
	public void render() {
		InputAxis.handleLastFrameInput();
		renderScreens();
		InputAxis.handleThisFrameInput();
	}

	@Override
	public void dispose() {
		textureAtlas.dispose();
		assetManager.dispose();
		batch.dispose();
		caveScreen.dispose();
		fillColorShader.dispose();
	}

	private void renderScreens() {
		super.render();
	}

	private AssetManager createAssetManager() {
		AssetManager assetManager = new AssetManager();
		assetManager.load("images/The Infernal Depths.atlas", TextureAtlas.class);
		assetManager.load("sound/caveTheme.mp3", Music.class);
		assetManager.finishLoading();
		return assetManager;
	}

	private void createPlayerAndCaveScreen(TextureAtlas textureAtlas) {
		ArrayList<TextureAtlas.AtlasRegion> atlasRegions = SpritesheetUtils.findAtlasRegionsStartingWith("Character", textureAtlas);
		ArrayList<TextureRegion[]> playerSpritesheets = SpritesheetUtils.splitAtlasRegionsTo1DArrays(atlasRegions, 32, 32);

		player = new Player(500, 500, playerSpritesheets);
		player.setWeapons(new WeaponInstances(textureAtlas, player));
		caveScreen = new CaveScreen(this, assetManager.get("sound/caveTheme.mp3", Music.class), textureAtlas);
		InputAxis.setCamera(caveScreen.camera);
		player.setScreen(caveScreen);
		Vector2Int playerPosition = spawnablePosition();
		player.x = playerPosition.x;
		player.y = playerPosition.y;
		caveScreen.camera.setPositionDirectlyToPlayerPosition();
	}

	private Vector2Int spawnablePosition() {
		ArrayList<Vector2Int> openTilePositions = caveScreen.getOpenTilePositions();
		if (openTilePositions.size() == 0) {
			return Vector2Int.ZERO;
		}
		Vector2Int spawnLocationInTiles = openTilePositions.get(ExtraMathUtils.randomInt(openTilePositions.size()));
		return new Vector2Int(spawnLocationInTiles.x * Application.TILE_WIDTH + (int)player.getBoundsOffset().x,
				spawnLocationInTiles.y * Application.TILE_HEIGHT + (int)player.getBoundsOffset().y);
	}

	public static void setFillColorShader(Batch batch, Color color) {
		batch.setShader(fillColorShader);
		fillColorShader.bind();
		fillColorShader.setUniformf("u_color", color);
	}
}
