package com.mikm.rendering.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.Vector2Int;
import com.mikm.entities.player.Player;
import com.mikm.rendering.TextureAtlasUtils;
import com.mikm.rendering.tilemap.CaveTilemap;

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

	public static final boolean playMusic = false;

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
		caveScreen.camera.setPositionDirectlyToPlayerPosition();
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
