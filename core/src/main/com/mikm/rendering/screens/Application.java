package com.mikm.rendering.screens;

// removed unused import
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Circle;
import com.esotericsoftware.kryo.io.KryoBufferUnderflowException;
import com.mikm.entities.prefabLoader.PrefabInstantiator;
import com.mikm.utils.Assets;
import com.mikm._components.*;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm.utils.debug.DebugRenderer;
import com.mikm.entities.DamageInformation;
import com.mikm.input.GameInput;
import com.mikm.input.InputRaw;
import com.mikm.rendering.Camera;
import com.mikm.rendering.sound.SoundEffects;
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
    public TestScreen testScreen;

	public GameScreen[] screens;

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

        caveScreen = new CaveScreen();
		townScreen = new TownScreen();
		blacksmithScreen = new BlacksmithScreen();
		slimeBossRoomScreen = new SlimeBossRoomScreen();
		wizardScreen = new WizardScreen();
		motiScreen = new MotiScreen();
		screens= new GameScreen[]{caveScreen, townScreen, slimeBossRoomScreen, blacksmithScreen, wizardScreen, motiScreen};

		Camera.setPositionDirectlyToPlayerPosition();
        testScreen = new TestScreen();
		setGameScreen(townScreen);
		townScreen.playSong(null);
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
            PlayerCombatComponent playerCombatComponent = PlayerCombatComponent.MAPPER.get(Application.getInstance().getPlayer());
            playerCombatComponent.swordLevel = readSaveData();
            playerCombatComponent.bowLevel = readSaveData();
			for (int i = 0; i < RockType.SIZE; i++) {
				RockType.get(i).increaseOreAmount(readSaveData());
				RockType.get(i).tempOreAmount = 0;
			}

			boolean b = readSaveData() == 1;
			if (b) {
				if (playerCombatComponent.bowLevel != 0) {
                    //TODO weapons
//					playerCombatComponent.equippedWeapon = playerCombatComponent.weaponInstances.bows[playerCombatComponent.bowLevel - 1];
//					playerCombatComponent.currentHeldItem = playerCombatComponent.equippedWeapon;
				}
			} else {
				if (playerCombatComponent.swordLevel != 0) {
//					playerCombatComponent.equippedWeapon = playerCombatComponent.weaponInstances.swords[playerCombatComponent.swordLevel-1];
//					playerCombatComponent.currentHeldItem = playerCombatComponent.equippedWeapon;
				}
			}
			SlimeBossRoomScreen.slimeBossDefeated = readSaveData() == 1;
		} catch (KryoBufferUnderflowException e) {
			//file was empty, don't load anything
		}
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

    public Entity getPlayer() {
        return currentScreen.player;
    }

    public Transform getPlayerTransform() {
        return Transform.MAPPER.get(currentScreen.player);
    }

    public WorldColliderComponent getPlayerCollider() {
        return WorldColliderComponent.MAPPER.get(currentScreen.player);
    }

    public RoutineListComponent getPlayerRoutineListComponent() {
        return RoutineListComponent.MAPPER.get(currentScreen.player);
    }

    public SpriteComponent getPlayerSpriteComponent() {
        return SpriteComponent.MAPPER.get(currentScreen.player);
    }

    public CombatComponent getPlayerCombatComponent() {
        return CombatComponent.MAPPER.get(currentScreen.player);
    }


    public PlayerCombatComponent getPlayerPlayerCombatComponent() {
        return PlayerCombatComponent.MAPPER.get(currentScreen.player);
    }

    public Circle getPlayerHitbox() {
        return getPlayerCollider().getHitbox(getPlayerTransform());
    }

    public float getPlayerX() { return getPlayerTransform().x; }
    public float getPlayerY() { return getPlayerTransform().y; }
    public float getPlayerXCentered() { return getPlayerTransform().getCenteredX(); }
    public float getPlayerYCentered() { return getPlayerTransform().getCenteredY(); }

	private void checkRespawn() {
        CombatComponent combatComponent = getPlayerCombatComponent();
        PlayerCombatComponent playerCombatComponent = getPlayerPlayerCombatComponent();

		if (combatComponent.dead) {
            playerCombatComponent.respawnTimer += Gdx.graphics.getDeltaTime();
			if (playerCombatComponent.respawnTimer > PlayerCombatComponent.RESPAWN_TIME) {
				//respawn
				Application.getInstance().caveScreen.updateCurrentMemento();
				CaveScreen.floor = 0;
				playerCombatComponent.respawnTimer -= PlayerCombatComponent.RESPAWN_TIME;
                combatComponent.hp = combatComponent.MAX_HP;
                combatComponent.dead = false;
                getPlayerSpriteComponent().visible = true;
                getPlayerRoutineListComponent().active = true;
                getPlayerCollider().active = true;
                setGameScreen(townScreen);
			}
		}
	}

	private void onApplicationExit() {
		if (RockType.playerHasAnyTempOre()) {
            PrefabInstantiator.addGrave(Application.getInstance().currentScreen);
			caveScreen.updateCurrentMemento();
		}
		writeSaveData();
	}

	private void writeSaveData() {
		if (currentScreen == caveScreen) {
			caveScreen.updateCurrentMemento();
		}
		ArrayList<Integer> saveData = new ArrayList<>();
        PlayerCombatComponent component = PlayerCombatComponent.MAPPER.get(getPlayer());
		saveData.add(component.swordLevel);
		saveData.add(component.bowLevel);
		for (int i = 0; i < RockType.SIZE; i++) {
			saveData.add(RockType.get(i).getOreAmount()- RockType.get(i).tempOreAmount);
		}
		int b = 0;
        //TODO weapons
//		if (component.bowLevel != 0 && component.equippedWeapon == component.weaponInstances.bows[component.bowLevel-1]) {
//			b = 1;
//		}
		saveData.add(b);
		saveData.add(SlimeBossRoomScreen.slimeBossDefeated ? 1:0);
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

    public boolean systemShouldTick() {
        return !timestop && !paused;
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
        Transform transform = Transform.MAPPER.get(gameScreen.player);
		transform.x = gameScreen.getInitialPlayerPosition().x;
        transform.y = gameScreen.getInitialPlayerPosition().y;
		if (currentScreen != null) {
			if (currentScreen != gameScreen) {
				currentScreen.onExit();
			}
		}
		if (currentScreen != null && currentScreen.song != null) {
			currentScreen.stopSong(gameScreen);
		}
		GameScreen old = currentScreen;
		currentScreen = gameScreen;
		setScreen(gameScreen);
		if (gameScreen.song != null) {
			if (old != null) {
				gameScreen.playSong(old);
			}
		}
		gameScreen.onEnter();
	}

	private void renderScreens() {
		super.render();
	}


	private void handleDebugInput() {
		// Handle pause button logic
		if (GameInput.isPauseButtonJustPressed()) {
			if (!(currentScreen instanceof TownScreen && ((TownScreen)currentScreen).isMainMenuActive())) {
				paused = !paused;
			}
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
			if (currentScreen == caveScreen && !paused) {
                Camera.setPositionDirectlyToPlayerPosition();
                setGameScreen(townScreen);
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
				getPlayerCombatComponent().hp = 1;
                getPlayerRoutineListComponent().takeDamage(new DamageInformation(1, 0, 1), getPlayer());
			}
		}
	}
}
