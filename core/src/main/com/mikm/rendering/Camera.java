package com.mikm.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.mikm.input.GameInput;
import com.mikm.rendering.screens.Application;

public class Camera {
    public static final float VIEWPORT_ZOOM = .25f;
    private final float CAMERA_SPEED = .2f;
    private final float LEAD_MULTIPLIER = 60;
    private final float CAMERA_LEAD_SPEED = .3f;
    private final float IGNORED_BOX_WIDTH = 1440 / 64f;
    private final float IGNORED_BOX_HEIGHT = 810 / 64f;

    public static OrthographicCamera orthographicCamera;
    public static float x, y;
    private static float lookDirectionX, lookDirectionY;
    private final Vector2 ignoredBoxOffset = new Vector2();

    public Camera() {
        orthographicCamera = new OrthographicCamera();
        orthographicCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        orthographicCamera.zoom = 1;

        setPositionDirectlyToPlayerPosition();
    }

    public void update() {
        lookDirectionX += (GameInput.getAttackingVector().x * LEAD_MULTIPLIER - lookDirectionX) * CAMERA_LEAD_SPEED;
        lookDirectionY += (GameInput.getAttackingVector().y * LEAD_MULTIPLIER - lookDirectionY) * CAMERA_LEAD_SPEED;

        Vector2 targetPosition = new Vector2(Application.player.getCenteredPosition().x - x, Application.player.getCenteredPosition().y - y);

        setIgnoredBoxOffsetAndMoveCamera(targetPosition);
        updateOrthographicCamera();
    }

    private void setIgnoredBoxOffsetAndMoveCamera(Vector2 targetPosition) {
        final float HALF_WIDTH = IGNORED_BOX_WIDTH/2f;
        final float HALF_HEIGHT = IGNORED_BOX_HEIGHT/2f;
        if (targetPosition.x > HALF_WIDTH) {
            ignoredBoxOffset.x = -HALF_WIDTH;
            x += (targetPosition.x + ignoredBoxOffset.x) * CAMERA_SPEED;
        }
        if (targetPosition.x < -HALF_WIDTH) {
            ignoredBoxOffset.x = HALF_WIDTH;
            x += (targetPosition.x + ignoredBoxOffset.x) * CAMERA_SPEED;
        }
        if (targetPosition.y > HALF_HEIGHT) {
            ignoredBoxOffset.y = -HALF_HEIGHT;
            y += (targetPosition.y + ignoredBoxOffset.y) * CAMERA_SPEED;
        }
        if (targetPosition.y < -HALF_HEIGHT) {
            ignoredBoxOffset.y = HALF_HEIGHT;
            y += (targetPosition.y + ignoredBoxOffset.y) * CAMERA_SPEED;
        }
    }

    private static void updateOrthographicCamera() {
        orthographicCamera.position.set(new Vector3(MathUtils.round((x + lookDirectionX) / VIEWPORT_ZOOM) * VIEWPORT_ZOOM, MathUtils.round((y+ lookDirectionY) / VIEWPORT_ZOOM) * VIEWPORT_ZOOM, 0));
        orthographicCamera.update();
    }

    public static void renderLighting(Batch batch) {
//        batch.draw(Application.dark, x - 1000, y-850);
//        batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_SRC_ALPHA); // Blend with background
//        batch.draw(Application.light, Application.player.x-128,Application.player.y-128); // Draw the circle of light
//        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA); // Normal rendering
    }

    public static void setPositionDirectlyToPlayerPosition() {
        x = Application.player.getCenteredPosition().x;
        y = Application.player.getCenteredPosition().y;
        updateOrthographicCamera();
    }

    public static Vector2 getMousePositionWorldCoordinates() {
        Vector2 mousePosRelativeToPlayer = GameInput.mousePosRelativeToPlayer();
        return new Vector2(mousePosRelativeToPlayer.x+x, mousePosRelativeToPlayer.y+y);
    }
}