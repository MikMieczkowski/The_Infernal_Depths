package com.mikm.rendering.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mikm.Assets;
import com.mikm.RandomUtils;
import com.mikm.debug.DebugRenderer;
import com.mikm.entities.Door;
import com.mikm.entities.NPC;
import com.mikm.input.GameInput;
import com.mikm.input.InputRaw;
import com.mikm.rendering.Camera;
import com.mikm.rendering.SoundEffects;
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
    private NPC npc;
    private final int WEAPON_PRICE_IN_ORES = 7;
    private int selected = 1;
    private float menuXOffset, menuYOffset, mouseXOffset, mouseYOffset;
    private Rectangle r1, r2, r3;
    private Vector2 mousePos;
    private int lastControllerX = 0;

    public int tipNumber = 0;
    BlacksmithScreen() {
        super();
        collidableGrid = new boolean[][]{
                {true, true, true, true, true, true, true, true, true},
                {true, true, true, true, true, true, true, true, true},
                {true, true, true, true, true, false, false, true, true},
                {true, true, false, false, false, false, false, true, true},
                {true, true, true, true, false, true, true, true, true},
                {true, true, true, true, false, false, false, true, true},
                {true, true, true, true, true, false, true, true, true},
                {true, true, true, true, true, true, true, true, true},
                {true, true, true, true, true, true, true, true, true}
        };
        tiledMap = new TiledMap();
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1);
        entities.add(new BlacksmithRoom(0, -4));
        addInanimateEntity(new Door(88, 24, 1));
        npc = new NPC(77, 67);
        createMusic(Assets.getInstance().getAsset("sound/caveThemeOld.mp3", Music.class));
        addInanimateEntity(npc);
        removeInanimateEntity(Application.player.shadow);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(BG_COLOR);
        Camera.x = 79;
        Camera.y = 66;
        Camera.orthographicCamera.position.set(Camera.x, Camera.y, 0);
        Camera.orthographicCamera.update();
        Application.batch.begin();
        Application.batch.setProjectionMatrix(Camera.orthographicCamera.combined);
        tiledMapRenderer.setView(Camera.orthographicCamera);
        drawAssets();
        DebugRenderer.getInstance().update();
        Camera.renderLighting(Application.batch);
        Camera.orthographicCamera.update();
        Application.batch.draw(npcImage, 77, 67);
        if (npc.isPlayerInTalkingRange()) {
            Application.batch.draw(GameInput.getTalkButtonImage(), 77+8, 67+8+16);
        }
        renderUI();
        renderMenu();
        Application.batch.end();
    }

    @Override
    public void drawOther() {
        Application.batch.draw(npcImage, 77, 67);
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
        }
    }

    private void handleControllerBuying() {
        Application.batch.draw(selector, r1.x+(selected-1)*29-2, r1.y+1);
        if (lastControllerX != 0 && GameInput.getHorizontalAxisInt() == 0) {
            selected += lastControllerX;
            selected = MathUtils.clamp(selected, 1, 3);
        }
        lastControllerX = GameInput.getHorizontalAxisInt();
        if (GameInput.isAttackButtonJustPressed() || GameInput.isDiveButtonJustPressed()) {
            if (selected == 1 && Application.player.swordLevel < 4) {
                if (RockType.get(Application.player.swordLevel + 1).getOreAmount() >= WEAPON_PRICE_IN_ORES) {
                    buy(true);
                } else {
                    SoundEffects.play(SoundEffects.menuDeny);
                }
            } else if (selected == 2 && Application.player.bowLevel < 4) {
                if (RockType.get(Application.player.bowLevel + 1).getOreAmount() >= WEAPON_PRICE_IN_ORES) {
                    buy(false);
                } else {
                    SoundEffects.play(SoundEffects.menuDeny);
                }
            } else if (selected == 3) {
                switchWeapon();
            }
        }
    }

    private void switchWeapon() {
        if (Application.player.swordLevel == 0 || Application.player.bowLevel == 0) {
            return;
        }
        if (Application.player.equippedWeapon != Application.player.weaponInstances.swords[Application.player.swordLevel-1]) {
            Application.player.equippedWeapon = Application.player.weaponInstances.swords[Application.player.swordLevel - 1];
        } else {
            Application.player.equippedWeapon = Application.player.weaponInstances.bows[Application.player.bowLevel - 1];
        }
        Application.player.currentHeldItem = Application.player.equippedWeapon;
    }
    private void handleMouseBuying() {
        if (GameInput.isAttackButtonJustPressed()) {
            if (r1.contains(mousePos) && Application.player.swordLevel < 4) {
                if (RockType.get(Application.player.swordLevel + 1).getOreAmount() >= WEAPON_PRICE_IN_ORES) {
                    buy(true);
                } else {
                    SoundEffects.play(SoundEffects.menuDeny);
                }
            }
            if (r2.contains(mousePos) && Application.player.bowLevel < 4) {
                if (RockType.get(Application.player.bowLevel + 1).getOreAmount() >= WEAPON_PRICE_IN_ORES) {
                    buy(false);
                } else {
                    SoundEffects.play(SoundEffects.menuDeny);
                }
            }
            if (r3.contains(mousePos)) {
                switchWeapon();
            }
        }
    }

    private void buy(boolean sword) {
        SoundEffects.play(SoundEffects.reward);
        if (sword) {
            Application.player.equippedWeapon = Application.player.weaponInstances.swords[Application.player.swordLevel];
            RockType.get(Application.player.swordLevel + 1).increaseOreAmount(-WEAPON_PRICE_IN_ORES);
            RockType.validateOres();
            Application.player.swordLevel++;
        } else {
            Application.player.equippedWeapon = Application.player.weaponInstances.bows[Application.player.bowLevel];
            RockType.get(Application.player.bowLevel + 1).increaseOreAmount(-WEAPON_PRICE_IN_ORES);
            RockType.validateOres();
            Application.player.bowLevel++;
            tipNumber = 1;
        }
        Application.player.currentHeldItem = Application.player.equippedWeapon;
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
        if (Application.player.swordLevel < 4) {
            Application.batch.draw(items[0][Application.player.swordLevel], menuXOffset+ 5, menuYOffset + 7);
            if (RockType.get(Application.player.swordLevel+1).getOreAmount() >= WEAPON_PRICE_IN_ORES) {
                Application.batch.setColor(Color.LIME);
            } else {
                Application.batch.setColor(Color.RED);
            }
            Application.batch.draw(Assets.numbers[WEAPON_PRICE_IN_ORES], menuXOffset + 2, menuYOffset -4);
            Application.batch.setColor(Color.WHITE);
        }
        if (Application.player.bowLevel < 4) {
            Application.batch.draw(items[0][4+Application.player.bowLevel], menuXOffset + 5 + 29, menuYOffset + 7);
            if (RockType.get(Application.player.bowLevel+1).getOreAmount() >= WEAPON_PRICE_IN_ORES) {
                Application.batch.setColor(Color.LIME);
            } else {
                Application.batch.setColor(Color.RED);
            }
            Application.batch.draw(Assets.numbers[WEAPON_PRICE_IN_ORES], menuXOffset + 2 + 29, menuYOffset -4);
            Application.batch.setColor(Color.WHITE);
        }
        if (Application.player.swordLevel !=0 && Application.player.bowLevel != 0) {
            if (Application.player.equippedWeapon == Application.player.weaponInstances.bows[Application.player.bowLevel-1]) {
                Application.batch.draw(items[0][Application.player.swordLevel-1], menuXOffset+ 5+29*2, menuYOffset + 7);
            } else {
                Application.batch.draw(items[0][4+Application.player.bowLevel-1], menuXOffset+ 5+29*2, menuYOffset + 7);
            }
        }
    }


    @Override
    public boolean[][] isCollidableGrid() {
        return collidableGrid;
    }

    @Override
    public void onEnter() {
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
        Application.player.x = 378;
        Application.player.y = 338;
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
