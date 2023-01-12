package com.mikm.entities.particles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.mikm.rendering.screens.GameScreen;
import com.mikm.rendering.tilemap.RockType;

public class ParticleParameters {
    TextureRegion image;
    boolean hasGravity, hasShadow, shouldDecelerate, collidesWithWalls;

    public float peakHeight;

    int positionOffsetRadius;
    float angleMin, angleMax, speedMin, speedMax, sizeMin, sizeMax, maxLifeTime, proportionOfTimeSpentDecelerating;
    Color startColor, endColor;
    int amountMin, amountMax;

    public static ParticleParameters getRockParameters(RockType rockType) {
        ParticleParameters rockParameters = new ParticleParameters();
        if (rockType == RockType.NORMAL) {
            rockParameters.image = GameScreen.particleImages[0][4];
        }
        if (rockType == RockType.COPPER) {
            rockParameters.image = GameScreen.particleImages[1][1];
        }
        if (rockType == RockType.IRON) {
            rockParameters.image = GameScreen.particleImages[1][2];
        }
        rockParameters.amountMin = 4;
        rockParameters.amountMax = 8;
        rockParameters.positionOffsetRadius = 5;
        rockParameters.sizeMin = .4f;
        rockParameters.sizeMax = .8f;
        rockParameters.speedMin = .4f;
        rockParameters.speedMax = .7f;
        rockParameters.proportionOfTimeSpentDecelerating = 1;
        rockParameters.maxLifeTime = 1f;
        rockParameters.hasGravity = true;
        rockParameters.hasShadow = true;
        rockParameters.shouldDecelerate = true;
        rockParameters.angleMin = 0;
        rockParameters.angleMax = MathUtils.PI2;
        rockParameters.peakHeight = 12;
        rockParameters.collidesWithWalls = true;
        return rockParameters;
    }

    public static ParticleParameters getKnockbackDustParameters() {
        ParticleParameters knockbackDustParameters = new ParticleParameters();
        knockbackDustParameters.image = GameScreen.particleImages[0][0];
        knockbackDustParameters.amountMin = 6;
        knockbackDustParameters.amountMax = 6;
        knockbackDustParameters.positionOffsetRadius = 5;
        knockbackDustParameters.sizeMin = .5f;
        knockbackDustParameters.sizeMax = 1f;
        knockbackDustParameters.speedMin = 1f;
        knockbackDustParameters.speedMax = 2f;
        knockbackDustParameters.proportionOfTimeSpentDecelerating = 1;
        knockbackDustParameters.maxLifeTime = 1f;
        knockbackDustParameters.hasGravity = false;
        knockbackDustParameters.hasShadow = false;
        knockbackDustParameters.shouldDecelerate = false;
        knockbackDustParameters.angleMin = -MathUtils.PI/8f;
        knockbackDustParameters.angleMax = MathUtils.PI/8f;
        knockbackDustParameters.peakHeight = 0;
        knockbackDustParameters.collidesWithWalls = false;
        return knockbackDustParameters;
    }

    public static ParticleParameters getDiveDustParameters() {
        ParticleParameters diveDustParameters = getKnockbackDustParameters();
        diveDustParameters.angleMin = 0;
        diveDustParameters.angleMax = MathUtils.PI2;
        diveDustParameters.amountMin = 16;
        diveDustParameters.amountMax = 24;
        diveDustParameters.speedMin = .5f;
        diveDustParameters.speedMax = 1f;
        diveDustParameters.sizeMin = .1f;
        diveDustParameters.sizeMax = 1f;
        diveDustParameters.maxLifeTime = .3f;
        diveDustParameters.collidesWithWalls = true;
        return diveDustParameters;
    }

    public static ParticleParameters getArrowParameters() {
        ParticleParameters arrowParameters = new ParticleParameters();
        arrowParameters.image = GameScreen.particleImages[0][2];
        arrowParameters.amountMin = 3;
        arrowParameters.amountMax = 3;
        arrowParameters.positionOffsetRadius = 0;
        arrowParameters.sizeMin = .5f;
        arrowParameters.sizeMax = .7f;
        arrowParameters.speedMin = .5f;
        arrowParameters.speedMax = 2f;
        arrowParameters.proportionOfTimeSpentDecelerating = 1;
        arrowParameters.maxLifeTime = 1f;
        arrowParameters.hasGravity = true;
        arrowParameters.hasShadow = false;
        arrowParameters.shouldDecelerate = false;
        arrowParameters.angleMin = 0;
        arrowParameters.angleMax = MathUtils.PI2;
        arrowParameters.peakHeight = 5;
        return arrowParameters;
    }
}
