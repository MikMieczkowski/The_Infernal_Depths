package com.mikm.rendering.screens;

import com.badlogic.gdx.audio.Music;
// removed unused import
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
// removed unused import
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.mikm.entities.prefabLoader.PrefabInstantiator;
import com.mikm.utils.Assets;
// removed unused imports
import com.mikm.utils.ExtraMathUtils;
import com.mikm.Vector2Int;
import com.mikm._components.Transform;
// removed unused imports
import com.mikm.entities.inanimateEntities.particles.ParticleTypes;
import com.mikm.rendering.Camera;
import com.mikm.rendering.sound.SoundEffects;
import com.mikm.rendering.cave.RockType;
import com.mikm.serialization.Serializer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.mikm.input.GameInput;
import com.mikm.input.InputRaw;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
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
    public boolean showMainMenu = true;
    private boolean showOverwritePrompt = false;
    private int overwritePromptSelection = 0; // 0 = Yes, 1 = No
    private float masterVolume = 1.0f;
    private Vector2 lastMousePos = new Vector2();
    private boolean keyboardControllerActive = false;
    private int lastControllerY = 0; // for left stick vertical menu navigation
    private int lastControllerX = 0; // for left stick horizontal prompt toggling
    private TextureRegion menuBg = Assets.getInstance().getTextureRegion("UIbg", 97, 77);
    private TextureRegion volumeBg = Assets.getInstance().getTextureRegion("UIrect", 97, 77);
    private TextureRegion selector = Assets.getInstance().getTextureRegion("UISelector", 29, 29);
    private TextureRegion titleImage = Assets.getInstance().getTextureRegion("UIrect", 97, 77); // Placeholder, adjust as needed
    private int selectedMenuIndex = 1;
    private Rectangle[] menuOptionRects = new Rectangle[4];
    private Rectangle volumeBarRect;
    private boolean mouseOverVolume = false;
    private boolean draggingVolumeBar = false;
    private int controllerNavCooldownFrames = 0; // prevent dpad+stick double-move
    private int controllerPromptCooldownFrames = 0; // prevent double toggle in prompt
    private Vector2 worldMouse = new Vector2();

    private final String AMBIENCE_SOUND_EFFECT = "townAmbience.ogg";
    private final Vector2Int blacksmithDoorCoords = new Vector2Int(24*16, 23*16);

    TownScreen() {
        super();
        tiledMap = new TmxMapLoader().load("tiled/Overworld.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1);
        tiledMapRenderer.setView(Camera.orthographicCamera);
        createMusic(Assets.getInstance().getAsset("sound/townTheme.mp3", Music.class));
        collidableGrid = readCollisionTiledmapLayer(2, getMapWidth(), getMapHeight());
        holePositions = readCollisionTiledmapLayer(3, getMapWidth(), getMapHeight());
        readAndCreateDestructiblesTiledmapLayer(1, Assets.getInstance().getTextureRegion("grass"), true);

        Vector2Int wizardDoorCoords = new Vector2Int(18*16, 28*16);

        PrefabInstantiator.addDoor(this, wizardDoorCoords.x, wizardDoorCoords.y, 4);
        PrefabInstantiator.addDoor(this, blacksmithDoorCoords.x, blacksmithDoorCoords.y, 3);

        // Check for save files
        hasSaveFile = Serializer.getInstance().saveFilesExist();
        showMainMenu = true;
        Application.getInstance().paused = true;

        //PrefabInstantiator.addEntity("slime",this, 200, 200);

        PrefabInstantiator.addParticles(this, 200, 150, 0, ParticleTypes.getRockParameters(RockType.NORMAL));

        //PrefabInstantiator.addProjectile(this, 200, 250);
        //PrefabInstantiator.addProjectile(this, 200, 200);
        //PrefabInstantiator.addProjectile(this, 200, 250);

    }

    @Override
    public void render(float delta) {
        super.setRenderCamera(!showMainMenu);
        super.setRenderUI(!showMainMenu);
        super.render(delta);
        if (!showMainMenu) {
            handleFireCrackling();
        }
    }

    /*
    System.out.print("\033[2A"); // Move cursor up 2 lines
    System.out.print("\rMouse: " + worldMouse.x + " " + worldMouse.y + "    ");
    System.out.println();
    System.out.print("\rRect: " + menuOptionRects[0].x + " " + menuOptionRects[0].y + " " + menuOptionRects[0].width + " " + menuOptionRects[0].height + "    ");
    System.out.println();
     */

    @Override
    protected void drawAssetsPreEntities() {
        int offset = 15*16;
        Application.batch.draw(jarBugFix, offset+6*16, offset+6*16);
    }

    @Override
    protected void drawAssetsPostEntities() {
        drawHighLayer();
        //TODO ensure smoke particles are rendering properly
        if (showMainMenu) {
            renderMainMenu();
        }
    }


    private void handleFireCrackling() {
        Transform transform = Application.getInstance().getPlayerTransform();
        float distToDoor = ExtraMathUtils.distance(transform.getCenteredX(), transform.getCenteredY(), blacksmithDoorCoords.x, blacksmithDoorCoords.y);
        float soundRadius = 64;
        float inverseClampedNumberOfSoundRadiusAwayFromDoor = 1 - MathUtils.clamp(distToDoor/soundRadius, 0, 1);
        //square law of sound
        float vol = inverseClampedNumberOfSoundRadiusAwayFromDoor * inverseClampedNumberOfSoundRadiusAwayFromDoor;
        //Stupid bug requires this -- Apparently townScreen update is run after caveScreen enter
        if (Application.getInstance().currentScreen == Application.getInstance().townScreen) {
            SoundEffects.setLoopVolume(BlacksmithScreen.FIRE_AMBIENCE, vol);
        }
    }
    
    private void renderMainMenu() {
        Camera.orthographicCamera.update();
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
        // Cooldown tick
        if (controllerPromptCooldownFrames > 0) controllerPromptCooldownFrames--;
        
        // Handle navigation
        if (GameInput.isDpadLeftJustPressed() || GameInput.isDpadRightJustPressed()) {
            overwritePromptSelection = (overwritePromptSelection + 1) % 2;
            controllerPromptCooldownFrames = 8;
        }
        // Also allow controller left stick horizontal to toggle (on release to neutral)
        if (InputRaw.usingController) {
            int horiz = InputRaw.controllerXAxisIntNoDpad();
            if (controllerPromptCooldownFrames == 0 && lastControllerX != 0 && horiz == 0) {
                overwritePromptSelection = (overwritePromptSelection + 1) % 2;
                controllerPromptCooldownFrames = 8;
            }
            lastControllerX = horiz;
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
        // Cooldown tick
        if (controllerNavCooldownFrames > 0) controllerNavCooldownFrames--;
        
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
            controllerNavCooldownFrames = 8;
            // Skip grayed-out Continue option when navigating with keyboard
            if (!hasSaveFile && selectedMenuIndex == 0) {
                if (GameInput.isDpadUpJustPressed()) {
                    selectedMenuIndex = optionCount - 1; // Go to last option
                } else {
                    selectedMenuIndex = 1; // Go to next option
                }
            }
        } else if (InputRaw.usingController) {
            // Allow controller left stick vertical to navigate (trigger on release to neutral)
            int vert = InputRaw.controllerYAxisIntNoDpad();
            if (controllerNavCooldownFrames == 0 && lastControllerY != 0 && vert == 0) {
                keyboardControllerActive = true;
                if (lastControllerY == 1) {
                    selectedMenuIndex = (selectedMenuIndex - 1 + optionCount) % optionCount; // up
                } else if (lastControllerY == -1) {
                    selectedMenuIndex = (selectedMenuIndex + 1) % optionCount; // down
                }
                controllerNavCooldownFrames = 8;
                // Skip grayed-out Continue option when navigating with controller
                if (!hasSaveFile && selectedMenuIndex == 0) {
                    if (lastControllerY == 1) {
                        selectedMenuIndex = optionCount - 1; // wrap to last option
                    } else {
                        selectedMenuIndex = 1; // go to next option
                    }
                }
            }
            lastControllerY = vert;
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
        SoundEffects.SFX_VOLUME = masterVolume;
        SoundEffects.updateLoopVolumes();
    }

    @Override
    public void onEnter() {
        SoundEffects.playLoop(AMBIENCE_SOUND_EFFECT);
        if (!SoundEffects.loopIsPlaying(BlacksmithScreen.FIRE_AMBIENCE)) {
            SoundEffects.playLoop(BlacksmithScreen.FIRE_AMBIENCE);
            SoundEffects.setLoopVolume(BlacksmithScreen.FIRE_AMBIENCE, 0);
        }
        RockType.validateOres();
        Application.getInstance().getPlayerCombatComponent().hp = Application.getInstance().getPlayerCombatComponent().MAX_HP;
    }

    @Override
    public void onExit() {
        SoundEffects.stopLoop(AMBIENCE_SOUND_EFFECT);
    }

    @Override
    public boolean[][] isCollidableGrid() {
        return collidableGrid;
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
            //TODO particles
//            new ParticleEffect(ParticleTypes.getSmokeParameters(), MathUtils.HALF_PI, offset+12*16+3,offset+ 13*16-6);
//            new ParticleEffect(ParticleTypes.getSmokeParameters(), MathUtils.HALF_PI, offset+16*16+3,offset+ 13*16-6);
//            new ParticleEffect(ParticleTypes.getSmokeParameters(), MathUtils.HALF_PI, offset+8*16+6,offset+ 13*16-6);
            PrefabInstantiator.addParticles(this, offset+12*16+3,offset+ 13*16-6, MathUtils.HALF_PI, ParticleTypes.getSmokeParameters());
            PrefabInstantiator.addParticles(this, offset+16*16+3,offset+ 13*16-6, MathUtils.HALF_PI, ParticleTypes.getSmokeParameters());
            PrefabInstantiator.addParticles(this, offset+8*16+6, offset+ 13*16-6, MathUtils.HALF_PI, ParticleTypes.getSmokeParameters());
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
}
