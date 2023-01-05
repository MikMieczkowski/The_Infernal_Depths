package com.mikm.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.mikm.ExtraMathUtils;
import com.mikm.Vector2Int;
import com.mikm.entities.player.Player;

public class Camera {
    private final float ZOOM = .25f;
    private final float CAMERA_SPEED = .2f;

    public OrthographicCamera orthographicCamera;
    public float x, y;

    private final Player player;

    public Camera(Player player) {
        this.player = player;
        orthographicCamera = new OrthographicCamera();
        orthographicCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        orthographicCamera.zoom = ZOOM;
        setPositionDirectlyToPlayerPosition();
    }

    public void update() {
        Vector2 targetPosition = new Vector2((player.x - x) + Player.PLAYER_WIDTH_PIXELS / 2f, (player.y - y) + Player.PLAYER_HEIGHT_PIXELS / 2f);
        x += MathUtils.round(targetPosition.x * CAMERA_SPEED/ZOOM)*ZOOM;
        y += MathUtils.round(targetPosition.y * CAMERA_SPEED/ZOOM)*ZOOM;
        orthographicCamera.position.set(new Vector3(x, y, 0));
        orthographicCamera.update();
    }

    public void setPositionDirectlyToPlayerPosition() {
        x = player.x + Player.PLAYER_WIDTH_PIXELS / 2f;
        y = player.y + Player.PLAYER_HEIGHT_PIXELS / 2f;
    }

    public Vector2Int playerCenteredPositionInScreenCoordinates() {
        return new Vector2Int((int)(player.getCenteredPosition().x - x) + Gdx.graphics.getWidth() / 2, (int)(player.getCenteredPosition().y - y) + Gdx.graphics.getHeight() / 2);
    }
}