package com.mikm.rendering.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.mikm.Assets;
import com.mikm.debug.DebugRenderer;
import com.mikm.entities.player.Player;
import com.mikm.entities.player.weapons.WeaponInstances;
import com.mikm.input.InputRaw;
import com.mikm.rendering.Camera;
import com.mikm.serialization.Serializer;

public class Application extends Game {
	public static final int TILE_WIDTH = 16, TILE_HEIGHT = 16;
	public static final boolean PLAY_MUSIC = false;

	public static SpriteBatch batch;
	//TODO move this out of this class
	public static ShaderProgram fillColorShader;

	public static GameScreen currentScreen;
	public static CaveScreen caveScreen;
	public static TownScreen townScreen;
	public SlimeBossRoomScreen slimeBossRoomScreen;

	public static Player player;

	public static boolean timestop;
	private static int timeStopFrames;
	private static final int MAX_TIMESTOP_FRAMES = 10;

	@Override
	public void create() {
		batch = new SpriteBatch();

		fillColorShader = new ShaderProgram(batch.getShader().getVertexShaderSource(), Gdx.files.internal("images/fillColor.frag").readString());
		if (!fillColorShader.isCompiled()){
			throw new RuntimeException(fillColorShader.getLog());
		}

		createPlayerAndCaveScreen();
		townScreen = new TownScreen(this);
		slimeBossRoomScreen = new SlimeBossRoomScreen(this);

		Camera.setPositionDirectlyToPlayerPosition();
		setGameScreen(townScreen);
	}

	private void createPlayerAndCaveScreen() {
		Application.player = new Player(500, 500);
		caveScreen = new CaveScreen(this);
		player.setWeapons(new WeaponInstances(caveScreen));
		Camera.setPositionDirectlyToPlayerPosition();
	}

	@Override
	public void render() {
		InputRaw.checkForControllers();
		InputRaw.handleLastFrameInput();
		renderScreens();
		if (timestop) {
			timeStopFrames++;
			if (timeStopFrames > MAX_TIMESTOP_FRAMES) {
				timeStopFrames = 0;
				timestop = false;
			}
		}
		InputRaw.handleThisFrameInput();
		handleDebugInput();
	}

	@Override
	public void dispose() {
		batch.dispose();
		caveScreen.dispose();
		townScreen.dispose();
		slimeBossRoomScreen.dispose();
		fillColorShader.dispose();
		Assets.getInstance().dispose();
		DebugRenderer.getInstance().dispose();
		Serializer.getInstance().dispose();
	}

	public static void freezeTime() {
		timestop = true;
	}

	public static void setFillColorShader(Batch batch, Color color) {
		batch.setShader(fillColorShader);
		fillColorShader.bind();
		fillColorShader.setUniformf("u_color", color);
	}

	public void setGameScreen(GameScreen gameScreen) {
		if (currentScreen != null && currentScreen.song != null) {
			currentScreen.stopSong();
		}
		currentScreen = gameScreen;
		setScreen(gameScreen);
		if (gameScreen.song != null) {
			gameScreen.playSong();
		}
	}

	private void renderScreens() {
		super.render();
	}

	private void handleDebugInput() {
		if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
			if (currentScreen == caveScreen) {
				player.x = 100;
				player.y = 100;
				Camera.setPositionDirectlyToPlayerPosition();
				setGameScreen(townScreen);
			}
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
			setGameScreen(Application.caveScreen);
			caveScreen.increaseFloor();
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.V)) {
			caveScreen.decreaseFloor();
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
			freezeTime();
		}
	}
}
