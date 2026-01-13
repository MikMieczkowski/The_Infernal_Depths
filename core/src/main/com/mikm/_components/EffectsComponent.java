package com.mikm._components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.graphics.Color;

@RuntimeDataComponent
public class EffectsComponent implements Component {
    public static final ComponentMapper<EffectsComponent> MAPPER = ComponentMapper.getFor(EffectsComponent.class);
    //squish
    //change to frames
    public float squishTimer;
    public float maxSquishTime;
    public float squishAmount;
    public boolean squishing;
    public float preSquishTimer;
    public boolean triggerSquish = false;
    public float squishDelay;

    //flash
    public boolean shouldFlash;
    public float flashTimer;
    public static final int MAX_FLASH_TIME = 2;
    public Color flashColor;

    //bouncing
    public float bounceTimer;
    public float maxBounceTime;
    //.1f
    public float bounceCoefficient;
    public final int BOUNCE_FREQUENCY = 8;
    public boolean shouldBounce;
    public float peakHeight;
    //changing color
    public float colorTimer;
    public float maxColorTime;
    public boolean shouldChangeColor;
    public Color startColor, endColor;
    //growing/shrinking
    public float sizeTimer;
    public float maxSizeTime;
    public boolean shouldChangeSize;
    public float startSize, endSize;


    public void startSquish(float squishDelay, float squishAmount) {
        triggerSquish = true;
        this.squishDelay = squishDelay;
        this.squishAmount = squishAmount;
        maxSquishTime = .02f;
    }
    public void startSquish(float squishDelay, float squishAmount, float maxSquishTime, boolean overrideLastSquish) {
        if (!overrideLastSquish && (squishing||triggerSquish)) {
            return;
        }
        triggerSquish = true;
        this.squishDelay = squishDelay;
        this.squishAmount = squishAmount;
        this.maxSquishTime = maxSquishTime;
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

    public void startColorChange(float maxColorTime, Color startColor, Color endColor) {
        this.maxColorTime = maxColorTime;
        this.startColor = startColor;
        this.endColor = endColor;
        colorTimer = 0;
        shouldChangeColor = true;
    }

    public void startSizeChange(float maxSizeTime, float sizeMin, float sizeMax) {
        this.maxSizeTime = maxSizeTime;
        this.endSize = sizeMax;
        this.startSize = sizeMin;
        sizeTimer = 0;
        shouldChangeSize = true;
    }

    public void startBouncing(float maxBounceTime, float bounceCoefficient, float peakHeight) {
        this.maxBounceTime = maxBounceTime;
        this.bounceCoefficient = bounceCoefficient;
        this.peakHeight = peakHeight;
        shouldBounce = true;
        bounceTimer = 0;
    }
}
