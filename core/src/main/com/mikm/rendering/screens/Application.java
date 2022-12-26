package com.mikm.rendering.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.entities.player.Player;

public class Application extends Game {
	public static final int defaultTileWidth = 16, defaultTileHeight = 16;
	SpriteBatch batch;
	private CaveScreen caveScreen;
	public Player player;
	public static TextureRegion testTexture;

	@Override
	public void create() {
		batch = new SpriteBatch();

		AssetManager assetManager = createAssetManager();

//		Texture playerSpritesheetTexture = assetManager.get("images/player.png", Texture.class);
//		TextureRegion[][] playerSpritesheet = TextureRegion.split(playerSpritesheetTexture, Player.playerWidthPixels, Player.playerHeightPixels);
//		testTexture = assetManager.get("images/sand.png", Texture.class);
		TextureAtlas textureAtlas = assetManager.get("images/The Infernal Depths.atlas", TextureAtlas.class);
		TextureRegion[][] playerSpritesheet = textureAtlas.findRegion("player").split(Player.playerWidthPixels, Player.playerHeightPixels);
		testTexture = textureAtlas.findRegion("sand").split(defaultTileWidth, defaultTileHeight)[0][0];

		player = new Player(500, 500, playerSpritesheet);
		caveScreen = new CaveScreen(this, textureAtlas);
		player.setScreen(caveScreen);
		setScreen(caveScreen);
	}

	@Override
	public void render() {
		renderScreens();
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
}
