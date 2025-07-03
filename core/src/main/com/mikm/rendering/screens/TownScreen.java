package com.mikm.rendering.screens;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mikm.Assets;
import com.mikm.entities.Door;
import com.mikm.entities.enemies.Slime;
import com.mikm.rendering.Camera;
import com.mikm.rendering.cave.RockType;
import com.badlogic.gdx.Gdx;
import com.mikm.input.GameInput;
import com.badlogic.gdx.math.Rectangle;
import com.mikm.debug.DebugRenderer;

public class TownScreen extends GameScreen {

    private static TextureRegion stallroof = Assets.getInstance().getTextureRegion("stallroof", 80, 48);
    private static TextureRegion blacksmithroof = Assets.getInstance().getTextureRegion("blacksmithRoof", 80, 96);
    private static TextureRegion jarBugFix = Assets.getInstance().getTextureRegion("weirdJarBug", 80, 96);
    private static TextureRegion tree = Assets.getInstance().getTextureRegion("treetop", 48, 48);
    private static TextureRegion roof = Assets.getInstance().getTextureRegion("houseRoof", 64,96);
    private boolean[][] collidableGrid;
    private boolean[][] holePositions;
    private boolean showMainMenu = true;
    private boolean hasSaveFile = false;
    private boolean showOverwritePrompt = false;
    private float masterVolume = 1.0f;
    private boolean musicOn = true;
    private TextureRegion menuBg = Assets.getInstance().getTextureRegion("UI", 97, 77);
    private TextureRegion selector = Assets.getInstance().getTextureRegion("UISelector", 29, 29);
    private TextureRegion soundIcon = Assets.getInstance().getTextureRegion("UI", 32, 32); // Placeholder, adjust as needed
    private TextureRegion titleImage = Assets.getInstance().getTextureRegion("UI", 97, 77); // Placeholder, adjust as needed
    private int selectedMenuIndex = 0;
    private final String[] menuOptions = {"Continue", "Start", "Music", "Volume"};
    private Rectangle[] menuOptionRects = new Rectangle[4];
    private Rectangle volumeBarRect;
    private boolean mouseOverVolume = false;
    private boolean mousePressedLastFrame = false;
    private boolean draggingVolumeBar = false;
    private float frozenCameraX = Float.NaN;
    private float frozenCameraY = Float.NaN;

    TownScreen() {
        super();
        tiledMap = new TmxMapLoader().load("Overworld.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1);
        tiledMapRenderer.setView(Camera.orthographicCamera);
        createMusic(Assets.getInstance().getAsset("sound/townTheme.mp3", Music.class));
        collidableGrid = readCollisionTiledmapLayer(2, getMapWidth(), getMapHeight());
        holePositions = readCollisionTiledmapLayer(3, getMapWidth(), getMapHeight());
        readAndCreateDestructiblesTiledmapLayer(1, getMapWidth(), getMapHeight());

        int offset = 15*16;
        addInanimateEntity(new Door(offset+3*16, offset+13*16, 4));
        addInanimateEntity(new Door(offset+9*16, offset+8*16, 3));
        //addInanimateEntity(new NPC(Assets.testTexture, 50, 50));

        // Check for save files
        hasSaveFile = checkForSaveFiles();
    }

    private boolean checkForSaveFiles() {
        for (int i = 0; i <= 10; i++) {
            java.io.File f = new java.io.File(Gdx.files.getLocalStoragePath() + "InfernalDepthsSaveFiles/save" + i + ".bin");
            if (f.exists() && f.length() > 0) return true;
        }
        return false;
    }

    @Override
    public void render(float delta) {
        if (showMainMenu) {
            Application.getInstance().timestop = true;
            ScreenUtils.clear(Color.BLACK);
            // Do NOT call camera.update() here
            Application.batch.begin();
            Application.batch.setProjectionMatrix(Camera.orthographicCamera.combined);
            tiledMapRenderer.setView(Camera.orthographicCamera);
            drawAssets();
            DebugRenderer.getInstance().update();
            Camera.renderLighting(Application.batch);
            Camera.updateOrthographicCamera();
            renderUI();
            Application.batch.end();
            renderMainMenu();
        } else {
            Application.getInstance().timestop = false;
            super.render(delta);
        }
    }

    private void renderMainMenu() {
        Camera.orthographicCamera.update();
        Application.batch.begin();
        Application.batch.setProjectionMatrix(Camera.orthographicCamera.combined);
        // Move menu background and title closer to top left
        drawComponentOnEdge(menuBg, 0, 2f, 10, 60); // top left, minimal padding
        drawComponentOnEdge(titleImage, 0, 1.5f, 20, 120); // top left, below background
        // Draw menu options and calculate their rectangles
        int yStart = 60;
        int yStep = 40;
        int optionCount = hasSaveFile ? 4 : 3; // Hide Continue if no save
        int drawIndex = 0;
        float baseX = Camera.orthographicCamera.position.x - 100;
        float baseY = Camera.orthographicCamera.position.y + yStart;
        for (int i = 0; i < optionCount; i++) {
            float y = baseY - yStep * i;
            menuOptionRects[i] = new Rectangle(baseX - 10, y - 20, 180, 32);
        }
        int drawMenuIdx = 0;
        if (hasSaveFile) {
            drawMenuOption("Continue", drawMenuIdx, yStart, yStep, baseX);
            drawMenuIdx++;
        }
        drawMenuOption("Start", drawMenuIdx, yStart, yStep, baseX);
        drawMenuIdx++;
        drawMenuOption("Music: " + (musicOn ? "On" : "Off"), drawMenuIdx, yStart, yStep, baseX);
        drawMenuIdx++;
        drawMenuOption("Volume", drawMenuIdx, yStart, yStep, baseX);
        // Draw volume bar and set its rectangle
        int volumeBarY = yStart - yStep * 3;
        drawVolumeBar(volumeBarY, baseX);
        float barX = baseX + 40;
        float barY = Camera.orthographicCamera.position.y + volumeBarY;
        float barWidth = 100;
        float barHeight = 10;
        volumeBarRect = new Rectangle(barX, barY, barWidth, barHeight);
        // Draw selector (keyboard/controller or mouse hover)
        int selectorIndex = getMenuSelectorIndex(optionCount);
        drawSelector(yStart, yStep, selectorIndex, baseX);
        Application.batch.end();
        handleMenuInput(optionCount, selectorIndex);
    }

    private void drawMenuOption(String text, int index, int yStart, int yStep, float baseX) {
        Assets.font.draw(Application.batch, text, baseX, Camera.orthographicCamera.position.y + yStart - yStep * index);
    }

    private int getMenuSelectorIndex(int optionCount) {
        // Mouse hover takes priority
        Vector2 mouse = GameInput.getMousePos();
        for (int i = 0; i < optionCount; i++) {
            if (menuOptionRects[i] != null && menuOptionRects[i].contains(mouse)) {
                return i;
            }
        }
        return selectedMenuIndex;
    }

    private void drawSelector(int yStart, int yStep, int selectorIndex, float baseX) {
        Application.batch.draw(selector, baseX - 20, Camera.orthographicCamera.position.y + yStart - yStep * selectorIndex - 10);
    }

    private void drawVolumeBar(int y, float baseX) {
        float barX = baseX + 40;
        float barY = Camera.orthographicCamera.position.y + y;
        float barWidth = 100;
        float barHeight = 10;
        // Draw background
        Application.batch.setColor(Color.DARK_GRAY);
        Application.batch.draw(menuBg, barX, barY, barWidth, barHeight);
        // Draw filled part
        Application.batch.setColor(Color.GREEN);
        Application.batch.draw(menuBg, barX, barY, barWidth * masterVolume, barHeight);
        Application.batch.setColor(Color.WHITE);
    }

    private void handleMenuInput(int optionCount, int selectorIndex) {
        // Keyboard/controller navigation
        int horiz = GameInput.getHorizontalAxisInt();
        int vert = GameInput.getVerticalAxisInt();
        boolean attackPressed = GameInput.isAttackButtonJustPressed();
        // Only update selectedMenuIndex if not hovering with mouse
        Vector2 mouse = GameInput.getMousePos();
        boolean mouseHover = false;
        for (int i = 0; i < optionCount; i++) {
            if (menuOptionRects[i] != null && menuOptionRects[i].contains(mouse)) {
                mouseHover = true;
                break;
            }
        }
        if (!mouseHover) {
            if (vert == 1) {
                selectedMenuIndex = (selectedMenuIndex - 1 + optionCount) % optionCount;
            } else if (vert == -1) {
                selectedMenuIndex = (selectedMenuIndex + 1) % optionCount;
            }
        } else {
            selectedMenuIndex = selectorIndex;
        }
        // Mouse click or attack button
        boolean mousePressed = GameInput.isAttackButtonJustPressed();
        boolean mouseHeld = GameInput.isAttackButtonPressed();
        boolean activate = false;
        if (mouseHover && mousePressed) {
            activate = true;
        } else if (!mouseHover && attackPressed) {
            activate = true;
        }
        if (activate) {
            int idx = selectedMenuIndex;
            if (!hasSaveFile && idx > 0) idx++;
            switch (idx) {
                case 0: // Continue
                    if (hasSaveFile) {
                        showMainMenu = false;
                    }
                    break;
                case 1: // Start
                    if (hasSaveFile) {
                        showOverwritePrompt = true;
                        // TODO: Draw overwrite prompt and handle confirmation
                    } else {
                        showMainMenu = false;
                        // TODO: Start new game logic
                    }
                    break;
                case 2: // Music
                    musicOn = !musicOn;
                    if (musicOn) song.play(); else song.pause();
                    break;
                case 3: // Volume
                    // Volume adjustment handled by left/right keys or mouse drag
                    break;
            }
        }
        // Volume bar adjustment
        mouseOverVolume = volumeBarRect != null && volumeBarRect.contains(mouse);
        // Start dragging if mouse pressed on bar
        if (mouseOverVolume && mousePressed) {
            draggingVolumeBar = true;
        }
        // Stop dragging if mouse released
        if (!mouseHeld) {
            draggingVolumeBar = false;
        }
        // While dragging, update volume based on mouse X (regardless of Y)
        if (draggingVolumeBar && mouseHeld) {
            float rel = (mouse.x - volumeBarRect.x) / volumeBarRect.width;
            masterVolume = Math.max(0, Math.min(1, rel));
            song.setVolume(masterVolume);
        } else if (selectedMenuIndex == 3 && horiz != 0) {
            masterVolume = Math.max(0, Math.min(1, masterVolume + horiz * 0.01f));
            song.setVolume(masterVolume);
        }
        mousePressedLastFrame = mouseHeld;
    }

    @Override
    public void onEnter() {
        RockType.validateOres();
        Application.player.hp = Application.player.getMaxHp();
    }

    @Override
    public boolean[][] isCollidableGrid() {
        return collidableGrid;
    }

    @Override
    void drawAssets() {
        int offset = 15*16;
        Application.batch.draw(jarBugFix, offset+6*16, offset+6*16);
        super.drawAssets();
        drawHighLayer();
    }

    @Override
    public void drawOther() {
        drawHighLayer();
    }

    private void drawHighLayer() {
        int offset = 15*16;
        Application.batch.draw(blacksmithroof, offset+6*16, offset+8*16);
        Application.batch.draw(tree, offset+5*16, offset+14*16);
        Application.batch.draw(tree, offset+-1*16,offset+ 11*16);
        Application.batch.draw(tree, offset+1*16, offset+3*16);
        Application.batch.draw(tree, offset+0*16, offset+7*16);
        Application.batch.draw(tree, offset+23*16,offset+ 7*16);
        Application.batch.draw(tree, offset+21*16,offset+ 16*16);
        Application.batch.draw(roof, offset+12*16,offset+ 9*16);
        Application.batch.draw(roof, offset+16*16,offset+ 9*16);
    }

    public boolean[][] getHolePositions() {
        return holePositions;
    }

    @Override
    public Vector2 getInitialPlayerPosition() {
        return new Vector2(320,290);
    }

    @Override
    public int getMapWidth() {
        return 60;
    }

    @Override
    public int getMapHeight() {
        return 50;
    }
}
