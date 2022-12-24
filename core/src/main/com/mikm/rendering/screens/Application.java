package com.mikm.rendering.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.entities.player.Player;

public class Application extends Game {
	public static final int defaultTileWidth = 16, defaultTileHeight = 16;
	SpriteBatch batch;
	public static TextureRegion img;
	private CaveScreen caveScreen;
	public Player player;

	@Override
	public void create() {
		batch = new SpriteBatch();

		AssetManager assetManager = createAssetManager();
		img = new TextureRegion(assetManager.get("images/sand.png", Texture.class));

		player = new Player(500, 500, img);
		caveScreen = new CaveScreen(this, assetManager);
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
		assetManager.load("images/caveTiles.png", Texture.class);
		assetManager.load("images/sand.png", Texture.class);
		assetManager.finishLoading();
		return assetManager;
	}
}
