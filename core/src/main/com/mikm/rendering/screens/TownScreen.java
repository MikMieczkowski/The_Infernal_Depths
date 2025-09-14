package com.mikm.rendering.screens;

import com.badlogic.gdx.audio.Music;
// removed unused import
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
// removed unused import
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mikm.Assets;
// removed unused imports
import com.mikm.entities.Door;
import com.mikm.entities.InanimateEntity;
import com.mikm.entities.RemovableArray;
import com.mikm.entities.Shadow;
// removed unused imports
import com.mikm.entities.particles.ParticleEffect;
import com.mikm.entities.particles.ParticleTypes;
import com.mikm.rendering.Camera;
import com.mikm.rendering.SoundEffects;
import com.mikm.rendering.cave.RockType;
import com.mikm.serialization.Serializer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.mikm.input.GameInput;
import com.mikm.input.InputRaw;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.mikm.debug.DebugRenderer;
// removed unused import

public class TownScreen extends GameScreen {

    private static TextureRegion stallroof = Assets.getInstance().getTextureRegion("stallroof", 80, 48);
    private static TextureRegion blacksmithroof = Assets.getInstance().getTextureRegion("blacksmithRoof", 80, 96);
    private static TextureRegion jarBugFix = Assets.getInstance().getTextureRegion("weirdJarBug", 80, 96);
    private static TextureRegion tree = Assets.getInstance().getTextureRegion("treetop", 48, 48);
    private static TextureRegion roof = Assets.getInstance().getTextureRegion("houseRoof", 64,96);
    private static TextureRegion townShadows = Assets.getInstance().getTextureRegion("townShadows", 1300,769);
    private boolean[][] collidableGrid;
    private boolean[][] holePositions;
    private boolean hasSaveFile = false;
    private boolean showMainMenu = true;
    private boolean showOverwritePrompt = false;
    private int overwritePromptSelection = 0; // 0 = Yes, 1 = No
    private float masterVolume = 1.0f;
    private Vector2 lastMousePos = new Vector2();
    private boolean keyboardControllerActive = false;
    private TextureRegion menuBg = Assets.getInstance().getTextureRegion("UIbg", 97, 77);
    private TextureRegion volumeBg = Assets.getInstance().getTextureRegion("UIrect", 97, 77);
    private TextureRegion selector = Assets.getInstance().getTextureRegion("UISelector", 29, 29);
    private TextureRegion titleImage = Assets.getInstance().getTextureRegion("UIrect", 97, 77); // Placeholder, adjust as needed
    private int selectedMenuIndex = 1;
    private Rectangle[] menuOptionRects = new Rectangle[4];
    private Rectangle volumeBarRect;
    private boolean mouseOverVolume = false;
    private boolean draggingVolumeBar = false;
    private Vector2 worldMouse = new Vector2();

    
    public final RemovableArray<InanimateEntity> smokeParticles = new RemovableArray<>();

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
        hasSaveFile = Serializer.getInstance().saveFilesExist();
        showMainMenu = true;
        Application.getInstance().paused = true;


    }

    @Override
    public void render(float delta) {
        if (showMainMenu) {
            // Main menu is shown - pause is on but don't show pause screen
            ScreenUtils.clear(Color.BLACK);
            // Do NOT call camera.update() here
            Application.batch.begin();
            Application.batch.setProjectionMatrix(Camera.orthographicCamera.combined);
            tiledMapRenderer.setView(Camera.orthographicCamera);
            drawAssets();
            smokeParticles.render(Application.batch);
            DebugRenderer.getInstance().update();
            Camera.renderLighting(Application.batch);
            Camera.updateOrthographicCamera();
            Application.batch.end();
            renderMainMenu();
            System.out.print("\033[2A"); // Move cursor up 2 lines
            System.out.print("\rMouse: " + worldMouse.x + " " + worldMouse.y + "    ");
            System.out.println();
            System.out.print("\rRect: " + menuOptionRects[0].x + " " + menuOptionRects[0].y + " " + menuOptionRects[0].width + " " + menuOptionRects[0].height + "    ");
            System.out.println();
        } else if (Application.getInstance().paused) {
            // Main menu is hidden but pause is on - show regular pause screen
            super.render(delta);
        } else {
            // Normal game rendering
            if (!Application.getInstance().timestop && !Application.getInstance().paused) {
                camera.update();
                Application.batch.begin();
                Application.batch.setProjectionMatrix(Camera.orthographicCamera.combined);
                tiledMapRenderer.setView(Camera.orthographicCamera);
                drawAssets();
                //This is the one change from super.render(delta)
                smokeParticles.render(Application.batch);
                DebugRenderer.getInstance().update();
                Camera.renderLighting(Application.batch);
                Camera.updateOrthographicCamera();
                renderUI();
                Application.batch.end();
            } else {
                drawNoUpdate();
            }
        }
    }

    private void renderMainMenu() {
        Camera.orthographicCamera.update();
        Application.batch.begin();
        Application.batch.setProjectionMatrix(Camera.orthographicCamera.combined);
        
        // Menu setup
        int yStart = 60;
        int yStep = 40;
        int optionCount = 4;
        float baseX = Camera.orthographicCamera.position.x - 100;
        float baseY = Camera.orthographicCamera.position.y + yStart;
        
        // Setup menu option rectangles (keeping same coordinates)
        for (int i = 0; i < optionCount; i++) {
            float y = baseY - yStep * i;
            menuOptionRects[i] = new Rectangle(baseX - 10, y - 20, 180, 32);
        }
        
        // Draw menu options
        int drawIndex = 0;
        // Always draw Continue, but gray it out if no save file
        if (!hasSaveFile) {
            Assets.font.setColor(Color.GRAY);
        }
        drawMenuOption("Continue", drawIndex, yStart, yStep, baseX);
        Assets.font.setColor(Color.WHITE);
        drawIndex++;
        drawMenuOption("New Game", drawIndex, yStart, yStep, baseX);
        drawIndex++;
        drawMenuOption("Music: " + (Application.musicOn ? "On" : "Off"), drawIndex, yStart, yStep, baseX);
        drawIndex++;
        drawMenuOption("Volume", drawIndex, yStart, yStep, baseX);
        
        // Draw volume bar
        int volumeBarY = yStart - yStep * 3;
        drawVolumeBar(volumeBarY, baseX);
        float barX = baseX + 40;
        float barY = Camera.orthographicCamera.position.y + volumeBarY-10;
        volumeBarRect = new Rectangle(424, 394, 100, 20);
        
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
        Application.batch.draw(volumeBg, boxX, boxY, boxWidth, boxHeight);
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
        Application.batch.draw(selector, selectorX+20, optionY - 14, 30, 16);
        
        Assets.font.draw(Application.batch, yesText, yesX, optionY);
        Assets.font.draw(Application.batch, noText, noX, optionY);
    }
    
    private void handleOverwritePromptInput() {
        // Handle mouse input using proper coordinate conversion
        Vector2 mouse = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        // Convert to world coordinates
        com.badlogic.gdx.math.Vector3 world3 = Camera.orthographicCamera.unproject(new com.badlogic.gdx.math.Vector3(mouse.x, mouse.y, 0));
        worldMouse.set(world3.x, world3.y);
        
        // Handle navigation
        if (GameInput.isDpadLeftJustPressed() || GameInput.isDpadRightJustPressed()) {
            overwritePromptSelection = (overwritePromptSelection + 1) % 2;
        }
        
        // Handle mouse X coordinate selection (only when not using controller)
        if (InputRaw.usingController) {
            // Don't override controller selection with mouse position
        } else {
            if (worldMouse.x > 440) {
                overwritePromptSelection = 1; // No
            } else {
                overwritePromptSelection = 0; // Yes
            }
        }
        
        // Handle selection
        boolean enterPressed = Gdx.input.isKeyJustPressed(Input.Keys.ENTER);
        boolean mousePressed = GameInput.isAttackButtonJustPressed();
        boolean dashPressed = GameInput.isDiveButtonJustPressed();
        
        if (enterPressed || mousePressed || dashPressed) {
            if (overwritePromptSelection == 0) {
                // Yes - start new game
                showOverwritePrompt = false;
                showMainMenu = false;
                Application.getInstance().paused = false;
                // Clear save data and start new game
                //Application.getInstance().loadScreens();
                Serializer.getInstance().resetSaveFiles();
                Application.getInstance().caveScreen.init();
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
        Application.batch.draw(volumeBg, barX, barY, barWidth, barHeight);
        
        // Draw filled part
        Application.batch.setColor(Color.LIME);
        Application.batch.draw(volumeBg, barX, barY, barWidth * masterVolume, barHeight);
        Application.batch.setColor(Color.WHITE);
    }

    private void handleMenuInput(int optionCount) {
        // Handle mouse input using proper coordinate conversion
        Vector2 mouse = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        // Convert to world coordinates
        com.badlogic.gdx.math.Vector3 world3 = Camera.orthographicCamera.unproject(new com.badlogic.gdx.math.Vector3(mouse.x, mouse.y, 0));
        worldMouse.set(world3.x, world3.y);
        
        // Check if mouse has moved
        if (!lastMousePos.equals(worldMouse)) {
            keyboardControllerActive = false;
            lastMousePos.set(worldMouse);
        }
        
        // Handle mouse hover selection based on Y coordinates
        boolean mouseHover = false;
        int hoveredIndex = -1;
        
        
        // Y coordinate based selection using world coordinates
        if (worldMouse.y > 497) {
            hoveredIndex = 0; // Continue
            mouseHover = true;
        } else if (worldMouse.y > 458) {
            hoveredIndex = 1; // New Game
            mouseHover = true;
        } else if (worldMouse.y > 415) {
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
            // Skip grayed-out Continue option when navigating with keyboard
            if (!hasSaveFile && selectedMenuIndex == 0) {
                if (GameInput.isDpadUpJustPressed()) {
                    selectedMenuIndex = optionCount - 1; // Go to last option
                } else {
                    selectedMenuIndex = 1; // Go to next option
                }
            }
        } else if (mouseHover && !keyboardControllerActive) {
            // Don't allow mouse hover selection of grayed-out Continue
            if (hoveredIndex == 0 && !hasSaveFile) {
                // Do nothing - don't change selection
            } else {
                selectedMenuIndex = hoveredIndex;
            }
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
        handleVolumeBar(worldMouse, mousePressed);
    }
    
    private void handleMenuSelection() {
        switch (selectedMenuIndex) {
            case 0: // Continue
                if (hasSaveFile) {
                    showMainMenu = false;
                    Application.getInstance().paused = false;
                    //Application.getInstance().loadScreens();
                    Application.getInstance().loadAllSaveData(); 
                    Application.getInstance().caveScreen.init();
                }
                break;
            case 1: // New Game
                if (hasSaveFile) {
                    showOverwritePrompt = true;
                } else {
                    showMainMenu = false;
                    Application.getInstance().paused = false;
                    //Serializer.getInstance().createSaveFilesIfNotExists();
                    //Application.getInstance().loadScreens();
                    Application.getInstance().caveScreen.init();
                }
                break;
            case 2: // Music
                Application.musicOn = !Application.musicOn;
                if (Application.musicOn) song.play(); else song.stop();
                break;
            case 3: // Volume
                // Volume adjustment handled by left/right keys or mouse drag
                break;
        }
    }
    
    private void handleVolumeBar(Vector2 worldMouse, boolean mousePressed) {
        boolean mouseHeld = GameInput.isAttackButtonPressed();
        
        // Check if mouse is over volume bar using world coordinates
        mouseOverVolume = volumeBarRect != null && volumeBarRect.contains(worldMouse);
        
        // Handle dragging
        if (mouseOverVolume && mousePressed) {
            draggingVolumeBar = true;
        }
        if (!mouseHeld) {
            draggingVolumeBar = false;
        }
        
        // Update volume using world coordinates
        if (draggingVolumeBar && mouseHeld) {
            float rel = (worldMouse.x - volumeBarRect.x) / volumeBarRect.width;
            masterVolume = Math.max(0, Math.min(1, rel));
            changeVolume(masterVolume);
        } else if (selectedMenuIndex == 3) {
            int horiz = GameInput.getHorizontalAxisInt();
            if (horiz != 0) {
                masterVolume = Math.max(0, Math.min(1, masterVolume + horiz * 0.01f));
                changeVolume(masterVolume);
            }
        }
    }

    private void changeVolume(float masterVolume) {
        song.setVolume(masterVolume);
        Application.getInstance().caveScreen.song.setVolume(masterVolume);
        Application.getInstance().slimeBossRoomScreen.song.setVolume(masterVolume);
        Application.getInstance().motiScreen.song.setVolume(masterVolume);
        SoundEffects.SFX_VOLUME = masterVolume;
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




    private float smokeTimer;
    private final float SMOKE_TIMER_MAX = 0.11f;

    private void drawHighLayer() {
        int offset = 15*16;
        //TODO make these entities
        
        Application.batch.draw(blacksmithroof, offset+6*16, offset+8*16);
        Application.batch.draw(tree, offset+5*16, offset+14*16);
        Application.batch.draw(tree, offset+-1*16,offset+ 11*16);
        Application.batch.draw(tree, offset+1*16, offset+3*16);
        Application.batch.draw(tree, offset+0*16, offset+7*16);
        Application.batch.draw(tree, offset+23*16,offset+ 7*16);
        Application.batch.draw(tree, offset+21*16,offset+ 16*16);
        Application.batch.draw(roof, offset+12*16,offset+ 9*16);
        Application.batch.draw(townShadows, -8,224, 648, 24*16);
        Application.batch.draw(roof, offset+16*16,offset+ 9*16);
        smokeTimer += Gdx.graphics.getDeltaTime();
        if (smokeTimer > SMOKE_TIMER_MAX) {
            smokeTimer -= SMOKE_TIMER_MAX;
            new ParticleEffect(ParticleTypes.getSmokeParameters(), MathUtils.HALF_PI, offset+12*16+3,offset+ 13*16-6);
            new ParticleEffect(ParticleTypes.getSmokeParameters(), MathUtils.HALF_PI, offset+16*16+3,offset+ 13*16-6);
            new ParticleEffect(ParticleTypes.getSmokeParameters(), MathUtils.HALF_PI, offset+8*16+6,offset+ 13*16-6);
        }
    
    }

    public boolean isMainMenuActive() {
        return showMainMenu;
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

    public void addSmokeParticle(InanimateEntity entity) {
        smokeParticles.add(entity);
        if (entity.hasShadow()) {
            Shadow shadow = new Shadow(entity);
            entity.shadow = shadow;
            smokeParticles.add(shadow);
        }
        System.out.println("done");
    }

    public void removeSmokeParticle(InanimateEntity entity) {
        smokeParticles.remove(entity);
        smokeParticles.remove(entity.shadow);
    }
}
