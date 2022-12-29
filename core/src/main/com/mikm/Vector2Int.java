package com.mikm;

public class Vector2Int {
    public int x;
    public int y;
    public int animationIndex;

    public Vector2Int(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2Int(int x, int y, int animationIndex) {
        this.x = x;
        this.y = y;
        this.animationIndex = animationIndex;
    }

    public Vector2Int() {
        x = 0;
        y = 0;
    }

    public static Vector2Int ZERO = new Vector2Int(0, 0);

    public static Vector2Int LEFT = new Vector2Int(-1, 0, 2);
    public static Vector2Int RIGHT = new Vector2Int(1, 0, 2);
    public static Vector2Int UP = new Vector2Int(0, 1, 3);
    public static Vector2Int DOWN = new Vector2Int(0, -1, 0);
    public static Vector2Int UPLEFT = new Vector2Int(-1, 1, 4);
    public static Vector2Int UPRIGHT = new Vector2Int(1, 1, 4);
    public static Vector2Int DOWNLEFT = new Vector2Int(-1, -1, 1);
    public static Vector2Int DOWNRIGHT = new Vector2Int(1, -1, 1);
    public static Vector2Int[] DIRECTIONS = new Vector2Int[]{LEFT, RIGHT, UP, DOWN, UPLEFT, UPRIGHT, DOWNLEFT, DOWNRIGHT};


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
