package com.mikm.rendering.screens;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mikm._components.SpriteComponent;
import com.mikm.entities.prefabLoader.PrefabInstantiator;
import com.mikm.utils.Assets;
import com.mikm.utils.RandomUtils;
import com.mikm._components.PlayerCombatComponent;
import com.mikm._components.ShadowComponent;
import com.mikm._components.Transform;
import com.mikm.input.GameInput;
import com.mikm.input.InputRaw;
import com.mikm.rendering.Camera;
import com.mikm.rendering.sound.SoundEffects;
import com.mikm.rendering.cave.RockType;

public class BlacksmithScreen extends GameScreen{
    private Color BG_COLOR = new Color(20/255f, 9/255f, 9/255f, 1);
    boolean[][] collidableGrid;
    public static boolean showMenu = false;

    private TextureRegion menu = Assets.getInstance().getTextureRegion("UI", 97, 77);
    private TextureRegion[][] tips = Assets.getInstance().getSplitTextureRegion("tipsText", 250, 100);
    private TextureRegion selector = Assets.getInstance().getTextureRegion("UISelector", 29, 29);
    private TextureRegion[][] items = Assets.getInstance().getSplitTextureRegion("items");
    private TextureRegion npcImage = Assets.getInstance().getTextureRegion("blacksmith", 32, 32);
    //TODO NPC
    //private NPC npc;
    private final int WEAPON_PRICE_IN_ORES = 7;
    private int selected = 1;
    private float menuXOffset, menuYOffset, mouseXOffset, mouseYOffset;
    private Rectangle r1, r2, r3;
    private Vector2 mousePos;
    private int lastControllerX = 0;
    public static int talkedToTimes = 0;

    public int tipNumber = 0;

    private String MENU_DENY_SOUND_EFFECT = "menuDeny.ogg";
    public static  String BLACKSMITH_ANNOYED_SOUND_EFFECT = "blacksmithAnnoyed.ogg";
    private String REWARD_SOUND_EFFECT = "reward.ogg";
    public static final String FIRE_AMBIENCE = "fireCrackle.ogg";

    private final float MAX_TIME_GRUNT = 2;
    private float timeSinceGruntTimer = MAX_TIME_GRUNT;

    BlacksmithScreen() {
        super();
        //TODO tidy assets/tiled: remove images, rename, and ensure internal directories
        tiledMap = new TmxMapLoader().load("tiled/BlacksmithScreen.tmx");
        collidableGrid = readCollisionTiledmapLayer(1, 9, 9);
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1);
        PrefabInstantiator.addDoor(this, 88+16, 24, 1);
        createMusic(Assets.getInstance().getAsset("sound/caveThemeOld.mp3", Music.class));

        Entity e = PrefabInstantiator.addEntity("npc", this, 77+4, 67+4);
        SpriteComponent.MAPPER.get(e).textureRegion = npcImage;

        ShadowComponent.MAPPER.get(player).active = false;
    }

    @Override
    public void render(float delta) {
        timeSinceGruntTimer+= Gdx.graphics.getDeltaTime();
        ScreenUtils.clear(BG_COLOR);
        super.lockCameraAt(79,66);

        super.setRenderCamera(false);
        super.setRenderUI(false);
        super.render(delta);
    }

    @Override
    protected void drawAssetsPostEntities() {
        renderUI();
        renderMenu();
    }

    public void renderMenu() {
        if (showMenu) {
            updateScreenCoordinates();
            drawComponentOnEdge(menu, 5, 1, 0, 0);
            drawComponentOnEdge(tips[tipNumber][0], 5, 0.4f, 10, 13);
            /*
            Camera.VIEWPORT_ZOOM = 1f;
            viewport.setUnitsPerPixel(Camera.VIEWPORT_ZOOM);
            resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            Assets.font.draw(Application.batch, "Welcome to my shop. If you bring me ores I will make you a weapon so that you can slay monsters.", -500, 100, 200, -1, true);
            */
            if (InputRaw.usingController) {
                handleControllerBuying();
            } else {
                handleMouseBuying();
            }
            drawMenuComponents();

            if (GameInput.isMenuCancelButtonJustPressed()) {
                showMenu = false;
            }
        }
    }

    private void handleControllerBuying() {
        Application.batch.draw(selector, r1.x+(selected-1)*29-2, r1.y+1);
        if (lastControllerX != 0 && GameInput.getHorizontalAxisInt() == 0) {
            selected += lastControllerX;
            selected = MathUtils.clamp(selected, 1, 3);
        }
        lastControllerX = GameInput.getHorizontalAxisInt();

        PlayerCombatComponent playerCombatComponent = PlayerCombatComponent.MAPPER.get(Application.getInstance().getPlayer());
        
        if (GameInput.isAttackButtonJustPressed() || GameInput.isDiveButtonJustPressed()) {
            if (selected == 1 && playerCombatComponent.swordLevel < 4) {
                if (RockType.get(playerCombatComponent.swordLevel + 1).getOreAmount() >= WEAPON_PRICE_IN_ORES) {
                    buy(true);
                } else {
                    playDenySound();
                }
            } else if (selected == 2 && playerCombatComponent.bowLevel < 4) {
                if (RockType.get(playerCombatComponent.bowLevel + 1).getOreAmount() >= WEAPON_PRICE_IN_ORES) {
                    buy(false);
                } else {
                    playDenySound();
                }
            } else if (selected == 3) {
                switchWeapon();
            }
        }
    }

    private void playDenySound() {
        SoundEffects.play(MENU_DENY_SOUND_EFFECT);
        if (timeSinceGruntTimer > MAX_TIME_GRUNT) {
            SoundEffects.play(BLACKSMITH_ANNOYED_SOUND_EFFECT);
            timeSinceGruntTimer -= MAX_TIME_GRUNT;
        }
    }

    private void switchWeapon() {
        PlayerCombatComponent playerCombatComponent = PlayerCombatComponent.MAPPER.get(Application.getInstance().getPlayer());
        if (playerCombatComponent.swordLevel == 0 || playerCombatComponent.bowLevel == 0) {
            return;
        }
        //TODO weapon
//        if (playerCombatComponent.equippedWeapon != playerCombatComponent.weaponInstances.swords[playerCombatComponent.swordLevel-1]) {
//            playerCombatComponent.equippedWeapon = playerCombatComponent.weaponInstances.swords[playerCombatComponent.swordLevel - 1];
//        } else {
//            playerCombatComponent.equippedWeapon = playerCombatComponent.weaponInstances.bows[playerCombatComponent.bowLevel - 1];
//        }
//        playerCombatComponent.currentHeldItem = playerCombatComponent.equippedWeapon;
    }
    private void handleMouseBuying() {
        PlayerCombatComponent playerCombatComponent = PlayerCombatComponent.MAPPER.get(Application.getInstance().getPlayer());
        if (GameInput.isAttackButtonJustPressed()) {
            if (r1.contains(mousePos) && playerCombatComponent.swordLevel < 4) {
                if (RockType.get(playerCombatComponent.swordLevel + 1).getOreAmount() >= WEAPON_PRICE_IN_ORES) {
                    buy(true);
                } else {
                    playDenySound();
                }
            }
            if (r2.contains(mousePos) && playerCombatComponent.bowLevel < 4) {
                if (RockType.get(playerCombatComponent.bowLevel + 1).getOreAmount() >= WEAPON_PRICE_IN_ORES) {
                    buy(false);
                } else {
                    playDenySound();
                }
            }
            if (r3.contains(mousePos)) {
                switchWeapon();
            }
        }
    }

    private void buy(boolean sword) {
        PlayerCombatComponent playerCombatComponent = PlayerCombatComponent.MAPPER.get(Application.getInstance().getPlayer());
        SoundEffects.play(REWARD_SOUND_EFFECT);
        SoundEffects.play("hammerBuilt.ogg");
        if (sword) {
            //TODO weapon
            //playerCombatComponent.equippedWeapon = playerCombatComponent.weaponInstances.swords[playerCombatComponent.swordLevel];
            RockType.get(playerCombatComponent.swordLevel + 1).increaseOreAmount(-WEAPON_PRICE_IN_ORES);
            RockType.validateOres();
            playerCombatComponent.swordLevel++;
        } else {
            //playerCombatComponent.equippedWeapon = playerCombatComponent.weaponInstances.bows[playerCombatComponent.bowLevel];
            RockType.get(playerCombatComponent.bowLevel + 1).increaseOreAmount(-WEAPON_PRICE_IN_ORES);
            RockType.validateOres();
            //playerCombatComponent.bowLevel++;
            tipNumber = 1;
        }
        //playerCombatComponent.currentHeldItem = playerCombatComponent.equippedWeapon;
    }

    private void updateScreenCoordinates() {
        float magicSlopeX = 10;
        float magicInterceptX = 790;
        float magicSlopeY = 405f / 41;
        float magicInterceptY = 651.951219512f;

        float menuSlopeX = 5;
        float menuInterceptX = 450;
        float menuSlopeY = 405f / 39;
        float menuInterceptY = 270;

        mouseXOffset = (int) ((magicInterceptX - Gdx.graphics.getWidth()) / magicSlopeX);
        mouseYOffset = (int) ((magicInterceptY - Gdx.graphics.getHeight()) / magicSlopeY);
        mousePos = new Vector2(InputRaw.mouseXPosition() + mouseXOffset, InputRaw.mouseYPosition() + mouseYOffset);
        menuXOffset = (int) ((Gdx.graphics.getWidth() - menuInterceptX) / menuSlopeX) + mouseXOffset;
        menuYOffset = (int) ((Gdx.graphics.getHeight() - menuInterceptY) / menuSlopeY) + mouseYOffset;

        r1 = new Rectangle(menuXOffset, menuYOffset, 26, 26);
        r2 = new Rectangle(menuXOffset + 29, menuYOffset, 26, 26);
        r3 = new Rectangle(menuXOffset + 29 * 2, menuYOffset, 26, 26);
    }

    private void drawMenuComponents() {
        PlayerCombatComponent playerCombatComponent = PlayerCombatComponent.MAPPER.get(Application.getInstance().getPlayer());
        if (playerCombatComponent.swordLevel < 4) {
            Application.batch.draw(items[0][playerCombatComponent.swordLevel], menuXOffset+ 5, menuYOffset + 7);
            if (RockType.get(playerCombatComponent.swordLevel+1).getOreAmount() >= WEAPON_PRICE_IN_ORES) {
                Application.batch.setColor(Color.LIME);
            } else {
                Application.batch.setColor(Color.RED);
            }
            Application.batch.draw(Assets.numbers[WEAPON_PRICE_IN_ORES], menuXOffset + 2, menuYOffset -4);
            Application.batch.setColor(Color.WHITE);
        }
        if (playerCombatComponent.bowLevel < 4) {
            Application.batch.draw(items[0][4+playerCombatComponent.bowLevel], menuXOffset + 5 + 29, menuYOffset + 7);
            if (RockType.get(playerCombatComponent.bowLevel+1).getOreAmount() >= WEAPON_PRICE_IN_ORES) {
                Application.batch.setColor(Color.LIME);
            } else {
                Application.batch.setColor(Color.RED);
            }
            Application.batch.draw(Assets.numbers[WEAPON_PRICE_IN_ORES], menuXOffset + 2 + 29, menuYOffset -4);
            Application.batch.setColor(Color.WHITE);
        }
        if (playerCombatComponent.swordLevel !=0 && playerCombatComponent.bowLevel != 0) {
            //TODO weapon
//            if (playerCombatComponent.equippedWeapon == playerCombatComponent.weaponInstances.bows[playerCombatComponent.bowLevel-1]) {
//                Application.batch.draw(items[0][playerCombatComponent.swordLevel-1], menuXOffset+ 5+29*2, menuYOffset + 7);
//            } else {
//                Application.batch.draw(items[0][4+playerCombatComponent.bowLevel-1], menuXOffset+ 5+29*2, menuYOffset + 7);
//            }
        }
    }


    @Override
    public boolean[][] isCollidableGrid() {
        return collidableGrid;
    }

    @Override
    public void onEnter() {
        if (!SoundEffects.loopIsPlaying(FIRE_AMBIENCE)) {
            SoundEffects.playLoop(FIRE_AMBIENCE);
        }
        SoundEffects.setLoopVolume(FIRE_AMBIENCE, 1);
        talkedToTimes = 0;
        Camera.VIEWPORT_ZOOM = .2f;
        tipNumber = RandomUtils.getBoolean() ? 0 : 2;
        selected = 1;
        viewport.setUnitsPerPixel(Camera.VIEWPORT_ZOOM);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        tiledMapRenderer.setView(Camera.orthographicCamera);
    }

    @Override
    public void onExit() {
        Camera.VIEWPORT_ZOOM = Camera.DEFAULT_VIEWPORT_ZOOM;
        viewport.setUnitsPerPixel(Camera.VIEWPORT_ZOOM);

        Transform playerTransform = Application.getInstance().getPlayerTransform();
        playerTransform.x = 378;
        playerTransform.y = 338;
        showMenu = false;
    }

    @Override
    public Vector2 getInitialPlayerPosition() {
        return new Vector2(80, 32);
    }

    @Override
    public int getMapWidth() {
        return 9;
    }

    @Override
    public int getMapHeight() {
        return 9;
    }
}
