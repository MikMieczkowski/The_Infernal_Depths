package com.mikm.rendering;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Application extends Game {
	SpriteBatch batch;
	public static Texture img;
	private CaveScreen caveScreen;

	@Override
	public void create() {
		batch = new SpriteBatch();
		img = new Texture("images/sand.png");

		AssetManager assetManager = createAssetManager();
		caveScreen = new CaveScreen(this, assetManager);
		setScreen(caveScreen);
	}

	@Override
	public void render() {
		renderScreens();
	}

	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
		caveScreen.dispose();
	}

	private void renderScreens() {
		super.render();
	}

	private AssetManager createAssetManager() {
		AssetManager assetManager = new AssetManager();
		assetManager.load("images/caveTiles.png", Texture.class);
		assetManager.finishLoading();
		return assetManager;
	}
}
