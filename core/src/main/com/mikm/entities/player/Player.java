package com.mikm.entities.player;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mikm.DeltaTime;
import com.mikm.Vector2Int;
import com.mikm.debug.DebugRenderer;
import com.mikm.entities.Entity;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.animation.DirectionalAnimation;
import com.mikm.entities.player.states.*;
import com.mikm.entities.player.weapons.Weapon;
import com.mikm.entities.player.weapons.WeaponInstances;
import com.mikm.input.GameInput;
import com.mikm.rendering.cave.CaveTilemapCreator;
import com.mikm.rendering.cave.RockType;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.CaveScreen;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Player extends Entity {
    public static final int PLAYER_WIDTH_PIXELS = 32, PLAYER_HEIGHT_PIXELS = 32;
    private final boolean NO_CLIP = false;

    public final float ACCELERATION_FRAMES = 6;
    public final float DECELERATION_FRAMES = 3;

    public final float DIVE_SPEED = 6;
    public final float DIVE_FRICTION = .3f;
    public final float DIVE_FRICTION_SPEED = .317f;
    public final float DIVE_STARTING_SIN_COUNT = 1;
    public final float DIVE_END_TIME_FRAME = 0.2f;

    public final float ROLL_SPEED = 4;
    public final float ROLL_STARTING_SIN_COUNT = 0;
    public final float ROLL_FRICTION = .3f;
    public final float ROLL_FRICTION_SPEED = .317f;
    public final float ROLL_ENDING_TIME = 0.35f;
    public final float ROLL_JUMP_SPEED = .25f;
    public final float ROLL_JUMP_HEIGHT = 12f;

    private final float PLAYER_INVINCIBILITY_TIME = 1;

    public PlayerWalkingState walkingState;
    public PlayerDivingState divingState;
    public PlayerRollingState rollingState;
    public PlayerAttackingAndWalkingState attackingState;
    public PlayerFallingState fallingState;
    private static Map<AnimationName, DirectionalAnimation> animations = new HashMap<>();

    public Weapon equippedWeapon;
    public Weapon currentHeldItem;
    public int swordLevel = 0;
    public int bowLevel = 0;
    public WeaponInstances weaponInstances;
    private boolean holdingPickaxe = false;

    public int money;
    public boolean dead = false;
    public float deadTime = 0;
    public final float RESPAWN_TIME = 3;

    public Player(float x, float y) {
        super(x, y);
        damagesPlayer = false;
        isAttackable = true;
        maxInvincibilityTime = PLAYER_INVINCIBILITY_TIME;
    }

    public void setWeapons(WeaponInstances weapons) {
        this.weaponInstances = weapons;
        equippedWeapon = weapons.pickaxe;
        currentHeldItem = equippedWeapon;
    }

    @Override
    public void update() {
        handleInput();
        checkHolePositions();
        super.update();
        currentHeldItem.update();
    }

    private void checkHolePositions() {
        if (currentState != fallingState && currentState != divingState) {
            boolean[][] holePositions;
            if (Application.getInstance().currentScreen == Application.getInstance().caveScreen) {
                holePositions = Application.getInstance().caveScreen.getHolePositionsToCheck();
            } else if (Application.getInstance().currentScreen == Application.getInstance().townScreen) {
                holePositions = Application.getInstance().townScreen.getHolePositions();
            } else {
                return;
            }
            ArrayList<Vector2Int> wallTilesToCheck = collider.getWallTilePositionsToCheck();
            for (Vector2Int checkedWallTilePosition : wallTilesToCheck) {
                int x = 0, y;
                for (y = -1; y <= 1; y += 1) {
                    checkTile(checkedWallTilePosition, holePositions, x, y);
                }
                y=0;
                for (x = -1; x <= 1; x += 1) {
                    checkTile(checkedWallTilePosition, holePositions, x, y);
                }
            }
        }
    }

    private void checkTile(Vector2Int checkedWallTilePosition, boolean[][] holePositions, int x, int y) {
        Vector2Int v = new Vector2Int(checkedWallTilePosition.x + x, checkedWallTilePosition.y + y);
        Rectangle checkedTileBounds = new Rectangle(v.x * Application.TILE_WIDTH, v.y * Application.TILE_HEIGHT, Application.TILE_WIDTH, Application.TILE_HEIGHT);
        boolean isInBounds = v.x >= 0 && v.x < holePositions.length && v.y >= 0 && v.y < holePositions[0].length;
        if (isInBounds && holePositions[v.y][v.x]) {
            if (checkedTileBounds.contains(new Circle(getHitbox().x, getHitbox().y, getHitbox().radius-6))) {
                fallingState.enter();
            }
        }
    }




    private void handleInput() {
        if (GameInput.isMoving()) {
            direction = new Vector2Int(GameInput.getHorizontalAxisInt(), GameInput.getVerticalAxisInt());
        }
        if (GameInput.isSwitchButtonJustPressed()) {
            if (holdingPickaxe) {
                currentHeldItem = equippedWeapon;
            } else {
                currentHeldItem.exitAttackState();
                currentHeldItem = weaponInstances.pickaxe;
            }
            holdingPickaxe = !holdingPickaxe;
        }
    }

    @Override
    public void draw(Batch batch) {
        drawPlayerAndWeaponBasedOnZIndex(batch);
    }

    private void drawPlayerAndWeaponBasedOnZIndex(Batch batch) {
        if (currentHeldItem.zIndex == 0) {
            currentHeldItem.draw(batch);
            drawPlayer(batch);
            return;
        }
        drawPlayer(batch);
        currentHeldItem.draw(batch);
    }

    private void drawPlayer(Batch batch) {
        handleFlash(batch);
        if (inInvincibility) {
            batch.setColor(new Color(1,1,1,.5f));
        }
        animationManager.draw(batch);
        batch.setColor(Color.WHITE);
    }

    @Override
    public float getOriginX() {
        return PLAYER_WIDTH_PIXELS/2f;
    }

    @Override
    public Rectangle getBounds() {
        if (NO_CLIP) {
            return new Rectangle(0, 0, 0,0);
        }
        return new Rectangle(x+9, y+9, 14, 14);
    }

    @Override
    public Rectangle getShadowBounds() {
        return new Rectangle(x + 8, y + 6, Application.TILE_WIDTH, Application.TILE_HEIGHT);
    }

    @Override
    public Rectangle getFullBounds() {
        return new Rectangle(x, y, PLAYER_WIDTH_PIXELS, PLAYER_HEIGHT_PIXELS);
    }

    public Vector2 getCenteredPosition() {
        return new Vector2(x + getFullBounds().width/2, y + getFullBounds().height/2);
    }

    @Override
    public void createStates() {
        walkingState = new PlayerWalkingState(this);
        divingState = new PlayerDivingState(this);
        standingState = new PlayerStandingState(this);
        rollingState = new PlayerRollingState(this);
        attackingState = new PlayerAttackingAndWalkingState(this);
        fallingState = new PlayerFallingState(this);
        standingState.enter();
    }

    @Override
    protected void createAnimations() {
        DirectionalAnimation walk = new DirectionalAnimation("Character_Walk", .33f, Animation.PlayMode.LOOP);
        animations.put(AnimationName.WALK, walk);
        animations.put(AnimationName.STAND, walk.createDirectionalAnimationFromFirstFrames());
        animations.put(AnimationName.HIT, new DirectionalAnimation("Character_DiveDown", 32,32));
        animations.put(AnimationName.PLAYER_DIVE, new DirectionalAnimation("Character_Dive", .1f, Animation.PlayMode.NORMAL));
        animations.put(AnimationName.PLAYER_ROLL, new DirectionalAnimation("Character_Roll", .055f, Animation.PlayMode.NORMAL));
    }

    @Override
    protected Map<?,?> getAnimations() {
        return animations;
    }

    @Override
    public float getSpeed() {
        return 2;
    }

    @Override
    public int getMaxHp() {
        return 9;
    }
}
