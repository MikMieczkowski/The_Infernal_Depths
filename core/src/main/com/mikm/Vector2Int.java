package com.mikm;

import java.util.HashMap;

public class Vector2Int {
    public int x;
    public int y;

    public Vector2Int(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2Int() {
        x = 0;
        y = 0;
    }

    public static Vector2Int ZERO = new Vector2Int(0, 0);

    public static Vector2Int LEFT = new Vector2Int(-1, 0);
    public static Vector2Int RIGHT = new Vector2Int(1, 0);
    public static Vector2Int UP = new Vector2Int(0, 1);
    public static Vector2Int DOWN = new Vector2Int(0, -1);
    public static Vector2Int UPLEFT = new Vector2Int(-1, 1);
    public static Vector2Int UPRIGHT = new Vector2Int(1, 1);
    public static Vector2Int DOWNLEFT = new Vector2Int(-1, -1);
    public static Vector2Int DOWNRIGHT = new Vector2Int(1, -1);

    public static HashMap<Vector2Int, Integer> EIGHT_DIRECTIONAL_MAPPINGS;
    public static HashMap<Vector2Int, Integer> FOUR_DIRECTIONAL_MAPPINGS;
    public static HashMap<Vector2Int, Integer> TWO_DIRECTIONAL_MAPPINGS;

    static {
        EIGHT_DIRECTIONAL_MAPPINGS = new HashMap<>();
        EIGHT_DIRECTIONAL_MAPPINGS.put(LEFT, 2);
        EIGHT_DIRECTIONAL_MAPPINGS.put(RIGHT, 2);
        EIGHT_DIRECTIONAL_MAPPINGS.put(UP, 3);
        EIGHT_DIRECTIONAL_MAPPINGS.put(DOWN, 0);
        EIGHT_DIRECTIONAL_MAPPINGS.put(UPLEFT, 4);
        EIGHT_DIRECTIONAL_MAPPINGS.put(UPRIGHT, 4);
        EIGHT_DIRECTIONAL_MAPPINGS.put(DOWNLEFT, 1);
        EIGHT_DIRECTIONAL_MAPPINGS.put(DOWNRIGHT, 1);

        FOUR_DIRECTIONAL_MAPPINGS = new HashMap<>();
        FOUR_DIRECTIONAL_MAPPINGS.put(LEFT, 0);
        FOUR_DIRECTIONAL_MAPPINGS.put(RIGHT, 0);
        FOUR_DIRECTIONAL_MAPPINGS.put(UP, 1);
        FOUR_DIRECTIONAL_MAPPINGS.put(DOWN, 1);
        FOUR_DIRECTIONAL_MAPPINGS.put(UPLEFT, 1);
        FOUR_DIRECTIONAL_MAPPINGS.put(UPRIGHT, 1);
        FOUR_DIRECTIONAL_MAPPINGS.put(DOWNLEFT, 0);
        FOUR_DIRECTIONAL_MAPPINGS.put(DOWNRIGHT, 0);

        TWO_DIRECTIONAL_MAPPINGS = new HashMap<>();
        TWO_DIRECTIONAL_MAPPINGS.put(LEFT, 0);
        TWO_DIRECTIONAL_MAPPINGS.put(RIGHT, 0);
        TWO_DIRECTIONAL_MAPPINGS.put(UP, 0);
        TWO_DIRECTIONAL_MAPPINGS.put(DOWN, 0);
        TWO_DIRECTIONAL_MAPPINGS.put(UPLEFT, 0);
        TWO_DIRECTIONAL_MAPPINGS.put(UPRIGHT, 0);
        TWO_DIRECTIONAL_MAPPINGS.put(DOWNLEFT, 0);
        TWO_DIRECTIONAL_MAPPINGS.put(DOWNRIGHT, 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector2Int that = (Vector2Int) o;
        return x == that.x && y == that.y;
    }
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
