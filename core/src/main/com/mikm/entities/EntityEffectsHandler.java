package com.mikm.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.mikm.DeltaTime;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.Entity;
import com.mikm.rendering.screens.Application;

public class EntityEffectsHandler {
    private float squishTimer;
    private float squishAmount;
    private boolean squishing;
    private float preSquishTimer;
    private boolean triggerSquish = false;
    private float squishDelay;
    private float timeSpentSquishing;

    private final int MAX_FLASH_TIME = 2;
    private boolean shouldFlash;
    private float flashTimerFrames;
    private Color flashColor;
    public boolean inInvincibility;
    private float invincibilityTimer;
    private final float MAX_ENEMY_INVINCIBILITY_TIME = .3f;
    public float maxInvincibilityTime = MAX_ENEMY_INVINCIBILITY_TIME;

    private Entity entity;
    EntityEffectsHandler(Entity entity) {
        this.entity = entity;
    }

    public void handleSquishAndInvincibility() {
        handleSquish();
        handleInvincibility();
    }
    public void handleFlash(Batch batch) {
        if (shouldFlash) {
            Application.getInstance().setFillColorShader(batch, flashColor);
            flashTimerFrames += DeltaTime.deltaTime();
            if (flashTimerFrames >= MAX_FLASH_TIME) {
                shouldFlash = false;
                batch.setShader(null);
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

    private void handleSquish() {
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
            entity.xScale = ExtraMathUtils.lerp(squishTimer, timeSpentSquishing, 1, squishAmount);
            entity.yScale = ExtraMathUtils.lerp(squishTimer, timeSpentSquishing, 1, 1 / squishAmount);
            if (squishTimer < timeSpentSquishing) {
                squishTimer += Gdx.graphics.getDeltaTime();
            } else {
                squishTimer = 0;
                squishing = false;
            }
        } else {
            entity.xScale = MathUtils.lerp(entity.xScale, 1, .5f);
            entity.yScale = MathUtils.lerp(entity.yScale, 1, .5f);
        }
    }

    private void handleInvincibility() {
        if (inInvincibility) {
            invincibilityTimer += Gdx.graphics.getDeltaTime();
            if (invincibilityTimer > maxInvincibilityTime) {
                invincibilityTimer = 0;
                inInvincibility = false;
            }
        }
    }
}
