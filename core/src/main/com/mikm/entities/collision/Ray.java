package com.mikm.entities.collision;

import com.badlogic.gdx.math.Vector2;

public class Ray {
    public Vector2 startPoint;
    public Vector2 endPoint;
    public Vector2 pqVector;

    public Ray(Vector2 startPoint, Vector2 direction) {
        this.startPoint = startPoint;
        this.endPoint = direction;
        calculatePQVector();
    }

    public Ray(float startX, float startY, float endX, float endY) {
        startPoint = new Vector2(startX, startY);
        endPoint = new Vector2(endX, endY);
        calculatePQVector();
    }

    public Vector2 getPointFromTValue(float t) {
        return new Vector2(startPoint.x + pqVector.x * t, startPoint.y + pqVector.y * t);
    }

    private void calculatePQVector() {
        pqVector = new Vector2(endPoint.x - startPoint.x, endPoint.y - startPoint.y);
    }
}
