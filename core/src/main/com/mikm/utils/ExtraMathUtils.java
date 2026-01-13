package com.mikm.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mikm.Vector2Int;
import com.mikm.rendering.screens.Application;

public class ExtraMathUtils {

    private ExtraMathUtils() {

    }

    public static Color lerpColor(float timer, float maxTime, Color startColor, Color endColor) {
        float progress = timer/maxTime;
        return new Color(
                lerp(timer, maxTime, startColor.r, endColor.r),
                lerp(timer, maxTime, startColor.g, endColor.g),
                lerp(timer, maxTime, startColor.b, endColor.b),
                lerp(timer, maxTime, startColor.a, endColor.a));
    }

    public static float lerpAngle(float timer, float maxTime, float startValue, float endValue) {
        float progress = timer / maxTime;
        if (Math.abs(startValue - endValue) >= MathUtils.PI) {
            if (startValue > endValue) {
                startValue = normalize_angle(startValue) - MathUtils.PI2;
            } else {
                endValue = normalize_angle(endValue) - MathUtils.PI2;
            }
        }
        return MathUtils.lerp(startValue, endValue, progress);
    }

    private static float normalize_angle(float angle) {
        return wrappingModulo(angle + MathUtils.PI, MathUtils.PI2) - MathUtils.PI;
    }

    private static float wrappingModulo(float p_x, float p_y) {
        float value = p_x % p_y;
        if (((value < 0) && (p_y > 0)) || ((value > 0) && (p_y < 0))) {
            value += p_y;
        }
        value += 0f;
        return value;
    }


    public static float lerp(float timer, float maxTime, float startValue, float endValue) {
        float progress = timer / maxTime;
        if (progress > 1) {
            return endValue;
        }
        return MathUtils.lerp(startValue, endValue, progress);
    }

    public static float lerp(float timer, float maxTime, float startProportion, float endProportion, float startValue, float endValue) {
        final float startTime = startProportion * maxTime;
        final float endTime = endProportion * maxTime;
        if (timer + startTime > endTime) {
            return endValue;
        }
        return MathUtils.lerp(startValue, endValue, (timer + startTime) / maxTime);
    }


    public static float inverseLerp(float a, float b, float value) {
        return (value - a) / (b - a);
    }



    //bounceCoefficient: nextPeakHeight = lastPeakHeight * bounceCoefficient.
    // 0 = straight line
    public static float bounceLerp(float timer, float maxTime, float peakValue, float bounceCoefficient, float bounceFrequency) {
        if (timer > maxTime) {
            return 0;
        }
        return (float)Math.pow(1/bounceCoefficient, -timer) * Math.abs(peakValue*MathUtils.sin(bounceFrequency*timer));
    }

    public static float sinLerp(float timer, float maxTime, float peakValue) {
        if (timer > maxTime) {
            return 0;
        }
        final float timeStretch = (1f / maxTime) * MathUtils.PI;
        return peakValue * MathUtils.sin(timeStretch * timer);
    }

    //piecewise parabolic curve going up from 0 to accProp (0-1) * timer, peak at peakValue, decSpeed controlling speed of descent. accProp controls speed of ascent.
    public static float skewedSinLerp(float timer, float maxTime, float peakValue, float accProp, float decSpeed) {
        float accSpeed = peakValue / accProp*accProp;
        float accTime = maxTime * accProp;
        float f = 0;
        if (timer < accTime) {
            f= accSpeed * timer*timer;
        } else {
            float tMinusAccTime = (timer - accTime);
            f= -decSpeed * tMinusAccTime*tMinusAccTime + peakValue;
        }
        return MathUtils.clamp(f, 0, peakValue);
    }

    /**
     * Returns a sine-based interpolation value that scales smoothly between a start and end proportion
     * of a time range, reaching a specified peak value.
     * <p>
     * This function is useful for creating smooth oscillations or easing effects that begin and end
     * within defined proportions of an overall duration.
     *
     * @param timer           The current time or progress value.
     * @param maxTime         The total duration or maximum time span.
     * @param startProportion The normalized start point of the interpolation (0–1 range of maxTime).
     * @param endProportion   The normalized end point of the interpolation (0–1 range of maxTime).
     * @param peakValue       The maximum amplitude or output scaling factor.
     * @return The interpolated sine value at the current timer position.
     *
     * @example
     * <pre>
     * float value = sinLerp(0.5f, 1f, 0f, 1f, 10f);
     * // Produces a smooth sine curve rising to 10 and back down across the full time range.
     * </pre>
     */
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

    public static float remap(float inputMin, float inputMax, float outputMin, float outputMax, float value) {
        return MathUtils.lerp(outputMin, outputMax, inverseLerp(inputMin, inputMax, value));
    }

    public static float distance(float x1, float y1, float x2, float y2) {
        return (float)Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
    }

    public static float roundToTenths(float num) {
        return MathUtils.round(num * 10)/10f;
    }

    public static Vector2Int toTileCoordinates(Vector2Int worldCoordinates) {
        return new Vector2Int(worldCoordinates.x / Application.TILE_WIDTH, worldCoordinates.y / Application.TILE_HEIGHT);
    }

    public static Vector2Int toTileCoordinates(Vector2 worldCoordinates) {
        return new Vector2Int((int)worldCoordinates.x / Application.TILE_WIDTH, (int)worldCoordinates.y / Application.TILE_HEIGHT);
    }

    public static Vector2Int toTileCoordinates(float x, float y) {
        return new Vector2Int((int)x / Application.TILE_WIDTH, (int)y / Application.TILE_HEIGHT);
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

    public static Vector2Int minComponents(Vector2Int v1, Vector2Int v2) {
        return new Vector2Int(Math.min(v1.x, v2.x), Math.min(v1.y, v2.y));
    }

    public static Vector2Int maxComponents(Vector2Int v1, Vector2Int v2) {
        return new Vector2Int(Math.max(v1.x, v2.x), Math.max(v1.y, v2.y));
    }

    public static float clamp(float n, float a, float b) {
        return Math.max(a, Math.min(n,b));
    }

    public static int ceilAwayFromZero(float n) {
        if (n >= 0) {
            return MathUtils.ceil(n);
        }
        return -MathUtils.ceil(-n);
    }

    public static int sign(float num) {
        if (num > 0) {
            return 1;
        }
        if (num == 0) {
            return 0;
        }
        return -1;
    }

    public static boolean haveSameSign(float num1, float num2) {
        return sign(num1) == sign(num2);
    }

    //Random number between SPEED_MIN and SPEED_MAX, times pos or neg 1
    public static float getRandomWanderVel(float SPEED_MIN, float SPEED_MAX) {
        float randomForcePositive = RandomUtils.getFloat(SPEED_MIN, SPEED_MAX);
        int randomSign = RandomUtils.getBoolean() ? 1 : -1;
        return randomSign * randomForcePositive;
    }

    //Turns an angle to a direction vector with elements that are -1, 0 or 1.
    public static Vector2Int angleToVector2Int(float radians) {
        return new Vector2Int(MathUtils.round(MathUtils.cos(radians)), MathUtils.round(MathUtils.sin(radians)));
    }
}
