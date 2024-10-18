package com.mikm.entities.particles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.mikm.Assets;
import com.mikm.rendering.Camera;
import com.mikm.rendering.cave.RockType;

public class ParticleTypes {
    TextureRegion image;
    boolean hasGravity, hasShadow, shouldDecelerate, collidesWithWalls;

    public float peakHeight;

    int positionOffsetRadius;
    float angleMin, angleMax, speedMin, speedMax, sizeMin, sizeMax, maxLifeTime, proportionOfTimeSpentDecelerating;
    int amountMin, amountMax;

    boolean usesColor;
    Color startColorMin, endColorMin;
    Color startColorMax, endColorMax;
    float finalScale = 0;

    public static ParticleTypes getRockParameters(RockType rockType) {
        ParticleTypes rockParameters = new ParticleTypes();
        rockParameters.image = rockType.getParticleImage();
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

    public static ParticleTypes getKnockbackDustParameters() {
        ParticleTypes knockbackDustParameters = new ParticleTypes();
        knockbackDustParameters.image = Assets.particleImages[0][0];
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

    public static ParticleTypes getDiveDustParameters() {
        ParticleTypes diveDustParameters = new ParticleTypes();
        diveDustParameters.image = Assets.particleImages[0][0];
        diveDustParameters.positionOffsetRadius = 5;
        diveDustParameters.hasGravity = false;
        diveDustParameters.hasShadow = false;
        diveDustParameters.angleMin = 0;
        diveDustParameters.angleMax = MathUtils.PI2;
        diveDustParameters.amountMin = 6;
        diveDustParameters.amountMax = 8;
        diveDustParameters.speedMin = .3f;
        diveDustParameters.peakHeight = 4;
        diveDustParameters.speedMax = .5f;
        diveDustParameters.sizeMin = .1f;
        diveDustParameters.shouldDecelerate = true;
        diveDustParameters.proportionOfTimeSpentDecelerating = 1;
        diveDustParameters.sizeMax = 1.5f;
        diveDustParameters.maxLifeTime = .8f;
        diveDustParameters.collidesWithWalls = true;
        return diveDustParameters;
    }

    public static ParticleTypes getArrowParameters() {
        ParticleTypes arrowParameters = new ParticleTypes();
        arrowParameters.image = Assets.particleImages[0][2];
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

    public static ParticleTypes getSlimeTrailParameters() {
        ParticleTypes slimeTrailParameters = new ParticleTypes();
        slimeTrailParameters.usesColor = true;
        int r1 = 60, g1 = 91, b1 = 44;
        int r2 = 18, g2 = 33, b2 = 24;
        slimeTrailParameters.startColorMin = new Color(r1/255f, g1/255f, b1/255f, 1);
        slimeTrailParameters.startColorMax = new Color((r1-10)/255f, (g1-10)/255f, (b1-10)/255f, 1);
        slimeTrailParameters.endColorMin = new Color(r2/255f, g2/255f, b2/255f, 1);
        slimeTrailParameters.endColorMax = new Color((r2-10)/255f, (g2-10)/255f, (b2-10)/255f, 1);
        slimeTrailParameters.finalScale = 1.2f;
        slimeTrailParameters.image = Assets.particleImages[0][0];
        slimeTrailParameters.positionOffsetRadius = 9;
        slimeTrailParameters.hasGravity = false;
        slimeTrailParameters.hasShadow = false;
        slimeTrailParameters.angleMin = 0;
        slimeTrailParameters.angleMax = MathUtils.PI2;
        slimeTrailParameters.amountMin = 9;
        slimeTrailParameters.amountMax = 9;
        slimeTrailParameters.speedMin = 0f;
        slimeTrailParameters.speedMax = .1f;
        slimeTrailParameters.peakHeight = 4;
        slimeTrailParameters.sizeMin = 1.5f;
        slimeTrailParameters.sizeMax = 1.5f;
        slimeTrailParameters.shouldDecelerate = true;
        slimeTrailParameters.proportionOfTimeSpentDecelerating = 1;
        slimeTrailParameters.maxLifeTime = 1f;
        slimeTrailParameters.collidesWithWalls = true;
        return slimeTrailParameters;
    }

    public static ParticleTypes getLightningParameters() {
        ParticleTypes lightningParameters = new ParticleTypes();
        lightningParameters.image = Assets.particleImages[0][1];
        lightningParameters.amountMin = 3;
        lightningParameters.amountMax = 3;
        lightningParameters.positionOffsetRadius = 0;
        lightningParameters.sizeMin = .5f;
        lightningParameters.sizeMax = .7f;
        lightningParameters.speedMin = 2f;
        lightningParameters.speedMax = 2f;
        lightningParameters.proportionOfTimeSpentDecelerating = 1;
        lightningParameters.maxLifeTime = .2f;
        lightningParameters.hasGravity = true;
        lightningParameters.hasShadow = false;
        lightningParameters.shouldDecelerate = true;
        lightningParameters.angleMin = 0;
        lightningParameters.angleMax = MathUtils.PI2;
        lightningParameters.peakHeight = 15;
        return lightningParameters;
    }

    public static ParticleTypes getDestructibleParameters(TextureRegion particleImage) {
        ParticleTypes output = getRockParameters(RockType.NORMAL);
        output.image = particleImage;
        return output;
    }
}
