package com.mikm;

import com.mikm.entities.animation.Directions;

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
        EIGHT_DIRECTIONAL_MAPPINGS.put(LEFT, Directions.LEFT);
        EIGHT_DIRECTIONAL_MAPPINGS.put(RIGHT, Directions.RIGHT);
        EIGHT_DIRECTIONAL_MAPPINGS.put(UP, Directions.UP);
        EIGHT_DIRECTIONAL_MAPPINGS.put(DOWN, Directions.DOWN);
        EIGHT_DIRECTIONAL_MAPPINGS.put(UPLEFT, Directions.UPLEFT);
        EIGHT_DIRECTIONAL_MAPPINGS.put(UPRIGHT, Directions.UPRIGHT);
        EIGHT_DIRECTIONAL_MAPPINGS.put(DOWNLEFT, Directions.DOWNLEFT);
        EIGHT_DIRECTIONAL_MAPPINGS.put(DOWNRIGHT, Directions.DOWNRIGHT);
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
