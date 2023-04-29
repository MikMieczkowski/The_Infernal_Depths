package com.mikm.rendering.cave;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.RandomUtils;
import com.mikm.rendering.screens.CaveScreen;
import com.mikm.rendering.screens.GameScreen;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum RockType {
    NORMAL(-1, 0), COPPER(0, 1), IRON(1, 5), CRYSTAL(2,15), INFERNAL(3,50), GEM(3,75);

    private static final List<RockType> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
    public static final int SIZE = VALUES.size();
    public int spritesheetPosition;
    public int sellPrice;
    public int oreAmount;

    RockType(int spritesheetPosition, int sellPrice) {
        this.spritesheetPosition = spritesheetPosition;
        this.sellPrice = sellPrice;
    }

    public void increaseOreAmount(RockType rockType) {
        if (rockType == NORMAL) {
            throw new RuntimeException("Can't increase ore amount of normal rock");
        }
        rockType.oreAmount++;
    }

    public TextureRegion getParticleImage() {
        if (this == NORMAL) {
            if (CaveScreen.getRecolorLevel() == 0) {
                return GameScreen.particleImages[0][4];
            } else {
                return GameScreen.particleImages[1][0];
            }
        }
        return GameScreen.particleImages[1][spritesheetPosition+1];
    }

    public static RockType getRandomRockType(float[] chances) {
        float sum = 0;
        for (float num : chances) {
            sum += num;
        }
        if (sum > 1.01f || sum < .99f) {
            System.err.println("chances added to " + sum);
        }

        float totalChance = 0;
        float randomFloat = RandomUtils.getFloat(0, 1);
        for (int i =0; i < chances.length; i++) {
            totalChance += chances[i];
            if (randomFloat < totalChance) {
                return VALUES.get(i);
            }
        }
        System.err.println("didn't choose a random float in Rock Type");
        return VALUES.get(SIZE - 1);
    }
}
