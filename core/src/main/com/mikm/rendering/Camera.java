package com.mikm.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.mikm.utils.DeltaTime;
import com.mikm.input.GameInput;
import com.mikm.rendering.screens.Application;

public class Camera {
    public static float VIEWPORT_ZOOM = .25f;
    public static float DEFAULT_VIEWPORT_ZOOM = VIEWPORT_ZOOM;
    private final float CAMERA_SPEED = .25f;
    private final float LEAD_MULTIPLIER = 60;
    private final float CAMERA_LEAD_SPEED = .3f;
    private final float IGNORED_BOX_WIDTH = 22;
    private final float IGNORED_BOX_HEIGHT = 12;

    public static OrthographicCamera orthographicCamera;
    public static float x, y;
    private static float lookDirectionX, lookDirectionY;
    private final Vector2 ignoredBoxOffset = new Vector2();

    private static Circle target;

    //why is this class not static
    public Camera() {
        if (Application.getInstance().currentScreen == null) {
            //magic numbers that set the townscreen start location. Do not change or else mouse coords get messed up
            target = new Circle(464,464, 1);
        } else {
            target = Application.getInstance().getPlayerHitbox();
        }
        orthographicCamera = new OrthographicCamera();
        orthographicCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        orthographicCamera.zoom = 1;

        setPositionDirectlyToPlayerPosition();
    }


    public void update() {
        target = Application.getInstance().getPlayerHitbox();
        lookDirectionX += (GameInput.getAttackingVector().x * LEAD_MULTIPLIER - lookDirectionX) * CAMERA_LEAD_SPEED;
        lookDirectionY += (GameInput.getAttackingVector().y * LEAD_MULTIPLIER - lookDirectionY) * CAMERA_LEAD_SPEED;

        Vector2 targetPosition = new Vector2(target.x - x, target.y - y);

        setIgnoredBoxOffsetAndMoveCamera(targetPosition);
        updateOrthographicCamera();
    }

    private void setIgnoredBoxOffsetAndMoveCamera(Vector2 targetPosition) {
        final float HALF_WIDTH = IGNORED_BOX_WIDTH/2f;
        final float HALF_HEIGHT = IGNORED_BOX_HEIGHT/2f;
        if (targetPosition.x > HALF_WIDTH) {
            ignoredBoxOffset.x = -HALF_WIDTH;
            moveCamera(targetPosition, true);
        }
        if (targetPosition.x < -HALF_WIDTH) {
            ignoredBoxOffset.x = HALF_WIDTH;
            moveCamera(targetPosition, true);
        }
        if (targetPosition.y > HALF_HEIGHT) {
            ignoredBoxOffset.y = -HALF_HEIGHT;
            moveCamera(targetPosition, false);
        }
        if (targetPosition.y < -HALF_HEIGHT) {
            ignoredBoxOffset.y = HALF_HEIGHT;
            moveCamera(targetPosition, false);
        }
    }

    private void moveCamera(Vector2 targetPosition, boolean xMovement) {
        if (xMovement) {
            float xVel = (targetPosition.x + ignoredBoxOffset.x) * CAMERA_SPEED * DeltaTime.deltaTime();
            if (Math.abs(xVel) < Math.abs(targetPosition.x)) {
                x += xVel;
            } else {
                x = target.x;
            }
        } else {
            float yVel = (targetPosition.y + ignoredBoxOffset.y) * CAMERA_SPEED * DeltaTime.deltaTime();
            if (Math.abs(yVel) < Math.abs(targetPosition.y)) {
                y += yVel;
            } else {
                y = target.y;
            }
        }
    }

    public static void updateOrthographicCamera() {
        orthographicCamera.position.set(new Vector3(MathUtils.round((x + lookDirectionX) / VIEWPORT_ZOOM) * VIEWPORT_ZOOM, MathUtils.round((y+ lookDirectionY) / VIEWPORT_ZOOM) * VIEWPORT_ZOOM, 0));
        //orthographicCamera.position.set(new Vector3(x , y, 0));
        orthographicCamera.update();
    }

    public static void renderLighting(Batch batch) {
//        batch.draw(Application.dark, x - 1000, y-850);
//        batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_SRC_ALPHA); // Blend with background
//        batch.draw(Application.light, Application.player.x-128,Application.player.y-128); // Draw the circle of light
//        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA); // Normal rendering
    }

    public static void setPositionDirectlyToPlayerPosition() {
        x = target.x;
        y = target.y;
        updateOrthographicCamera();
    }

    public static Vector2 getMousePositionWorldCoordinates() {
        Vector2 mousePosRelativeToPlayer = GameInput.mousePosRelativeToPlayer();
        return new Vector2(mousePosRelativeToPlayer.x+x, mousePosRelativeToPlayer.y+y);
    }

    public static Vector2 getCenterOfScreenWorldCoordinates() {
        return new Vector2(Camera.orthographicCamera.position.x, Camera.orthographicCamera.position.y);
    }
}



/*
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mikm.engine.screens.Application;
import com.mikm.engine.screens.CameraAdapter;

public class Camera extends CameraAdapter {
    private final float CAMERA_SPEED = .25f;
    private final float LEAD_MULTIPLIER = 60;
    private final float CAMERA_LEAD_SPEED = .3f;
    private final float IGNORED_BOX_WIDTH = 22;
    private final float IGNORED_BOX_HEIGHT = 12;

    private float x, y;
    private static float lookDirectionX, lookDirectionY;
    private final Vector2 ignoredBoxOffset = new Vector2();

    @Override
    public float getZoomLevel() {
        return .25f;
    }


    @Override
    public void update() {
        lookDirectionX += (GameInput.getAttackingVector().x * LEAD_MULTIPLIER - lookDirectionX) * CAMERA_LEAD_SPEED;
        lookDirectionY += (GameInput.getAttackingVector().y * LEAD_MULTIPLIER - lookDirectionY) * CAMERA_LEAD_SPEED;

        Vector2 targetPosition = new Vector2(Application.player.getCenteredPosition().x - x, Application.player.getCenteredPosition().y - y);

        setIgnoredBoxOffsetAndMoveCamera(targetPosition);
        setPosition(MathUtils.round((x + lookDirectionX) / VIEWPORT_ZOOM) * VIEWPORT_ZOOM, MathUtils.round((y+ lookDirectionY) / VIEWPORT_ZOOM) * VIEWPORT_ZOOM);
        super.update();
    }


    private void setIgnoredBoxOffsetAndMoveCamera(Vector2 targetPosition) {
        final float HALF_WIDTH = IGNORED_BOX_WIDTH/2f;
        final float HALF_HEIGHT = IGNORED_BOX_HEIGHT/2f;
        if (targetPosition.x > HALF_WIDTH) {
            ignoredBoxOffset.x = -HALF_WIDTH;
            moveCamera(targetPosition, true);
        }
        if (targetPosition.x < -HALF_WIDTH) {
            ignoredBoxOffset.x = HALF_WIDTH;
            moveCamera(targetPosition, true);
        }
        if (targetPosition.y > HALF_HEIGHT) {
            ignoredBoxOffset.y = -HALF_HEIGHT;
            moveCamera(targetPosition, false);
        }
        if (targetPosition.y < -HALF_HEIGHT) {
            ignoredBoxOffset.y = HALF_HEIGHT;
            moveCamera(targetPosition, false);
        }
    }

    private void moveCamera(Vector2 targetPosition, boolean xMovement) {
        if (xMovement) {
            float xVel = (targetPosition.x + ignoredBoxOffset.x) * CAMERA_SPEED * DeltaTime.deltaTime();
            if (Math.abs(xVel) < Math.abs(targetPosition.x)) {
                x += xVel;
            } else {
                x = Application.player.getCenteredPosition().x;
            }
        } else {
            float yVel = (targetPosition.y + ignoredBoxOffset.y) * CAMERA_SPEED * DeltaTime.deltaTime();
            if (Math.abs(yVel) < Math.abs(targetPosition.y)) {
                y += yVel;
            } else {
                y = Application.player.getCenteredPosition().y;
            }
        }
    }

    public static void renderLighting(Batch batch) {
//        batch.draw(Application.dark, x - 1000, y-850);
//        batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_SRC_ALPHA); // Blend with background
//        batch.draw(Application.light, Application.player.x-128,Application.player.y-128); // Draw the circle of light
//        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA); // Normal rendering
    }

    public void setPositionDirectlyToPlayerPosition() {
        x = Application.player.getCenteredPosition().x;
        y = Application.player.getCenteredPosition().y;
        updatePosition();
    }

    private void updatePosition() {
        setPosition(x, y);
        super.update();
    }

}

 */