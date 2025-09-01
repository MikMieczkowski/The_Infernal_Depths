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
import com.badlogic.gdx.Input;
import com.mikm.input.GameInput;
import com.badlogic.gdx.math.Rectangle;
import com.mikm.debug.DebugRenderer;
import com.mikm.input.InputRaw;

public class TownScreen extends GameScreen {

    private static TextureRegion stallroof = Assets.getInstance().getTextureRegion("stallroof", 80, 48);
    private static TextureRegion blacksmithroof = Assets.getInstance().getTextureRegion("blacksmithRoof", 80, 96);
    private static TextureRegion jarBugFix = Assets.getInstance().getTextureRegion("weirdJarBug", 80, 96);
    private static TextureRegion tree = Assets.getInstance().getTextureRegion("treetop", 48, 48);
    private static TextureRegion roof = Assets.getInstance().getTextureRegion("houseRoof", 64,96);
    private boolean[][] collidableGrid;
    private boolean[][] holePositions;
    private boolean hasSaveFile = false;
    private boolean showOverwritePrompt = false;
    private int overwritePromptSelection = 0; // 0 = Yes, 1 = No
    private float masterVolume = 1.0f;
    private boolean musicOn = true;
    private Vector2 lastMousePos = new Vector2();
    private boolean keyboardControllerActive = false;
    private TextureRegion menuBg = Assets.getInstance().getTextureRegion("UI", 97, 77);
    private TextureRegion selector = Assets.getInstance().getTextureRegion("UISelector", 29, 29);
    private TextureRegion soundIcon = Assets.getInstance().getTextureRegion("UI", 32, 32); // Placeholder, adjust as needed
    private TextureRegion titleImage = Assets.getInstance().getTextureRegion("UI", 97, 77); // Placeholder, adjust as needed
    private int selectedMenuIndex = 1;
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
        Application.getInstance().paused = true;
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
        if (Application.getInstance().paused) {
            ScreenUtils.clear(Color.BLACK);
            // Do NOT call camera.update() here
            Application.batch.begin();
            Application.batch.setProjectionMatrix(Camera.orthographicCamera.combined);
            tiledMapRenderer.setView(Camera.orthographicCamera);
            drawAssets();
            DebugRenderer.getInstance().update();
            Camera.renderLighting(Application.batch);
            Camera.updateOrthographicCamera();
            //renderUI();
            Application.batch.end();
            renderMainMenu();
            System.out.print("\033[2A"); // Move cursor up 2 lines
            System.out.print("\rMouse: " + GameInput.getMousePos().x + " " + GameInput.getMousePos().y + "    ");
            System.out.println();
            System.out.print("\rRect: " + menuOptionRects[0].x + " " + menuOptionRects[0].y + " " + menuOptionRects[0].width + " " + menuOptionRects[0].height + "    ");
            System.out.println();
        } else {
            super.render(delta);
        }
    }

    private void renderMainMenu() {
        Camera.orthographicCamera.update();
        Application.batch.begin();
        Application.batch.setProjectionMatrix(Camera.orthographicCamera.combined);
        
        // Menu setup
        int yStart = 60;
        int yStep = 40;
        int optionCount = hasSaveFile ? 4 : 3;
        float baseX = Camera.orthographicCamera.position.x - 100;
        float baseY = Camera.orthographicCamera.position.y + yStart;
        
        // Setup menu option rectangles (keeping same coordinates)
        for (int i = 0; i < optionCount; i++) {
            float y = baseY - yStep * i;
            menuOptionRects[i] = new Rectangle(baseX - 10, y - 20, 180, 32);
        }
        
        // Draw menu options
        int drawIndex = 0;
        if (hasSaveFile) {
            drawMenuOption("Continue", drawIndex, yStart, yStep, baseX);
            drawIndex++;
        }
        drawMenuOption("New Game", drawIndex, yStart, yStep, baseX);
        drawIndex++;
        drawMenuOption("Music: " + (musicOn ? "On" : "Off"), drawIndex, yStart, yStep, baseX);
        drawIndex++;
        drawMenuOption("Volume", drawIndex, yStart, yStep, baseX);
        
        // Draw volume bar
        int volumeBarY = yStart - yStep * 3;
        drawVolumeBar(volumeBarY, baseX);
        float barX = baseX + 40;
        float barY = Camera.orthographicCamera.position.y + volumeBarY-10;
        volumeBarRect = new Rectangle(140, 30, 100, 13);
        
        // Draw selector
        drawSelector(yStart, yStep, selectedMenuIndex, baseX);
        
        // Render overwrite prompt if active
        if (showOverwritePrompt) {
            renderOverwritePrompt();
        }
        
        Application.batch.end();
        
        // Handle input
        if (showOverwritePrompt) {
            handleOverwritePromptInput();
        } else {
            handleMenuInput(optionCount);
        }
    }
    
    private void renderOverwritePrompt() {
        // Draw semi-transparent overlay
        Application.batch.setColor(0, 0, 0, 0.7f);
        Application.batch.draw(menuBg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Application.batch.setColor(Color.WHITE);
        
        // Draw prompt box
        float centerX = Camera.orthographicCamera.position.x;
        float centerY = Camera.orthographicCamera.position.y;
        float boxWidth = 300;
        float boxHeight = 150;
        float boxX = centerX - boxWidth / 2;
        float boxY = centerY - boxHeight / 2;
        
        // Draw background box
        Application.batch.setColor(Color.DARK_GRAY);
        Application.batch.draw(menuBg, boxX, boxY, boxWidth, boxHeight);
        Application.batch.setColor(Color.WHITE);
        
        // Draw text
        String promptText = "Overwrite existing save?";
        String yesText = "Yes";
        String noText = "No";
        
        float textX = centerX - Assets.font.draw(Application.batch, promptText, 0, 0).width / 2;
        float textY = centerY + 30;
        Assets.font.draw(Application.batch, promptText, textX, textY);
        
        // Draw options
        float optionY = centerY - 20;
        float yesX = centerX - 80;
        float noX = centerX + 20;
        
        // Draw selector
        float selectorX = (overwritePromptSelection == 0) ? yesX - 25 : noX - 25;
        Application.batch.draw(selector, selectorX, optionY - 10);
        
        Assets.font.draw(Application.batch, yesText, yesX, optionY);
        Assets.font.draw(Application.batch, noText, noX, optionY);
    }
    
    private void handleOverwritePromptInput() {
        Vector2 mouse = GameInput.getMousePos();
        
        // Handle mouse X coordinate selection
        if (mouse.x > 160) {
            overwritePromptSelection = 1; // No
        } else {
            overwritePromptSelection = 0; // Yes
        }
        
        // Handle navigation
        if (GameInput.isDpadLeftJustPressed() || GameInput.isDpadRightJustPressed()) {
            overwritePromptSelection = (overwritePromptSelection + 1) % 2;
        }
        
        // Handle selection
        boolean enterPressed = Gdx.input.isKeyJustPressed(Input.Keys.ENTER);
        boolean mousePressed = GameInput.isAttackButtonJustPressed();
        boolean dashPressed = GameInput.isDiveButtonJustPressed();
        
        if (enterPressed || mousePressed || dashPressed) {
            if (overwritePromptSelection == 0) {
                // Yes - start new game
                showOverwritePrompt = false;
                Application.getInstance().paused = false;
                // TODO: Clear save data and start new game
            } else {
                // No - go back to menu
                showOverwritePrompt = false;
            }
        }
        
        // Handle escape to cancel
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            showOverwritePrompt = false;
        }
    }

    private void drawMenuOption(String text, int index, int yStart, int yStep, float baseX) {
        Assets.font.draw(Application.batch, text, baseX, Camera.orthographicCamera.position.y + yStart - yStep * index);
    }

    private void drawSelector(int yStart, int yStep, int selectorIndex, float baseX) {
        Application.batch.draw(selector, baseX - 5, Camera.orthographicCamera.position.y + yStart - yStep * selectorIndex - 12, 64, 16);
    }

    private void drawVolumeBar(int y, float baseX) {
        float barX = baseX + 60;
        float barY = Camera.orthographicCamera.position.y + y-10;
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

    private void handleMenuInput(int optionCount) {
        Vector2 mouse = GameInput.getMousePos();
        
        // Check if mouse has moved
        if (!lastMousePos.equals(mouse)) {
            keyboardControllerActive = false;
            lastMousePos.set(mouse);
        }
        
        // Handle mouse hover selection based on Y coordinates
        boolean mouseHover = false;
        int hoveredIndex = -1;
        
        
        // Y coordinate based selection
        if (mouse.y > 132) {
            hoveredIndex = 0; // Continue
            mouseHover = true;
        } else if (mouse.y > 95) {
            hoveredIndex = 1; // New Game
            mouseHover = true;
        } else if (mouse.y > 53) {
            hoveredIndex = 2; // Music
            mouseHover = true;
        } else {
            hoveredIndex = 3; // Volume
            mouseHover = true;
        }
        
        // Update selection based on input
        if (GameInput.isDpadUpJustPressed() || GameInput.isDpadDownJustPressed()) {
            keyboardControllerActive = true;
            if (GameInput.isDpadUpJustPressed()) {
                selectedMenuIndex = (selectedMenuIndex - 1 + optionCount) % optionCount;
            } else {
                selectedMenuIndex = (selectedMenuIndex + 1) % optionCount;
            }
        } else if (mouseHover && !keyboardControllerActive) {
            selectedMenuIndex = hoveredIndex;
        }
        
        // Handle activation
        boolean mousePressed = GameInput.isAttackButtonJustPressed();
        boolean dashPressed = GameInput.isDiveButtonJustPressed();
        boolean enterPressed = Gdx.input.isKeyJustPressed(Input.Keys.ENTER);
        boolean activate = (mouseHover && mousePressed) || dashPressed || enterPressed;
        
        if (activate) {
            handleMenuSelection();
        }
        
        // Handle volume bar
        handleVolumeBar(mouse, mousePressed);
    }
    
    private void handleMenuSelection() {
        int actualIndex = selectedMenuIndex;
        if (!hasSaveFile && actualIndex > 0) {
            actualIndex++; // Adjust for missing Continue option
        }
        
        switch (actualIndex) {
            case 0: // Continue
                if (hasSaveFile) {
                    Application.getInstance().paused = false;
                }
                break;
            case 1: // New Game
                if (hasSaveFile) {
                    showOverwritePrompt = true;
                } else {
                    Application.getInstance().paused = false;
                }
                break;
            case 2: // Music
                musicOn = !musicOn;
                if (musicOn) song.play(); else song.stop();
                break;
            case 3: // Volume
                // Volume adjustment handled by left/right keys or mouse drag
                break;
        }
    }
    
    private void handleVolumeBar(Vector2 mouse, boolean mousePressed) {
        boolean mouseHeld = GameInput.isAttackButtonPressed();
        
        // Check if mouse is over volume bar
        mouseOverVolume = volumeBarRect != null && volumeBarRect.contains(mouse);
        
        // Handle dragging
        if (mouseOverVolume && mousePressed) {
            draggingVolumeBar = true;
        }
        if (!mouseHeld) {
            draggingVolumeBar = false;
        }
        
        // Update volume
        if (draggingVolumeBar && mouseHeld) {
            float rel = (mouse.x - volumeBarRect.x) / volumeBarRect.width;
            masterVolume = Math.max(0, Math.min(1, rel));
            song.setVolume(masterVolume);
        } else if (selectedMenuIndex == 3) {
            int horiz = GameInput.getHorizontalAxisInt();
            if (horiz != 0) {
                masterVolume = Math.max(0, Math.min(1, masterVolume + horiz * 0.01f));
                song.setVolume(masterVolume);
            }
        }
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
