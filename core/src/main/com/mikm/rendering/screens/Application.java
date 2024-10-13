package com.mikm.rendering.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mikm.Assets;
import com.mikm.debug.DebugRenderer;
import com.mikm.entities.player.Player;
import com.mikm.entities.player.weapons.Weapon;
import com.mikm.entities.player.weapons.WeaponInstances;
import com.mikm.entities.projectiles.DamageInformation;
import com.mikm.input.InputRaw;
import com.mikm.rendering.Camera;
import com.mikm.rendering.SoundEffects;
import com.mikm.rendering.cave.RockType;
import com.mikm.serialization.Serializer;

public class Application extends Game {
	private static Application instance;

	private Application() {

	}

	public static Application getInstance() {
		if (instance == null) {
			instance = new Application();
		}
		return instance;
	}

	public static final int TILE_WIDTH = 16, TILE_HEIGHT = 16;
	public static final boolean PLAY_MUSIC = true;

	public static SpriteBatch batch;
	public ShaderProgram fillColorShader;

	public GameScreen currentScreen;
	public CaveScreen caveScreen;
	public TownScreen townScreen;
	public SlimeBossRoomScreen slimeBossRoomScreen;
	public BlacksmithScreen blacksmithScreen;
	public WizardScreen wizardScreen;

	public GameScreen[] screens;

	public static Player player;

	public boolean timestop;
	private int timeStopFrames;
	private final int MAX_TIMESTOP_FRAMES = 10;

	@Override
	public void create() {
		batch = new SpriteBatch();

		fillColorShader = new ShaderProgram(batch.getShader().getVertexShaderSource(), Gdx.files.internal("images/fillColor.frag").readString());
		if (!fillColorShader.isCompiled()){
			throw new RuntimeException(fillColorShader.getLog());
		}

		createPlayerAndCaveScreen();
		SoundEffects.create();
		townScreen = new TownScreen();
		blacksmithScreen = new BlacksmithScreen();
		slimeBossRoomScreen = new SlimeBossRoomScreen();
		wizardScreen = new WizardScreen();
		screens= new GameScreen[]{caveScreen, townScreen, slimeBossRoomScreen, blacksmithScreen, wizardScreen};

		Camera.setPositionDirectlyToPlayerPosition();
		setGameScreen(townScreen);

	}

	private void createPlayerAndCaveScreen() {
		player = new Player(448, 448);
		caveScreen = new CaveScreen();
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
		checkRespawn();
		handleDebugInput();
	}

	private void checkRespawn() {
		if (player.dead) {
			player.deadTime += Gdx.graphics.getDeltaTime();
			if (player.deadTime > player.RESPAWN_TIME) {
				player.dead = false;
				player.deadTime -= player.RESPAWN_TIME;
				player.hp = player.getMaxHp();
				player.damagedState.dead = false;
				for (int i = 0; i < RockType.SIZE; i++) {
					RockType.get(i).oreAmount = 0;
				}
				Application.getInstance().currentScreen.entities.doAfterRender(()->{
					Application.getInstance().setGameScreen(Application.getInstance().townScreen);
				});
			}
		}
	}

	@Override
	public void dispose() {
		batch.dispose();
		caveScreen.dispose();
		townScreen.dispose();
		blacksmithScreen.dispose();
		slimeBossRoomScreen.dispose();
		fillColorShader.dispose();
		Assets.getInstance().dispose();
		DebugRenderer.getInstance().dispose();
		SoundEffects.dispose();
		Serializer.getInstance().dispose();
	}

	public void freezeTime() {
		timestop = true;
	}

	public void setFillColorShader(Batch batch, Color color) {
		batch.setShader(fillColorShader);
		fillColorShader.bind();
		fillColorShader.setUniformf("u_color", color);
	}

	public void setGameScreen(GameScreen gameScreen) {
		if (currentScreen == gameScreen) {
			return;
		}
		Application.player.x = gameScreen.getInitialPlayerPosition().x;
		Application.player.y = gameScreen.getInitialPlayerPosition().y;
		if (currentScreen != null) {
			if (currentScreen != gameScreen) {
				currentScreen.onExit();
			}
		}
		if (currentScreen != null && currentScreen.song != null) {
			currentScreen.stopSong();
		}
		currentScreen = gameScreen;
		setScreen(gameScreen);
		if (gameScreen.song != null) {
			gameScreen.playSong();
		}
		gameScreen.onEnter();
	}

	private void renderScreens() {
		super.render();
	}


	private void handleDebugInput() {
		if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
			if (currentScreen == caveScreen) {
				Application.getInstance().caveScreen.entities.doAfterRender(()-> {
					Camera.setPositionDirectlyToPlayerPosition();
					setGameScreen(townScreen);
				});
			}
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
			setGameScreen(caveScreen);
			caveScreen.increaseFloor();
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.U)) {
			RockType.get(0).oreAmount+=5;
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.V)) {
			caveScreen.decreaseFloor();
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
			freezeTime();
		}
	}
}
