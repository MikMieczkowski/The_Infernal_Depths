package com.mikm.rendering.cave;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.utils.Assets;
import com.mikm.utils.RandomUtils;
import com.mikm.rendering.screens.CaveScreen;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

//refactor
public enum RockType {
    //refactor spritesheetPosition
    NORMAL(-1, 0),
    COPPER(0, 1),
    IRON(1, 5),
    CRYSTAL(2,15),
    INFERNAL(3,50),
    GEM(3,75);

    private static final List<RockType> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
    public static final int SIZE = VALUES.size();
    //these all act as static fields
    public int spritesheetPosition;
    public int sellPrice;
    private int oreAmount;
    public int tempOreAmount;

    RockType(int spritesheetPosition, int sellPrice) {
        this.spritesheetPosition = spritesheetPosition;
        this.sellPrice = sellPrice;
    }


    public int getOreAmount() {
        return oreAmount;
    }

    public void zeroOreAmount() {
        oreAmount = 0;
    }
    public void increaseOreAmount() {
        increaseOreAmount(1);
    }

    public void increaseOreAmount(int i) {
        oreAmount+=i;
        tempOreAmount+=i;
    }

    public static void validateOres() {
        for (int i = 0; i < SIZE; i++) {
            RockType.get(i).tempOreAmount = 0;
        }
    }

    public TextureRegion getParticleImage() {
        if (this == NORMAL) {
            if (CaveScreen.getRecolorLevel() == 0) {
                return Assets.particleImages[0][4];
            } else {
                return Assets.particleImages[1][0];
            }
        }
        return Assets.particleImages[1][spritesheetPosition+1];
    }

    public TextureRegion getRockImage() {
        if (this == NORMAL) {
            return CaveScreen.rockImages[CaveScreen.getRecolorLevel()][RandomUtils.getInt(2)];
        } else {
            return CaveScreen.oreImages[spritesheetPosition];
        }
    }

    public static boolean playerHasAnyOre() {
        return !(RockType.get(1).oreAmount == 0 && RockType.get(2).oreAmount == 0 && RockType.get(3).oreAmount == 0 && RockType.get(4).oreAmount == 0);
    }

    public static boolean playerHasAnyTempOre() {
        return !(RockType.get(1).tempOreAmount == 0 && RockType.get(2).tempOreAmount == 0 && RockType.get(3).tempOreAmount == 0 && RockType.get(4).tempOreAmount == 0);
    }

    public static RockType getRandomRockType(float[] chances) {
        float sum = 0;
        for (float num : chances) {
            sum += num;
        }
        if (sum > 1.01f || sum < .99f) {
            throw new RuntimeException("chances added to " + sum);
        }

        float totalChance = 0;
        float randomFloat = RandomUtils.getFloat(0, 1);
        for (int i =0; i < chances.length; i++) {
            totalChance += chances[i];
            if (randomFloat < totalChance) {
                return VALUES.get(i);
            }
        }
        throw new RuntimeException("didn't choose a random float in Rock Type");
        //return VALUES.get(SIZE - 1);
    }

    public static RockType get(int i) {
        return VALUES.get(i);
    }
}
