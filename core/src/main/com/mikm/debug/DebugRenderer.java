package com.mikm.debug;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.mikm.Assets;
import com.mikm.Method;
import com.mikm.entities.collision.Ray;
import com.mikm.rendering.Camera;
import com.mikm.rendering.screens.Application;

import java.util.ArrayList;

public class DebugRenderer {
    private static DebugRenderer instance;
    private final ShapeRenderer shapeRenderer;
    public static final Color DEBUG_BLUE = new Color(0, 0, 1, .75f);
    public static final Color DEBUG_RED = new Color(1, 0, 0, .75f);
    private ArrayList<Method> toDrawRepeatedly = new ArrayList<>();

    private DebugRenderer() {
        shapeRenderer = new ShapeRenderer();
    }

    public static DebugRenderer getInstance() {
        if (instance == null) {
            instance = new DebugRenderer();
        }
        return instance;
    }

    public void update() {
        for (Method method : toDrawRepeatedly) {
            method.invoke();
        }
    }

    public void dispose() {
        shapeRenderer.dispose();
    }

    public void drawHitboxes(Circle hitbox) {
        Application.batch.end();
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.setProjectionMatrix(Camera.orthographicCamera.combined);
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.begin();
        shapeRenderer.circle(hitbox.x, hitbox.y, hitbox.radius);
        shapeRenderer.end();
        Application.batch.begin();
    }

    public void drawLine(Ray rayIn, float lengthMultiplier) {
        Ray ray = new Ray(rayIn.startPoint,
                new Vector2(lengthMultiplier * rayIn.endPoint.x + (1-lengthMultiplier) * rayIn.startPoint.x,
                        lengthMultiplier * rayIn.endPoint.y + (1-lengthMultiplier) * rayIn.startPoint.y));
        drawLine(ray);
    }

    public void drawLine(Ray ray) {
        Application.batch.end();
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.setProjectionMatrix(Camera.orthographicCamera.combined);
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.begin();
        shapeRenderer.line(ray.startPoint, ray.endPoint);
        shapeRenderer.end();
        Application.batch.begin();
    }

    public void drawPoint(float x, float y) {
        Application.batch.setColor(DEBUG_BLUE);
        Application.batch.draw(Assets.testTexture, x-1, y-1, 3, 3);
        Application.batch.setColor(Color.WHITE);
    }

    public void drawTile(float tileX, float tileY) {
        Application.batch.setColor(DEBUG_BLUE);
        Application.batch.draw(Assets.testTexture, tileX * Application.TILE_WIDTH, tileY * Application.TILE_HEIGHT);
        Application.batch.setColor(Color.WHITE);
    }

    public void drawTile(float tileX, float tileY, Color color) {
        Application.batch.setColor(color);
        Application.batch.draw(Assets.testTexture, tileX * Application.TILE_WIDTH, tileY * Application.TILE_HEIGHT);
        Application.batch.setColor(Color.WHITE);
    }

    public void drawTile(float tileX, float tileY, float width, float height) {
        Application.batch.setColor(DEBUG_BLUE);
        Application.batch.draw(Assets.testTexture, tileX * Application.TILE_WIDTH, tileY * Application.TILE_HEIGHT, width, height);
        Application.batch.setColor(Color.WHITE);
    }


    public void doRepeatedly(Method method) {
        toDrawRepeatedly.add(method);
    }

    public void stopCallingLastMethod() {
        if (toDrawRepeatedly.size() > 0) {
            toDrawRepeatedly.remove(toDrawRepeatedly.size() - 1);
        }
    }
}
