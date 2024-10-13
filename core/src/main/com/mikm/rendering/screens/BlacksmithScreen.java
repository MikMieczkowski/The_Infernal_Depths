package com.mikm.rendering.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mikm.Assets;
import com.mikm.debug.DebugRenderer;
import com.mikm.entities.Door;
import com.mikm.entities.NPC;
import com.mikm.input.GameInput;
import com.mikm.input.InputRaw;
import com.mikm.rendering.Camera;
import com.mikm.rendering.cave.RockType;

public class BlacksmithScreen extends GameScreen{
    private Color BG_COLOR = new Color(20/255f, 9/255f, 9/255f, 1);
    boolean[][] collidableGrid;
    public boolean showMenu = false;

    private TextureRegion menu = Assets.getInstance().getTextureRegion("UI", 97, 77);
    private TextureRegion[][] items = Assets.getInstance().getSplitTextureRegion("items");
    private TextureRegion npcImage = Assets.getInstance().getTextureRegion("blacksmith", 32, 32);
    private NPC npc;
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

    public void renderMenu() {
        if (showMenu) {
            drawComponentOnEdge(menu, true, false, false, 1, 0, 0);

            float w = Gdx.graphics.getWidth() * Camera.VIEWPORT_ZOOM;
            float h = Gdx.graphics.getHeight() * Camera.VIEWPORT_ZOOM;
            float magicSlopeX = 10;
            float magicInterceptX = 790;
            float magicSlopeY = 405f / 41;
            float magicInterceptY = 651.951219512f;

            float menuSlopeX = 5;
            float menuInterceptX = 450;
            float menuSlopeY = 405f / 39;
            float menuInterceptY = 270;

            int xOffset = (int) ((magicInterceptX - Gdx.graphics.getWidth()) / magicSlopeX);
            int yOffset = (int) ((magicInterceptY - Gdx.graphics.getHeight()) / magicSlopeY);
            Vector2 mousePos = new Vector2(InputRaw.mouseXPosition() + xOffset, InputRaw.mouseYPosition() + yOffset);
            int menuXOffset = (int) ((Gdx.graphics.getWidth() - menuInterceptX) / menuSlopeX);
            int menuYOffset = (int) ((Gdx.graphics.getHeight() - menuInterceptY) / menuSlopeY);

            Rectangle r1 = new Rectangle(menuXOffset + xOffset, menuYOffset + yOffset, 26, 26);
            Rectangle r2 = new Rectangle(menuXOffset + xOffset + 29, menuYOffset + yOffset, 26, 26);
            Rectangle r3 = new Rectangle(menuXOffset + xOffset + 29 * 2, menuYOffset + yOffset, 26, 26);

            if (Application.player.swordLevel <= 4) {
                Application.batch.draw(items[0][Application.player.swordLevel], menuXOffset + xOffset + 5, menuYOffset + yOffset + 7);
            }
            if (Application.player.bowLevel <= 4) {
                Application.batch.draw(items[0][4+Application.player.bowLevel], menuXOffset + xOffset + 5 + 29, menuYOffset + yOffset + 7);
            }
            if (GameInput.isAttackButtonJustPressed()) {
                if (r1.contains(mousePos) && Application.player.swordLevel < 4 && RockType.get(Application.player.swordLevel+1).oreAmount >= 5) {
                    Application.player.equippedWeapon = Application.player.weaponInstances.swords[Application.player.swordLevel];
                    RockType.get(Application.player.swordLevel+1).oreAmount -= 5;
                    Application.player.swordLevel++;
                    Application.player.currentHeldItem = Application.player.equippedWeapon;
                }
                if (r2.contains(mousePos) && Application.player.bowLevel < 4 && RockType.get(Application.player.bowLevel+1).oreAmount >= 5) {
                    Application.player.equippedWeapon = Application.player.weaponInstances.bows[Application.player.bowLevel];
                    RockType.get(Application.player.bowLevel+1).oreAmount -= 5;
                    Application.player.bowLevel++;
                    Application.player.currentHeldItem = Application.player.equippedWeapon;
                }
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
        viewport.setUnitsPerPixel(Camera.VIEWPORT_ZOOM);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        tiledMapRenderer.setView(Camera.orthographicCamera);
        for (int i = 0; i < RockType.SIZE; i++) {
            if (RockType.get(i).oreAmount >= 5) {
                if (Application.player.swordLevel < i) {
                    System.out.println("Press E to upgrade sword to" + RockType.get(i).name());
                }
                if (Application.player.bowLevel < i) {
                    System.out.println("Press E to upgrade bow to " + RockType.get(i).name());
                }
            }
        }
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
}
