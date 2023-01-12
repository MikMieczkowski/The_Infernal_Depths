package com.mikm.entities.player;


import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mikm.Vector2Int;
import com.mikm.entities.Entity;
import com.mikm.entities.animation.EntityActionSpritesheets;
import com.mikm.entities.player.states.*;
import com.mikm.entities.player.weapons.Weapon;
import com.mikm.entities.player.weapons.WeaponInstances;
import com.mikm.input.GameInput;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.tilemap.RockType;

public class Player extends Entity {
    public static final int PLAYER_WIDTH_PIXELS = 32, PLAYER_HEIGHT_PIXELS = 32;
    private final boolean NO_CLIP = false;

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

    public PlayerWalkingState walkingState;
    public PlayerDivingState divingState;
    public PlayerRollingState rollingState;
    public PlayerAttackingState attackingState;

    public Weapon equippedWeapon;
    public Weapon currentHeldItem;
    private WeaponInstances weaponInstances;
    private boolean holdingPickaxe = false;

    public int[] oreAmounts = new int[RockType.SIZE];


    public Player(int x, int y, EntityActionSpritesheets entityActionSpritesheets) {
        super(x, y, entityActionSpritesheets);
        originX = PLAYER_WIDTH_PIXELS/2f;
        originY = 0;
        speed = 2;
        damagesPlayer = false;
    }

    public void setWeapons(WeaponInstances weapons) {
        this.weaponInstances = weapons;
        equippedWeapon = weapons.sword;
        currentHeldItem = equippedWeapon;
    }

    @Override
    public void update() {
        handleInput();
        currentHeldItem.update();
        super.update();
    }

    private void handleInput() {
        if (GameInput.isMoving()) {
            direction = new Vector2Int(GameInput.getHorizontalAxisInt(), GameInput.getVerticalAxisInt());
        }
        if (GameInput.isSwitchButtonJustPressed()) {
            if (holdingPickaxe) {
                currentHeldItem = equippedWeapon;
            } else {
                currentHeldItem = weaponInstances.pickaxe;
            }
            holdingPickaxe = !holdingPickaxe;
        }
    }

    @Override
    public void draw(Batch batch) {
        handleFlash(batch);
        drawPlayerAndWeaponBasedOnZIndex(batch);
    }

    private void drawPlayerAndWeaponBasedOnZIndex(Batch batch) {
        if (currentHeldItem.zIndex == 0) {
            currentHeldItem.draw(batch);
            currentState.animationManager.draw(batch);
            return;
        }
        currentState.animationManager.draw(batch);
        currentHeldItem.draw(batch);
    }

    @Override
    public Rectangle getBounds() {
        if (NO_CLIP) {
            return new Rectangle(0, 0, 0,0);
        }
        return new Rectangle(x+10, y+7, 12, 12);
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
        attackingState = new PlayerAttackingState(this);
        standingState.enter();
    }

    @Override
    public int getMaxHp() {
        return 10;
    }
}
