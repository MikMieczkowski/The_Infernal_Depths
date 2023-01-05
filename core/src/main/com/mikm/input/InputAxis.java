package com.mikm.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mikm.Vector2Int;
import com.mikm.ExtraMathUtils;
import com.mikm.rendering.Camera;

public class InputAxis {

    private static boolean usingController = false;
    private static Controller controller;
    private static boolean xPressedLastFrame = false, r2PressedLastFrame = false;
    private static boolean isXJustPressed = false;
    private static boolean isR2JustPressed = false;

    private static Camera camera;
    private final static float deadzone = .2f;

    private static float controllerXAxis() {
        float controllerXAxis = controller.getAxis(DS4Buttons.AXIS_LEFT_X);
        if (Math.abs(controllerXAxis) > deadzone) {
            return controllerXAxis;
        }
        return 0;
    }

    private static float controllerYAxis() {
        float controllerYAxis = controller.getAxis(DS4Buttons.AXIS_LEFT_Y);
        if (Math.abs(controllerYAxis) > deadzone) {
            return -controllerYAxis;
        }
        return 0;
    }

    private static float controllerXAxisRight() {
        float controllerXAxis = controller.getAxis(DS4Buttons.AXIS_RIGHT_X);
        if (Math.abs(controllerXAxis) > deadzone) {
            return controllerXAxis;
        }
        return 0;
    }

    private static float controllerYAxisRight() {
        float controllerYAxis = controller.getAxis(DS4Buttons.AXIS_RIGHT_Y);
        if (Math.abs(controllerYAxis) > deadzone) {
            return -controllerYAxis;
        }
        return 0;
    }


    public static int getHorizontalAxisInt() {
        if (usingController) {
            float controllerXAxis = controller.getAxis(DS4Buttons.AXIS_LEFT_X);
            if (controllerXAxis > deadzone) {
                return 1;
            }
            if (controllerXAxis < -deadzone) {
                return -1;
            }
        }
        return keyboardHorizontalAxisInt();
    }

    public static float getHorizontalAxis() {
        Vector2 normalizedMovementVector = ExtraMathUtils.normalizeAndScale(movementVector());
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
        if (usingController) {
            float controllerYAxis = controller.getAxis(DS4Buttons.AXIS_LEFT_Y);
            if (controllerYAxis > deadzone) {
                return -1;
            }
            if (controllerYAxis < -deadzone) {
                return 1;
            }
        }
        return keyboardVerticalAxisInt();
    }

    public static float getVerticalAxis() {
        Vector2 normalizedMovementVector = ExtraMathUtils.normalizeAndScale(movementVector());
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
        if (usingController) {
            vector = new Vector2(nonZeroValue(keyboardHorizontalAxisInt(), controllerXAxis()), nonZeroValue(keyboardVerticalAxisInt(), controllerYAxis()));
        } else {
            vector = new Vector2(keyboardHorizontalAxisInt(), keyboardVerticalAxisInt());
        }
        return vector;
    }

    private static float nonZeroValue(float num1, float num2) {
        if (num1 != 0) {
            return num1;
        }
        return num2;
    }

    public static boolean isMoving() {
        return getHorizontalAxisInt() != 0 || getVerticalAxisInt() != 0;
    }

    public static boolean isDiveButtonPressed() {
        return Gdx.input.isKeyJustPressed(Input.Keys.E) || isXJustPressed;
    }

    public static boolean isAttackButtonPressed() {
        if (usingController) {
            return Gdx.input.isTouched() || controller.getAxis(DS4Buttons.AXIS_R2) > 0;
        }
        return Gdx.input.isTouched();
    }

    public static void handleLastFrameInput() {
        if (usingController) {
            if (!xPressedLastFrame && controller.getButton(DS4Buttons.BUTTON_X)) {
                isXJustPressed = true;
            }
            if (!r2PressedLastFrame && controller.getAxis(DS4Buttons.AXIS_R2) > 0) {
                isR2JustPressed = true;
            }
        }
    }

    public static void handleThisFrameInput() {
        if (usingController) {
            isXJustPressed = false;
            xPressedLastFrame = controller.getButton(0);
            isR2JustPressed = false;
            r2PressedLastFrame = controller.getAxis(DS4Buttons.AXIS_R2) > 0;
        }
    }

    public static void checkForControllers() {
        if (Controllers.getControllers().size != 0) {
            usingController = true;
            controller = Controllers.getControllers().first();
        }
    }

    public static void setCamera(Camera camera) {
        InputAxis.camera = camera;
    }

    public static Vector2Int getAttackingDirectionInt() {
        Vector2Int controllerDirection = new Vector2Int();
        if (usingController) {
            controllerDirection = controllerAxisToDirectionVectorInt();
        }
        if (usingController && !controllerDirection.equals(Vector2Int.ZERO)) {
            return controllerDirection;
        } else {
            return mousePositionToDirectionVectorInt();
        }
    }

    public static float getAttackingAngle() {
        if (usingController && (isControllerLeftStickMoving() || isControllerRightStickMoving())) {
            if (isControllerRightStickMoving()) {
                return controllerRightStickAngle();
            } else {
                return controllerLeftStickAngle();
            }
        } else {
            return mouseAngle();
        }
    }

    private static boolean isControllerLeftStickMoving() {
        return controllerXAxis() != 0 || controllerYAxis() != 0;
    }

    private static boolean isControllerRightStickMoving() {
        return controllerXAxisRight() != 0 || controllerYAxisRight() != 0;
    }

    private static float mouseAngle() {
        return MathUtils.atan2(-(Gdx.input.getY() - camera.playerCenteredPositionInScreenCoordinates().y), Gdx.input.getX() - camera.playerCenteredPositionInScreenCoordinates().x);
    }

    private static float controllerLeftStickAngle() {
        return MathUtils.atan2(controllerYAxis(), controllerXAxis());
    }

    private static float controllerRightStickAngle() {
        return MathUtils.atan2(controllerYAxisRight(), controllerXAxisRight());
    }

    private static Vector2Int controllerAxisToDirectionVectorInt() {
        return new Vector2Int(MathUtils.round(controllerXAxis()), MathUtils.round(controllerYAxis()));
    }

    private static Vector2Int mousePositionToDirectionVectorInt() {
        return angleToDirectionVectorInt(mouseAngle());
    }

    private static Vector2Int angleToDirectionVectorInt(float radians) {
        return new Vector2Int(MathUtils.round(MathUtils.cos(radians)), MathUtils.round(MathUtils.sin(radians)));
    }
}
