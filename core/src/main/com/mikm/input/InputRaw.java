package com.mikm.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.Controllers;
import com.mikm.rendering.Camera;

public class InputRaw {
    static boolean usingController = false;
    public static ControllerMapping controllerMapping;

    private static Controller controller;
    private final static float deadzone = .2f;

    private static boolean xPressedLastFrame = false, r2PressedLastFrame = false;
    private static boolean isXJustPressed = false;
    private static boolean isR2JustPressed = false;
    private static boolean controllerHasInput = true;

    public static void checkForControllers() {
        if (Controllers.getControllers().size == 0) {
            useController(false);
            return;
        }
        useController(controllerHasInput);
        setControllerHasInput();
    }

    private static void useController(boolean connected) {
        if (connected) {
            usingController = true;
            controller = Controllers.getControllers().first();
            controllerMapping = controller.getMapping();
        } else {
            usingController = false;
        }
    }

    private static void setControllerHasInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.ANY_KEY) || Gdx.input.isTouched()) {
            controllerHasInput = false;
        }
        if (!controllerHasInput && isAnyControllerInputMoved()) {
            controllerHasInput = true;
        }
    }

    public static void handleLastFrameInput() {
        if (usingController) {
            if (!xPressedLastFrame && controller.getButton(controllerMapping.buttonA)) {
                isXJustPressed = true;
            }
            if (!r2PressedLastFrame && isR2Pressed()) {
                isR2JustPressed = true;
            }
        }
    }

    public static void handleThisFrameInput() {
        if (usingController) {
            isXJustPressed = false;
            xPressedLastFrame = controller.getButton(controllerMapping.buttonA);
            isR2JustPressed = false;
            r2PressedLastFrame = isR2Pressed();
        }
    }

    static boolean isKeyPressed(int keyCode) {
        return Gdx.input.isKeyPressed(keyCode);
    }

    static boolean isKeyJustPressed(int keyCode) {
        return Gdx.input.isKeyJustPressed(keyCode);
    }

    static boolean isControllerButtonPressed(int buttonCode) {
        if (buttonCode == controllerMapping.buttonR2) {
            return isR2Pressed();
        }
        return controller.getButton(buttonCode);
    }

    static boolean isControllerButtonJustPressed(int buttonCode) {
        if (buttonCode == controllerMapping.buttonA) {
            return isXJustPressed;
        }
        if (buttonCode == controllerMapping.buttonR2) {
            return isR2JustPressed;
        }
        throw new RuntimeException("unimplemented button release");
    }

    public static float mouseXPosition() {
        return Gdx.input.getX() * Camera.VIEWPORT_ZOOM;
    }

    public static float mouseYPosition() {
        return (Gdx.graphics.getHeight() - Gdx.input.getY())* Camera.VIEWPORT_ZOOM;
    }

    private static boolean isAnyControllerInputMoved() {
        for (int keyCode = 0; keyCode < controller.getMaxButtonIndex(); keyCode++) {
            if (controller.getButton(keyCode)) {
                return true;
            }
        }
        for (int axis = 0; axis < controller.getAxisCount(); axis++) {
            if (Math.abs(controller.getAxis(axis)) > deadzone) {
                return true;
            }
        }
        return false;
    }

    static float controllerXAxis() {
        float controllerXAxis = controller.getAxis(controllerMapping.axisLeftX);
        if (Math.abs(controllerXAxis) > deadzone) {
            return controllerXAxis;
        }
        return 0;
    }

    static int controllerXAxisInt() {
        float controllerXAxis = controller.getAxis(controllerMapping.axisLeftX);
        if (controllerXAxis > deadzone) {
            return 1;
        }
        if (controllerXAxis < -deadzone) {
            return -1;
        }
        return 0;
    }


    static float controllerYAxis() {
        float controllerYAxis = controller.getAxis(controllerMapping.axisLeftY);
        if (Math.abs(controllerYAxis) > deadzone) {
            return -controllerYAxis;
        }
        return 0;
    }

    static int controllerYAxisInt() {
        float controllerYAxis = controller.getAxis(controllerMapping.axisLeftY);
        if (controllerYAxis > deadzone) {
            return -1;
        }
        if (controllerYAxis < -deadzone) {
            return 1;
        }
        return 0;
    }

    static float controllerXAxisRight() {
        float controllerXAxis = controller.getAxis(controllerMapping.axisRightX);
        if (Math.abs(controllerXAxis) > deadzone) {
            return controllerXAxis;
        }
        return 0;
    }

    static float controllerYAxisRight() {
        float controllerYAxis = controller.getAxis(controllerMapping.axisRightY);
        if (Math.abs(controllerYAxis) > deadzone) {
            return -controllerYAxis;
        }
        return 0;
    }

    private static boolean isR2Pressed() {
        final int AXIS_R2 = 5;
        if (controller.getAxisCount() >= 6) {
            return controller.getAxis(AXIS_R2) > 0;
        }
        return controller.getButton(controllerMapping.buttonR2);
    }
}
