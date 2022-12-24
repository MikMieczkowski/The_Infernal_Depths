package com.mikm.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.mikm.entities.player.Player;

public class Camera {
    private final float zoom = .5f;
    private final float cameraSpeed = .2f;

    public OrthographicCamera orthographicCamera;
    public float x, y;

    private final Player player;

    public Camera(Player player) {
        this.player = player;
        orthographicCamera = new OrthographicCamera();
        orthographicCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        orthographicCamera.zoom = zoom;
        x = player.x + player.getBounds().width / 2;
        y = player.y + player.getBounds().height / 2;
    }

    public void update() {
        Vector2 targetPosition = new Vector2((player.x - x) + player.getBounds().width / 2, (player.y - y) + player.getBounds().height / 2);
        x += MathUtils.round(targetPosition.x) * cameraSpeed;
        y += MathUtils.round(targetPosition.y) * cameraSpeed;
        orthographicCamera.position.set(new Vector3(x, y, 0));
    }
}