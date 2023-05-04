package com.mikm;


public class Vector2Int {
    public int x;
    public int y;

    public Vector2Int(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2Int(float x, float y) {
        this.x = (int)x;
        this.y = (int)y;
    }

    public Vector2Int(Vector2Int vector2Int) {
        x = vector2Int.x;
        y = vector2Int.y;
    }
    public Vector2Int() {
        x = 0;
        y = 0;
    }

    public static final Vector2Int ZERO = new Vector2Int(0, 0);

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }
}
