package com.mikm.entities.animation;

import com.mikm.Vector2Int;

import java.util.*;

public enum Directions {
    LEFT(2, new Vector2Int(-1, 0)),
    RIGHT(2, new Vector2Int(1, 0)),
    UP(3, new Vector2Int(0, 1)),
    DOWN(0, new Vector2Int(0, -1)),
    UPLEFT(4, new Vector2Int(-1, 1)),
    UPRIGHT(4, new Vector2Int(1, 1)),
    DOWNLEFT(1, new Vector2Int(-1, -1)),
    DOWNRIGHT(1, new Vector2Int(1, -1));

    public final static int TOTAL = 5;
    private static final Map<Vector2Int, Integer> vector2IntToAnimationIndexMap = new HashMap<>();
    static {
        for (Directions value : values()) {
            vector2IntToAnimationIndexMap.put(value.vector2Int, value.animationIndex);
        }
    }

    public int animationIndex;
    public Vector2Int vector2Int;

    Directions(int animationIndex, Vector2Int vector2Int) {
        this.animationIndex = animationIndex;
        this.vector2Int = vector2Int;
    }

    public static int getAnimationIndexOfDirection(Vector2Int vector2Int) {
        return vector2IntToAnimationIndexMap.get(vector2Int);
    }
}
