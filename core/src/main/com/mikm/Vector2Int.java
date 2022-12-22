package com.mikm;

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

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
