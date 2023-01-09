package com.mikm.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.mikm.Vector2Int;
import com.mikm.entities.player.Player;
import com.mikm.input.GameInput;
import com.mikm.rendering.screens.Application;

public class Camera {
    public static final float VIEWPORT_ZOOM = .25f;
    private final float CAMERA_SPEED = .2f;
    private final float LEAD_MULTIPLIER = 8;
    private final float CAMERA_LEAD_SPEED = .3f;
    private final float IGNORED_BOX_WIDTH = Application.WORLD_WIDTH / 64f;
    private final float IGNORED_BOX_HEIGHT = Application.WORLD_HEIGHT / 64f;

    public OrthographicCamera orthographicCamera;
    public float x, y;
    private float lookDirectionX, lookDirectionY;
    private Vector2 ignoredBoxOffset = new Vector2();

    private final Player player;
    private Vector2 targetPosition = new Vector2();

    public Camera(Player player) {
        this.player = player;
        orthographicCamera = new OrthographicCamera();
        orthographicCamera.setToOrtho(false, Application.WORLD_WIDTH, Application.WORLD_HEIGHT);
        orthographicCamera.zoom = 1;

        setPositionDirectlyToPlayerPosition();
    }

    public void update() {
        lookDirectionX += (GameInput.getAttackingVector().x * LEAD_MULTIPLIER - lookDirectionX) * CAMERA_LEAD_SPEED;
        lookDirectionY += (GameInput.getAttackingVector().y * LEAD_MULTIPLIER - lookDirectionY) * CAMERA_LEAD_SPEED;

        targetPosition = new Vector2(player.getCenteredPosition().x - x, player.getCenteredPosition().y - y);

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

    private void updateOrthographicCamera() {
        orthographicCamera.position.set(new Vector3(MathUtils.ceil((x + lookDirectionX) / VIEWPORT_ZOOM) * VIEWPORT_ZOOM, MathUtils.ceil((y+ lookDirectionY) / VIEWPORT_ZOOM) * VIEWPORT_ZOOM, 0));
        orthographicCamera.update();
    }

    public void setPositionDirectlyToPlayerPosition() {
        x = player.getCenteredPosition().x;
        y = player.getCenteredPosition().y;
        updateOrthographicCamera();
    }

    public Vector2Int playerCenteredPositionInScreenCoordinates() {
        return new Vector2Int((int)(player.getCenteredPosition().x - x) + Gdx.graphics.getWidth() / 2, (int)(player.getCenteredPosition().y - y) + Gdx.graphics.getHeight() / 2);
    }
}