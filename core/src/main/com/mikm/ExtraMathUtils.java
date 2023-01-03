package com.mikm;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class ExtraMathUtils {
    private ExtraMathUtils() {

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
