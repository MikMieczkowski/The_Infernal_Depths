package com.mikm.rendering.tilemap;

import com.mikm.ExtraMathUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum RockType {
    NORMAL(-1), COPPER(0), IRON(1);

    private static final List<RockType> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
    public static final int SIZE = VALUES.size();
    public int spritesheetPosition;

    RockType(int spritesheetPosition) {
        this.spritesheetPosition = spritesheetPosition;
    }


    public static RockType getRandomRockType(float[] chances) {
        float sum = 0;
        for (float num : chances) {
            sum += num;
        }
        if (sum != 1) {
            throw new RuntimeException("chances must add to 1");
        }

        float totalChance = 0;
        float randomFloat = ExtraMathUtils.randomFloat(0, 1);
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
