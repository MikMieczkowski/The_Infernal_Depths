package com.mikm.utils;

import com.badlogic.gdx.graphics.Color;

import java.util.Random;

public class RandomUtils {
    public static final long SEED = 21;
    private static final Random random = new Random();

    private RandomUtils() {

    }

    public static void setSeed(long s) {
        random.setSeed(s);
    }

    public static Color getColor(Color color1, Color color2) {
        float minRed = Math.min(color1.r,color2.r);
        float minBlue = Math.min(color1.g,color2.g);
        float minGreen = Math.min(color1.b,color2.b);
        float maxRed = Math.max(color1.r,color2.r);
        float maxBlue = Math.max(color1.g,color2.g);
        float maxGreen = Math.max(color1.b,color2.b);
        return new Color(
                getFloat(minRed, maxRed),
                getFloat(minBlue, maxBlue),
                getFloat(minGreen, maxGreen),
                1);
    }

    public static int getInt(int min, int max) {
        return random.nextInt(max+1-min)+min;
    }

    /**
     * Returns an integer from [0, max] inclusive
     */
    public static int getInt(int max) {
        return random.nextInt(max+1);
    }

    public static boolean getPercentage(int percentChance) {
        return getInt(100) < percentChance;
    }

    public static float getFloat(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    public static boolean getBoolean() {
        return random.nextBoolean();
    }

    public static float getFloatRoundedToTenths(int max) {
        return random.nextInt(max * 10)/10f;
    }
}
