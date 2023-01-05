package com.mikm;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.Random;

public class ExtraMathUtils {
    private static final long SEED = 21;
    private static final Random random = new Random();

    private ExtraMathUtils() {

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
