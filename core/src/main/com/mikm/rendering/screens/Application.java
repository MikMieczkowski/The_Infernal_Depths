package com.mikm.rendering.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.mikm.ExtraMathUtils;
import com.mikm.Vector2Int;
import com.mikm.entities.NPC;
import com.mikm.entities.animation.ActionSpritesheetsAllDirections;
import com.mikm.entities.animation.AnimationsAlphabeticalIndex;
import com.mikm.entities.animation.EntityActionSpritesheets;
import com.mikm.entities.player.Player;
import com.mikm.entities.player.weapons.WeaponInstances;
import com.mikm.input.InputRaw;
import com.mikm.rendering.Camera;
import com.mikm.rendering.TextureAtlasUtils;

import java.util.ArrayList;

public class Application extends Game {
	public static final int TILE_WIDTH = 16, TILE_HEIGHT = 16;
	//480,270
	//public static final int WORLD_WIDTH = 1440, WORLD_HEIGHT = 810;

	SpriteBatch batch;
	public CaveScreen caveScreen;
	private TownScreen townScreen;
	public SlimeBossRoomScreen slimeBossRoomScreen;

	public static GameScreen currentScreen;
	public static Player player;
	public static TextureRegion testTexture;
	private AssetManager assetManager;
	private TextureAtlas textureAtlas;
	public static BitmapFont font;
	public static boolean timestop;
	private static int timeStopFrames;
	private static final int MAX_TIMESTOP_FRAMES = 10;

	public static final boolean PLAY_MUSIC = false;

	public static ShaderProgram fillColorShader;
	public static TextureRegion light;
	public static TextureRegion dark;
	public static TextureRegion gray;

	@Override
	public void create() {
		batch = new SpriteBatch();
		fillColorShader = new ShaderProgram(batch.getShader().getVertexShaderSource(), Gdx.files.internal("images/fillColor.frag").readString());
		if (!fillColorShader.isCompiled()){
			throw new RuntimeException(fillColorShader.getLog());
		}

		assetManager = createAssetManager();
		textureAtlas = assetManager.get("images/The Infernal Depths.atlas", TextureAtlas.class);
		testTexture = textureAtlas.findRegion("sand").split(TILE_WIDTH, TILE_HEIGHT)[0][0];

		font = new BitmapFont(Gdx.files.internal("fonts/EquipmentPro.fnt"));

		createPlayerAndCaveScreen(textureAtlas);
		townScreen = new TownScreen(this, textureAtlas);
		slimeBossRoomScreen = new SlimeBossRoomScreen(this,caveScreen, textureAtlas);
		townScreen.addInanimateEntity(new NPC(player.entityActionSpritesheets.standing.list.get(0)[0], 50, 50));
		light = new TextureRegion(new Texture(Gdx.files.internal("images/R0x4x.png")));
		dark = new TextureRegion(new Texture(Gdx.files.internal("images/dark.png")));
		gray = new TextureRegion(new Texture(Gdx.files.internal("images/gray.png")));
		player.x = 100;
		player.y = 100;
		setGameScreen(townScreen);

	}

	public void setGameScreen(GameScreen gameScreen) {
		currentScreen = gameScreen;
		setScreen(gameScreen);
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
		if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
			if (currentScreen == caveScreen) {
				player.x = 100;
				player.y = 100;
				Camera.setPositionDirectlyToPlayerPosition();
				setGameScreen(townScreen);
			} else {
				Vector2Int playerPosition = spawnablePosition();
				player.x = playerPosition.x;
				player.y = playerPosition.y;
				Camera.setPositionDirectlyToPlayerPosition();
				setGameScreen(caveScreen);
			}
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
			caveScreen.increaseFloor();
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
			freezeTime();
		}
	}

	public void putPlayerInOpenTile() {
		Vector2Int playerPosition = spawnablePosition();
		player.x = playerPosition.x;
		player.y = playerPosition.y;
		Camera.setPositionDirectlyToPlayerPosition();
	}


	@Override
	public void dispose() {
		textureAtlas.dispose();
		assetManager.dispose();
		batch.dispose();
		caveScreen.dispose();
		townScreen.dispose();
		slimeBossRoomScreen.dispose();
		fillColorShader.dispose();
	}

	private void renderScreens() {
		super.render();
	}

	private AssetManager createAssetManager() {
		AssetManager assetManager = new AssetManager();
		assetManager.load("images/The Infernal Depths.atlas", TextureAtlas.class);
		assetManager.load("sound/caveTheme.mp3", Music.class);
		assetManager.finishLoading();
		return assetManager;
	}

	private void createPlayerAndCaveScreen(TextureAtlas textureAtlas) {
		EntityActionSpritesheets playerActionSpritesheets = createPlayerActionSpritesheets();
		player = new Player(500, 500, playerActionSpritesheets);
		caveScreen = new CaveScreen(this, assetManager.get("sound/caveTheme.mp3", Music.class), textureAtlas);
		player.setWeapons(new WeaponInstances(caveScreen, textureAtlas));
		Vector2Int playerPosition = spawnablePosition();
		player.x = playerPosition.x;
		player.y = playerPosition.y;
		Camera.setPositionDirectlyToPlayerPosition();
	}

	private EntityActionSpritesheets createPlayerActionSpritesheets() {
		ArrayList<TextureRegion[]> playerSpritesheetsRaw = TextureAtlasUtils.findSplitTextureRegionsStartingWith("Character", textureAtlas, 32, 32);

		EntityActionSpritesheets output = new EntityActionSpritesheets();
		output.hit = playerSpritesheetsRaw.get(AnimationsAlphabeticalIndex.PLAYER_DIVE_STARTING_INDEX)[0];
		output.standing = ActionSpritesheetsAllDirections.createFromSpritesheetRange(playerSpritesheetsRaw, AnimationsAlphabeticalIndex.PLAYER_WALK_STARTING_INDEX, true);
		output.walking = ActionSpritesheetsAllDirections.createFromSpritesheetRange(playerSpritesheetsRaw, AnimationsAlphabeticalIndex.PLAYER_WALK_STARTING_INDEX);

		output.playerAttacking = ActionSpritesheetsAllDirections.createFromSpritesheetRange(playerSpritesheetsRaw, AnimationsAlphabeticalIndex.PLAYER_WALK_STARTING_INDEX);
		output.playerDiving = ActionSpritesheetsAllDirections.createFromSpritesheetRange(playerSpritesheetsRaw, AnimationsAlphabeticalIndex.PLAYER_DIVE_STARTING_INDEX);
		output.playerRolling = ActionSpritesheetsAllDirections.createFromSpritesheetRange(playerSpritesheetsRaw, AnimationsAlphabeticalIndex.PLAYER_ROLL_STARTING_INDEX);
		return output;
	}

	private Vector2Int spawnablePosition() {
		ArrayList<Vector2Int> openTilePositions = caveScreen.getOpenTilePositionsArray();
		if (openTilePositions.size() == 0) {
			return Vector2Int.ZERO;
		}
		Vector2Int spawnLocationInTiles = openTilePositions.get(ExtraMathUtils.randomInt(openTilePositions.size()-1));
		return new Vector2Int(spawnLocationInTiles.x * Application.TILE_WIDTH + (int)player.getBoundsOffset().x,
				spawnLocationInTiles.y * Application.TILE_HEIGHT + (int)player.getBoundsOffset().y);
	}

	public static void freezeTime() {
		timestop = true;
	}

	public static void setFillColorShader(Batch batch, Color color) {
		batch.setShader(fillColorShader);
		fillColorShader.bind();
		fillColorShader.setUniformf("u_color", color);
	}
}
