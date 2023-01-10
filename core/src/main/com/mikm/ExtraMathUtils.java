package com.mikm;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.Random;

public class ExtraMathUtils {
    private static final long SEED = 21;
    private static final Random random = new Random();

    private ExtraMathUtils() {

    }

    public static float sinLerp(float timer, float maxTime, float peakValue) {
        if (timer > maxTime) {
            return 0;
        }
        final float timeStretch = (1f/maxTime) * MathUtils.PI;
        return peakValue * MathUtils.sin(timeStretch * timer);
    }

    public static float sinLerp(float timer, float maxTime, float startProportion, float endProportion, float peakValue) {
        final float startTime = startProportion * maxTime;
        final float endTime = endProportion * maxTime;

        final float timeStretch = (1f/maxTime) * MathUtils.PI;
        if (timer + startTime> endTime) {
            return peakValue * MathUtils.sin(timeStretch * endTime);
        }
        return peakValue * MathUtils.sin(timeStretch * (timer + startTime));
    }

    public static Vector2 sinLerpVector2(float timer, float maxTime, float startProportion, float endProportion, Vector2 peakValue) {
        return new Vector2(sinLerp(timer, maxTime, startProportion, endProportion, peakValue.x), sinLerp(timer, maxTime, startProportion, endProportion, peakValue.y));
    }

    public static int randomInt(int min, int max) {
        return random.nextInt(min, max);
    }

    public static int randomInt(int max) {
        return random.nextInt(max);
    }

    public static float randomFloat(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    public static boolean randomBoolean() {
        return random.nextBoolean();
    }

    public static float randomFloatOneDecimalPlace(int max) {
        return random.nextInt(max * 10)/10f;
    }

    public static float roundToTenths(float num) {
        return MathUtils.round(num * 10)/10f;
    }

    public static Vector2 normalizeAndScale(Vector2 vector2) {
        float magnitude = vector2.len();
        if (magnitude > 1) {
            magnitude = 1;
        }
        return new Vector2(vector2.nor().x * magnitude, vector2.nor().y * magnitude);
    }
    public static Vector2 normalizeAndScale(Vector2Int vector2Int) {
        return normalizeAndScale(new Vector2(vector2Int.x, vector2Int.y));
    }
}
