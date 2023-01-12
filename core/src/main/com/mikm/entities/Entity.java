package com.mikm.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.mikm.Vector2Int;
import com.mikm.entities.animation.EntityActionSpritesheets;
import com.mikm.entities.enemies.states.DamagedState;
import com.mikm.rendering.screens.Application;


public abstract class Entity extends InanimateEntity {

    public float originX, originY;
    public float rotation;

    public Vector2Int direction = Vector2Int.DOWN;

    public int hp;
    public float speed;

    public State walkingState;
    public State standingState;
    public DamagedState damagedState;
    public State currentState;
    public State detectedPlayerState;
    public EntityActionSpritesheets entityActionSpritesheets;

    private final float SQUISH_SPEED = .5f;
    private float squishTime;
    private float squishAmount;
    private boolean squishing;
    private float preSquishTimer;
    private boolean triggerSquish = false;
    private float squishDelay;

    private final int MAX_FLASH_TIME = 2;
    private boolean shouldFlash;
    private int flashTimerFrames;
    private Color flashColor;

    public boolean damagesPlayer = true;
    public boolean isAttackable = true;


    public Entity(int x, int y, EntityActionSpritesheets entityActionSpritesheets) {
        super(x,y);
        this.entityActionSpritesheets = entityActionSpritesheets;
        damagedState = new DamagedState(this);
        hp = getMaxHp();
        createStates();
    }

    @Override
    public void update() {
        currentState.update();
        currentState.checkForStateTransition();
        checkWallCollisions();
        x += xVel;
        y += yVel;
    }

    @Override
    public void draw(Batch batch) {
        handleFlash(batch);
        currentState.animationManager.draw(batch);
    }

    public void handleFlash(Batch batch) {
        if (shouldFlash) {
            Application.setFillColorShader(batch, flashColor);
            flashTimerFrames++;
            if (flashTimerFrames >= MAX_FLASH_TIME) {
                shouldFlash = false;
                flashTimerFrames = 0;
            }
        } else {
            batch.setShader(null);
        }
    }
    public void die() {
        Application.currentScreen.entities.remove(this);
    }

    public abstract void createStates();

    public void updateSquish() {
        if (triggerSquish) {
            if (preSquishTimer > squishDelay) {
                preSquishTimer = 0;
                triggerSquish = false;
                squishing = true;
            } else {
                preSquishTimer += Gdx.graphics.getDeltaTime();
            }
        }
        if (squishing) {
            xScale = MathUtils.lerp(1, squishAmount, squishTime);
            yScale = MathUtils.lerp(1, 1 / squishAmount, squishTime);
            if (squishTime < 1) {
                squishTime += SQUISH_SPEED;
            } else {
                squishTime = 0;
                squishing = false;
            }
        } else {
            xScale = MathUtils.lerp(xScale, 1, .5f);
            yScale = MathUtils.lerp(yScale, 1, .5f);
        }
    }

    public void startSquish(float squishDelay, float squishAmount) {
        triggerSquish = true;
        this.squishDelay = squishDelay;
        this.squishAmount = squishAmount;
    }

    public void flash(Color color) {
        shouldFlash = true;
        flashColor = color;
    }

    public abstract int getMaxHp();
}
