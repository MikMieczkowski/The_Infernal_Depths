package com.mikm.entities.player;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.mikm.Vector2Int;
import com.mikm.entities.Entity;
import com.mikm.entities.player.states.*;
import com.mikm.entities.player.weapons.Weapon;
import com.mikm.entities.player.weapons.WeaponInstances;
import com.mikm.input.InputAxis;
import com.mikm.rendering.screens.Application;

import java.util.ArrayList;

public class Player extends Entity {
    public static final int playerWidthPixels = 32, playerHeightPixels = 32;
    private final boolean noClip = false;

    public final float diveSpeed = 6;
    public final float diveFriction = .3f;
    public final float diveFrictionSpeed = .317f;
    public final float diveStartingSinCount = 1;
    public final float diveEndTimeFrame = 0.2f;

    public final float rollSpeed = 4;
    public final float rollStartingSinCount = 0;
    public final float rollFriction = .3f;
    public final float rollFrictionSpeed = .317f;
    public final float rollEndingTime = 0.35f;
    public final float rollJumpSpeed = .25f;
    public final float rollJumpHeight = 12f;

    public PlayerStandingState standingState;
    public PlayerWalkingState walkingState;
    public PlayerDivingState divingState;
    public PlayerRollingState rollingState;
    public PlayerAttackingState attackingState;

    public Weapon currentWeapon;
    private WeaponInstances WEAPONS;


    public Player(int x, int y, ArrayList<TextureRegion[]> spritesheets) {
        super(x, y, spritesheets);
        originX = playerWidthPixels/2f;
        originY = 0;
        speed = 2;
    }

    public void setWeapons(WeaponInstances weapons) {
        this.WEAPONS = weapons;
        currentWeapon = weapons.sword;
    }

    @Override
    public void update() {
        if (InputAxis.isMoving()) {
            direction = new Vector2Int(InputAxis.getHorizontalAxisInt(), InputAxis.getVerticalAxisInt());
        }
        currentWeapon.update();
        super.update();
    }

    float time;
    @Override
    public void render(Batch batch) {
        time += Gdx.graphics.getDeltaTime();
        batch.setShader(Application.shader);
        Application.shader.bind();
        Application.shader.setUniformf("u_amount", 1f);
        Application.shader.setUniformf("u_speed", 2f);
        Application.shader.setUniformf("u_time", time);
        drawPlayerAndWeaponBasedOnZIndex(batch);
    }

    private void drawPlayerAndWeaponBasedOnZIndex(Batch batch) {
        if (currentWeapon.zIndex == 0) {
            currentWeapon.draw(batch);
            currentState.animationManager.draw(batch);
            return;
        }
        currentState.animationManager.draw(batch);
        currentWeapon.draw(batch);
    }

    @Override
    public Rectangle getBounds() {
        if (noClip) {
            return new Rectangle(0, 0, 0,0);
        }
        return new Rectangle(x+8, y+9, 16, 15);
    }

    @Override
    public Rectangle getFullBounds() {
        return new Rectangle(x, y, playerWidthPixels, playerHeightPixels);
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
        return 3;
    }
}
