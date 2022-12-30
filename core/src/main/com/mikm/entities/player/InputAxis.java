package com.mikm.entities.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.mikm.Vector2Utils;
import com.mikm.rendering.screens.Application;

public class InputAxis {
    private final static float deadzone = .2f;

    public static int getHorizontalAxisInt() {
        if (Application.usingController) {
            float controllerXAxis = Application.controller.getAxis(0);
            if (controllerXAxis > deadzone) {
                return 1;
            }
            if (controllerXAxis < -deadzone) {
                return -1;
            }
            return 0;
        } else {
            return keyboardHorizontalAxisInt();
        }
    }

    public static float getHorizontalAxis() {
        Vector2 normalizedMovementVector = Vector2Utils.normalizeAndScale(movementVector());
        return normalizedMovementVector.x;
    }

    private static int keyboardHorizontalAxisInt() {
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            return 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            return -1;
        }
        return 0;
    }

    public static int getVerticalAxisInt() {
        if (Application.usingController) {
            float controllerYAxis = Application.controller.getAxis(1);
            if (controllerYAxis > deadzone) {
                return -1;
            }
            if (controllerYAxis < -deadzone) {
                return 1;
            }
            return 0;
        } else {
            return keyboardVerticalAxisInt();
        }
    }

    public static float getVerticalAxis() {
        Vector2 normalizedMovementVector = Vector2Utils.normalizeAndScale(movementVector());
        return normalizedMovementVector.y;
    }

    private static int keyboardVerticalAxisInt() {
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            return 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            return -1;
        }
        return 0;
    }

    private static Vector2 movementVector() {
        Vector2 vector;
        if (Application.usingController) {
            vector = new Vector2(controllerXAxis(), controllerYAxis());
        } else {
            vector = new Vector2(keyboardHorizontalAxisInt(), keyboardVerticalAxisInt());
        }
        return vector;
    }

    private static float controllerXAxis() {
        float controllerXAxis = Application.controller.getAxis(0);
        if (Math.abs(controllerXAxis) > deadzone) {
            return controllerXAxis;
        }
        return 0;
    }

    private static float controllerYAxis() {
        float controllerYAxis = Application.controller.getAxis(1);
        if (Math.abs(controllerYAxis) > deadzone) {
            return -controllerYAxis;
        }
        return 0;
    }

    public static boolean isMoving() {
        if (Application.usingController) {
            float controllerXAxis = Application.controller.getAxis(0);
            float controllerYAxis = Application.controller.getAxis(1);
            return Math.abs(controllerXAxis) > .2f || (Math.abs(controllerYAxis) > .2f) || getHorizontalAxis() != 0 || getVerticalAxis() != 0;
        } else {
            return getHorizontalAxis() != 0 || getVerticalAxis() != 0;
        }
    }

    public static boolean isDiveButtonPressed() {
        return Gdx.input.isKeyJustPressed(Input.Keys.E) || Application.xPressed;
    }
}
