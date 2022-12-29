package com.mikm.rendering.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mikm.Vector2Int;
import com.mikm.entities.player.InputAxis;
import com.mikm.entities.player.Player;
import com.mikm.rendering.TextureAtlasUtils;
import com.mikm.rendering.tilemap.CaveLevelGenerator;

import java.util.ArrayList;
import java.util.Random;

public class Application extends Game {
	public static final int defaultTileWidth = 16, defaultTileHeight = 16;
	SpriteBatch batch;
	private CaveScreen caveScreen;
	public Player player;
	public static TextureRegion testTexture;

	public static boolean usingController = false;
	public static Controller controller;
	private static boolean xPressedLastFrame = false;
	public static boolean xPressed = false;

	@Override
	public void create() {
		batch = new SpriteBatch();

		AssetManager assetManager = createAssetManager();
		TextureAtlas textureAtlas = assetManager.get("images/The Infernal Depths.atlas", TextureAtlas.class);
		testTexture = textureAtlas.findRegion("sand").split(defaultTileWidth, defaultTileHeight)[0][0];

		ArrayList<TextureAtlas.AtlasRegion> atlasRegions = TextureAtlasUtils.findRegionsStartingWith("Character", textureAtlas);
		ArrayList<TextureRegion[]> playerSpritesheets = TextureAtlasUtils.splitAtlasRegionsTo1DArrays(atlasRegions, 32, 32);

		player = new Player(500, 500, playerSpritesheets);
		caveScreen = new CaveScreen(this, textureAtlas);
		player.setScreen(caveScreen);
		Vector2Int playerPosition = spawnablePosition();
		player.x = playerPosition.x;
		player.y = playerPosition.y;
		setScreen(caveScreen);


		if (Controllers.getControllers().size != 0) {
			usingController = true;
			controller = Controllers.getControllers().first();
		}
		System.out.println(usingController);
	}

	@Override
	public void render() {
		if (usingController && !xPressedLastFrame && controller.getButton(0)) {
			xPressed = true;
		}
		renderScreens();
		if (usingController) {
			xPressed = false;
			xPressedLastFrame = controller.getButton(0);
		}
	}

	@Override
	public void dispose () {
		batch.dispose();
		caveScreen.dispose();
	}

	private void renderScreens() {
		super.render();
	}

	private AssetManager createAssetManager() {
		AssetManager assetManager = new AssetManager();
		assetManager.load("images/The Infernal Depths.atlas", TextureAtlas.class);
		assetManager.finishLoading();
		return assetManager;
	}

	private Vector2Int spawnablePosition() {
//		Random random = new Random();
//		int randomX;
//		int randomY;
//		do {
//			randomX = random.nextInt(CaveLevelGenerator.mapWidth * 16);
//			randomY = random.nextInt(CaveLevelGenerator.mapHeight * 16);
//		} while (!caveScreen.canPlayerSpawnAt(randomX, randomY));
//		return new Vector2Int(randomX, randomY);
		return new Vector2Int(500, 500);
	}
}
