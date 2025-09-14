package com.mikm.rendering.screens;

// removed unused import
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.esotericsoftware.kryo.io.KryoBufferUnderflowException;
import com.mikm.Assets;
import com.mikm.debug.DebugRenderer;
import com.mikm.entities.Grave;
import com.mikm.entities.enemies.slimeBoss.SlimeBoss;
import com.mikm.entities.player.Player;
import com.mikm.entities.player.weapons.WeaponInstances;
import com.mikm.entities.projectiles.DamageInformation;
import com.mikm.input.GameInput;
import com.mikm.input.InputRaw;
import com.mikm.rendering.Camera;
import com.mikm.rendering.SoundEffects;
import com.mikm.rendering.cave.RockType;
import com.mikm.serialization.Serializer;

import java.util.ArrayList;

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
	public static boolean musicOn = true;

	public static SpriteBatch batch;
	public ShaderProgram fillColorShader;

	public GameScreen currentScreen;
	public CaveScreen caveScreen;
	public TownScreen townScreen;
	public SlimeBossRoomScreen slimeBossRoomScreen;
	public BlacksmithScreen blacksmithScreen;
	public WizardScreen wizardScreen;
	public MotiScreen motiScreen;

	public GameScreen[] screens;

	public static Player player;

	public boolean timestop;
	public boolean paused = false;
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
		motiScreen = new MotiScreen();
		screens= new GameScreen[]{caveScreen, townScreen, slimeBossRoomScreen, blacksmithScreen, wizardScreen, motiScreen};

		Camera.setPositionDirectlyToPlayerPosition();
		setGameScreen(townScreen);

	}

	private static int n = 0;
	private ArrayList<Integer> saveData;
	@SuppressWarnings("unchecked")
	private int readSaveData() {
		if (n==0) {
			saveData = (ArrayList<Integer>) Serializer.getInstance().read(ArrayList.class, 10);
		}
		n++;
		return saveData.get(n-1);
	}

	public void loadAllSaveData() {
		try {

			Application.player.swordLevel = readSaveData();
			Application.player.bowLevel = readSaveData();
			for (int i = 0; i < RockType.SIZE; i++) {
				RockType.get(i).increaseOreAmount(readSaveData());
				System.out.println(RockType.get(i).getOreAmount());
				RockType.get(i).tempOreAmount = 0;
			}

			boolean b = readSaveData() == 1;
			if (b) {
				if (Application.player.bowLevel != 0) {
					Application.player.equippedWeapon = Application.player.weaponInstances.bows[Application.player.bowLevel - 1];
					Application.player.currentHeldItem = Application.player.equippedWeapon;
				}
			} else {
				if (Application.player.swordLevel != 0) {
					Application.player.equippedWeapon = Application.player.weaponInstances.swords[Application.player.swordLevel-1];
					Application.player.currentHeldItem = Application.player.equippedWeapon;
				}
			}
			SlimeBoss.defeated = readSaveData() == 1;
		} catch (KryoBufferUnderflowException e) {
			//file was empty, don't load anything
		}
	}

	private void createPlayerAndCaveScreen() {
		player = new Player(448, 448);
		caveScreen = new CaveScreen();
		player.setWeapons(new WeaponInstances());
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
		handleDebugInput();
 		InputRaw.handleThisFrameInput();
		checkRespawn();
	}

	private void checkRespawn() {
		if (player.dead) {
			player.deadTime += Gdx.graphics.getDeltaTime();
			if (player.deadTime > player.RESPAWN_TIME) {
				//respawn
				Application.getInstance().caveScreen.updateCurrentMemento();
				CaveScreen.floor = 0;
				player.deadTime -= player.RESPAWN_TIME;
				player.hp = player.getMaxHp();
				player.damagedState.dead = false;
				Application.getInstance().currentScreen.entities.doAfterRender(()->{
					Application.getInstance().setGameScreen(Application.getInstance().townScreen);
					player.dead = false;
				});
			}
		}
	}

	private void onApplicationExit() {
		if (RockType.playerHasAnyTempOre()) {
			Application.getInstance().currentScreen.addInanimateEntityInstantly(new Grave(Application.player.x, Application.player.y));
			caveScreen.updateCurrentMemento();
		}
		writeSaveData();
	}

	private void writeSaveData() {
		if (currentScreen == caveScreen) {
			caveScreen.updateCurrentMemento();
		}
		ArrayList<Integer> saveData = new ArrayList<>();
		saveData.add(Application.player.swordLevel);
		saveData.add(Application.player.bowLevel);
		for (int i = 0; i < RockType.SIZE; i++) {
			saveData.add(RockType.get(i).getOreAmount()- RockType.get(i).tempOreAmount);
		}
		int b = 0;
		if (Application.player.bowLevel != 0 && Application.player.equippedWeapon == Application.player.weaponInstances.bows[Application.player.bowLevel-1]) {
			b = 1;
		}
		saveData.add(b);
		saveData.add(SlimeBoss.defeated ? 1:0);
		Serializer.getInstance().write(saveData, 10);
	}

	@Override
	public void dispose() {
		onApplicationExit();
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
		// Handle pause button logic
		if (GameInput.isPauseButtonJustPressed()) {
			System.out.println("pressed");
			if (currentScreen instanceof TownScreen && ((TownScreen)currentScreen).isMainMenuActive()) {
				// Main menu is active - ignore pause button
			} else {
				// Main menu is not active - handle pause normally
				paused = !paused;
			}
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
			if (currentScreen == caveScreen && !paused) {
				Application.getInstance().caveScreen.entities.doAfterRender(()-> {
					Camera.setPositionDirectlyToPlayerPosition();
					setGameScreen(townScreen);
				});
			}
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
			if (CaveScreen.floor < CaveScreen.LAST_FLOOR-5 && !paused) {
				setGameScreen(caveScreen);
				caveScreen.increaseFloor();
			}
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.U)) {
			if (!paused) {
				RockType.get(1).increaseOreAmount(7);
				RockType.get(2).increaseOreAmount(7);
				RockType.get(3).increaseOreAmount(7);
				RockType.get(4).increaseOreAmount(7);
			}
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.V)) {
			if (CaveScreen.floor > 0 && !paused) {
				setGameScreen(caveScreen);
				caveScreen.decreaseFloor();
			}
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
			if (!paused) {
				Application.player.hp =1;
				Application.player.damagedState.enter(new DamageInformation(1, 0 ,1));
			}
		}

	}
}
