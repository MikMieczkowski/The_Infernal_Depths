package com.mikm.debug;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.mikm.entities.collision.Ray;
import com.mikm.rendering.Camera;
import com.mikm.rendering.screens.Application;

public class Debug {
    private static final Color testColor = new Color(0, 0, 1, .75f);
    public static void drawHitboxes(Circle hitbox) {
        Application.batch.end();
        Application.debugShapeRenderer.setAutoShapeType(true);
        Application.debugShapeRenderer.setProjectionMatrix(Camera.orthographicCamera.combined);
        Application.debugShapeRenderer.setColor(Color.BLUE);
        Application.debugShapeRenderer.begin();
        Application.debugShapeRenderer.circle(hitbox.x, hitbox.y, hitbox.radius);
        Application.debugShapeRenderer.end();
        Application.batch.begin();
    }

    public static void drawLine(Ray rayIn, float lengthMultiplier) {
        Ray ray = new Ray(rayIn.startPoint,
                new Vector2(lengthMultiplier * rayIn.endPoint.x + (1-lengthMultiplier) * rayIn.startPoint.x,
                        lengthMultiplier * rayIn.endPoint.y + (1-lengthMultiplier) * rayIn.startPoint.y));
        drawLine(ray);
    }

    public static void drawLine(Ray ray) {
        Application.batch.end();
        Application.debugShapeRenderer.setAutoShapeType(true);
        Application.debugShapeRenderer.setProjectionMatrix(Camera.orthographicCamera.combined);
        Application.debugShapeRenderer.setColor(Color.BLUE);
        Application.debugShapeRenderer.begin();
        Application.debugShapeRenderer.line(ray.startPoint, ray.endPoint);
        Application.debugShapeRenderer.end();
        Application.batch.begin();
    }

    public static void drawPoint(float x, float y) {
        Application.batch.setColor(testColor);
        Application.batch.draw(Application.testTexture, x-1, y-1, 3, 3);
        Application.batch.setColor(Color.WHITE);
    }

    public static void drawTile(float x, float y) {
        Application.batch.setColor(testColor);
        Application.batch.draw(Application.testTexture, x, y);
        Application.batch.setColor(Color.WHITE);
    }

    public static void drawTile(float x, float y, float width, float height) {
        Application.batch.setColor(testColor);
        Application.batch.draw(Application.testTexture, x, y, width, height);
        Application.batch.setColor(Color.WHITE);
    }

}
