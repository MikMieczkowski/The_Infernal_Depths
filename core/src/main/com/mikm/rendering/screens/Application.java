package com.mikm.rendering.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.entities.player.Player;
import com.mikm.rendering.TextureAtlasUtils;

import java.util.ArrayList;

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
		TextureAtlas textureAtlas = assetManager.get("images/The Infernal Depths.atlas", TextureAtlas.class);
		testTexture = textureAtlas.findRegion("sand").split(defaultTileWidth, defaultTileHeight)[0][0];

		ArrayList<TextureAtlas.AtlasRegion> atlasRegions = TextureAtlasUtils.findRegionsStartingWith("Character", textureAtlas);
		ArrayList<TextureRegion[]> playerSpritesheets = TextureAtlasUtils.splitAtlasRegionsTo1DArrays(atlasRegions, 32, 32);
		player = new Player(500, 500, playerSpritesheets);

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
