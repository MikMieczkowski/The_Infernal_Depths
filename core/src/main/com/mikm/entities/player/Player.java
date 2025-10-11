package com.mikm.entities.player;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.*;
import com.mikm.Vector2Int;
import com.mikm.entities.Entity;
import com.mikm.entities.inanimateEntities.Grave;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.animation.DirectionalAnimation;
import com.mikm.entities.player.weapons.Weapon;
import com.mikm.entities.player.weapons.WeaponInstances;
import com.mikm.entityLoader.Blackboard;
import com.mikm.input.GameInput;
import com.mikm.rendering.cave.RockType;
import com.mikm.rendering.screens.Application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Player extends Entity {
    private final boolean NO_CLIP = false;

    private final float PLAYER_INVINCIBILITY_TIME = 1;

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
        effectsHandler.maxInvincibilityTime = PLAYER_INVINCIBILITY_TIME;
    }

    public void setWeapons(WeaponInstances weapons) {
        this.weaponInstances = weapons;
        equippedWeapon = weapons.pickaxe;
        currentHeldItem = equippedWeapon;
    }

    @Override
    public void update() {
        handleInput();
        super.update();
        currentHeldItem.update();
}

    @Override
    public void die() {
        super.die();
        dead = true;
        if (RockType.playerHasAnyTempOre()) {
            Application.getInstance().currentScreen.addInanimateEntity(new Grave(16 * (int) Application.player.x / 16, 16 * (int) Application.player.y / 16));
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
        if (GameInput.isPickaxeButtonJustPressed()) {
            currentHeldItem.exitAttackState();
            currentHeldItem = weaponInstances.pickaxe;
            holdingPickaxe = true;
        }
        if (GameInput.isWeaponButtonJustPressed()) {
            currentHeldItem = equippedWeapon;
            holdingPickaxe = false;
        }
    }

    @Override
    public void draw(Batch batch) {
        drawPlayerAndWeaponBasedOnZIndex(batch);
        if ((boolean)Blackboard.getInstance().getVar(this, "canFall")) {
            batch.draw(GameInput.getTalkButtonImage(), x+8, y+20);
        }
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
        effectsHandler.handleFlash(batch);
        if (effectsHandler.inInvincibility) {
            batch.setColor(new Color(1,1,1,.5f));
        }
        animationHandler.draw(batch);
        batch.setColor(Color.WHITE);
    }
}
