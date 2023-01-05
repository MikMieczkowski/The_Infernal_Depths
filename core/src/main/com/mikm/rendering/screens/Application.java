package com.mikm.rendering.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.mikm.Vector2Int;
import com.mikm.entities.player.Player;
import com.mikm.entities.player.weapons.WeaponInstances;
import com.mikm.input.InputAxis;
import com.mikm.rendering.SpritesheetUtils;
import com.mikm.rendering.tilemap.CaveTilemap;

import java.util.ArrayList;
import java.util.Random;

public class Application extends Game {
	public static final int defaultTileWidth = 16, defaultTileHeight = 16;
	SpriteBatch batch;
	private CaveScreen caveScreen;
	public Player player;
	public static TextureRegion testTexture;
	private AssetManager assetManager;
	private TextureAtlas textureAtlas;

	public static final boolean playMusic = false;

	public static ShaderProgram shader;

	@Override
	public void create() {
		batch = new SpriteBatch();
		shader = new ShaderProgram(batch.getShader().getVertexShaderSource(), Gdx.files.internal("images/underwater.frag").readString());
		if (!shader.isCompiled()){
			throw new RuntimeException(shader.getLog());
		}

		assetManager = createAssetManager();
		textureAtlas = assetManager.get("images/The Infernal Depths.atlas", TextureAtlas.class);
		testTexture = textureAtlas.findRegion("sand").split(defaultTileWidth, defaultTileHeight)[0][0];
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
		shader.dispose();
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
		Random random = new Random();
		int randomX;
		int randomY;
		int count = 0;
		do {
			randomX = random.nextInt(CaveTilemap.mapWidth * Application.defaultTileWidth);
			randomY = random.nextInt(CaveTilemap.mapHeight * Application.defaultTileHeight);
			randomX = randomX/Application.defaultTileWidth*Application.defaultTileWidth;
			randomY = randomY/Application.defaultTileHeight*Application.defaultTileHeight;
			count++;
			if (count > 500) {
				return new Vector2Int(0, 0);
			}
		} while (CaveTilemap.isRuleCellAtPosition(randomX, randomY));
		return new Vector2Int(randomX-8, randomY-8);
	}


}
