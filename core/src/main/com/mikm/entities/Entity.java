package com.mikm.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.mikm.ExtraMathUtils;
import com.mikm.Vector2Int;
import com.mikm.entities.animation.EntityActionSpritesheets;
import com.mikm.entities.enemies.states.DamagedState;
import com.mikm.rendering.screens.Application;


public abstract class Entity extends InanimateEntity {

    public float rotation;
    public Vector2Int direction = Vector2Int.DOWN;
    public int hp;

    public State walkingState;
    public State standingState;
    public DamagedState damagedState;
    public State detectedPlayerBuildUpState;
    public State currentState;
    public State detectedPlayerState;
    public EntityActionSpritesheets entityActionSpritesheets;

    private float squishTimer;
    private float squishAmount;
    private boolean squishing;
    private float preSquishTimer;
    private boolean triggerSquish = false;
    private float squishDelay;
    private float timeSpentSquishing;

    private final int MAX_FLASH_TIME = 2;
    private boolean shouldFlash;
    private int flashTimerFrames;
    private Color flashColor;

    public boolean damagesPlayer = true;
    public boolean isAttackable = true;

    public boolean inInvincibility;
    private float invincibilityTimer;
    private final float MAX_ENEMY_INVINCIBILITY_TIME = .3f;
    public float maxInvincibilityTime = MAX_ENEMY_INVINCIBILITY_TIME;


    public Entity(float x, float y, EntityActionSpritesheets entityActionSpritesheets) {
        super(x,y);
        this.entityActionSpritesheets = entityActionSpritesheets;
        damagedState = new DamagedState(this);
        hp = getMaxHp();
    }

    @Override
    public void update() {
        currentState.update();
        currentState.checkForStateTransition();
        handleSquish();
        handleInvincibility();
        collider.updateCollisions();
        x += xVel;
        y += yVel;
    }

    @Override
    public void draw(Batch batch) {
        handleFlash(batch);
        currentState.animationManager.draw(batch);
    }

    public void handleInvincibility() {
        if (inInvincibility) {
            invincibilityTimer += Gdx.graphics.getDeltaTime();
            if (invincibilityTimer > maxInvincibilityTime) {
                invincibilityTimer = 0;
                inInvincibility = false;
            }
        }
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

    public void startInvincibilityFrames() {
        inInvincibility = true;
        invincibilityTimer = 0;
    }

    public void die() {
        Application.currentScreen.removeEntity(this);
    }

    public abstract void createStates();

    public void handleSquish() {
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
            xScale = ExtraMathUtils.lerp(squishTimer, timeSpentSquishing, 1, squishAmount);
            yScale = ExtraMathUtils.lerp(squishTimer, timeSpentSquishing, 1, 1 / squishAmount);
            if (squishTimer < timeSpentSquishing) {
                squishTimer += Gdx.graphics.getDeltaTime();
            } else {
                squishTimer = 0;
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
        timeSpentSquishing = .02f;
    }

    public void startSquish(float squishDelay, float squishAmount, float timeSpentSquishing, boolean overrideLastSquish) {
        if (!overrideLastSquish && (squishing||triggerSquish)) {
            return;
        }
        triggerSquish = true;
        this.squishDelay = squishDelay;
        this.squishAmount = squishAmount;
        this.timeSpentSquishing = timeSpentSquishing;
    }

    public void stopSquish() {
        squishTimer = 0;
        preSquishTimer = 0;
        triggerSquish = false;
        squishing = false;
    }

    public void flash(Color color) {
        shouldFlash = true;
        flashColor = color;
    }


    public float getOriginX() {
        return 0;
    }

    public float getOriginY() {
        return 0;
    }

    public float getSpeed() {
        throw new RuntimeException("never defined this speed");
    }

    public abstract int getMaxHp();
}
