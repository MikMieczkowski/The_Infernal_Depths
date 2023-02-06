package com.mikm;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.mikm.entities.collision.Ray;
import com.mikm.rendering.Camera;
import com.mikm.rendering.screens.Application;

public class Debug {
    public static void drawHitboxes(Circle hitbox) {
        Application.batch.end();
        Application.currentScreen.debugShapeRenderer.setAutoShapeType(true);
        Application.currentScreen.debugShapeRenderer.setProjectionMatrix(Camera.orthographicCamera.combined);
        Application.currentScreen.debugShapeRenderer.setColor(Color.BLUE);
        Application.currentScreen.debugShapeRenderer.begin();
        Application.currentScreen.debugShapeRenderer.circle(hitbox.x, hitbox.y, hitbox.radius);
        Application.currentScreen.debugShapeRenderer.end();
        Application.batch.begin();
    }

    public static void drawLine(Ray ray) {
        Application.batch.end();
        Application.currentScreen.debugShapeRenderer.setAutoShapeType(true);
        Application.currentScreen.debugShapeRenderer.setProjectionMatrix(Camera.orthographicCamera.combined);
        Application.currentScreen.debugShapeRenderer.setColor(Color.BLUE);
        Application.currentScreen.debugShapeRenderer.begin();
        Application.currentScreen.debugShapeRenderer.line(ray.startPoint, ray.endPoint);
        Application.currentScreen.debugShapeRenderer.end();
        Application.batch.begin();
    }

    public static void drawLine(Ray rayIn, float lengthMultiplier) {
        Ray ray = new Ray(rayIn.startPoint,
                new Vector2(lengthMultiplier * rayIn.endPoint.x + (1-lengthMultiplier) * rayIn.startPoint.x,
                        lengthMultiplier * rayIn.endPoint.y + (1-lengthMultiplier) * rayIn.startPoint.y));
        drawLine(ray);
    }
}
