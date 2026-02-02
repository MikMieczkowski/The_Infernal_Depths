package com.mikm.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.math.MathUtils;
import com.mikm.rendering.Camera;

public class InputRaw {
    public static boolean usingController = false;
    public static ControllerMapping controllerMapping;

    private static Controller controller;
    private final static float deadzone = .2f;

    private static int[] usedButtonCodes;
    private static boolean[] buttonCodePressedLastFrame;
    private static boolean[] buttonCodeJustPressed;

    private static boolean hasSetUpController = false;
    private static boolean controllerHasInput = true;



    public static void checkForControllers() {
        if (Controllers.getControllers().size == 0) {
            useController(false);
            return;
        }
        useController(controllerHasInput);
        setControllerHasInput();
        
//        Controllers.addListener(new ControllerAdapter() {
//            @Override
//            public boolean buttonDown(Controller controller, int buttonIndex) {
//                System.out.println("Pressed: " + buttonIndex + " (" + controller.getName() + ")");
//                return true;
//            }
//        });
    
    }

    private static boolean[] buttonCodeJustReleased;

    private static void setUpController() {
        usedButtonCodes = new int[]{controllerMapping.buttonA, controllerMapping.buttonB, controllerMapping.buttonX, controllerMapping.buttonY,
        controllerMapping.buttonR2, controllerMapping.buttonStart, controllerMapping.buttonDpadUp, controllerMapping.buttonDpadDown, controllerMapping.buttonDpadLeft, controllerMapping.buttonDpadRight,
        controllerMapping.buttonL1, controllerMapping.buttonL2, controllerMapping.buttonR1};
        buttonCodePressedLastFrame = new boolean[usedButtonCodes.length];
        buttonCodeJustPressed = new boolean[usedButtonCodes.length];
        buttonCodeJustReleased = new boolean[usedButtonCodes.length];
    }

    private static void useController(boolean connected) {
        if (connected) {
            usingController = true;
            controller = Controllers.getControllers().first();
            controllerMapping = controller.getMapping();
            if (!hasSetUpController) {
                setUpController();
                hasSetUpController = true;
            }
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
            for (int i = 0; i < usedButtonCodes.length; i++) {
                boolean currentlyPressed = isControllerButtonPressed(usedButtonCodes[i]);
                if (!buttonCodePressedLastFrame[i] && currentlyPressed) {
                    buttonCodeJustPressed[i] = true;
                }
                if (buttonCodePressedLastFrame[i] && !currentlyPressed) {
                    buttonCodeJustReleased[i] = true;
                }
            }
        }
    }

    public static void handleThisFrameInput() {
        if (usingController) {
            for (int i = 0; i < usedButtonCodes.length; i++) {
                buttonCodeJustPressed[i] = false;
                buttonCodeJustReleased[i] = false;
                buttonCodePressedLastFrame[i] = isControllerButtonPressed(usedButtonCodes[i]);
            }
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
        for (int i = 0; i < usedButtonCodes.length; i++) {
            if (buttonCode == usedButtonCodes[i]) {
                return buttonCodeJustPressed[i];
            }
        }
        throw new RuntimeException("unimplemented button: " + buttonCode);
    }

    static boolean isControllerButtonJustReleased(int buttonCode) {
        for (int i = 0; i < usedButtonCodes.length; i++) {
            if (buttonCode == usedButtonCodes[i]) {
                return buttonCodeJustReleased[i];
            }
        }
        throw new RuntimeException("unimplemented button: " + buttonCode);
    }

    public static float mouseXPosition() {
        return Gdx.input.getX() * Camera.VIEWPORT_ZOOM;
    }

    public static float mouseYPosition() {
        return (Gdx.graphics.getHeight() - Gdx.input.getY())* Camera.VIEWPORT_ZOOM;
    }

    private static boolean isAnyControllerInputMoved() {
        for (int buttonCode = 0; buttonCode < controller.getMaxButtonIndex(); buttonCode++) {
            if (controller.getButton(buttonCode)) {
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
        controllerXAxis += controller.getButton(controllerMapping.buttonDpadLeft) ? -1 : 0;
        controllerXAxis += controller.getButton(controllerMapping.buttonDpadRight) ? 1 : 0;
        if (Math.abs(controllerXAxis) > deadzone) {
            return MathUtils.clamp(controllerXAxis, -1, 1);
        }
        return 0;
    }

    static int controllerXAxisInt() {
        float controllerXAxis = controller.getAxis(controllerMapping.axisLeftX);
        controllerXAxis += controller.getButton(controllerMapping.buttonDpadLeft) ? -1 : 0;
        controllerXAxis += controller.getButton(controllerMapping.buttonDpadRight) ? 1 : 0;
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
        controllerYAxis += controller.getButton(controllerMapping.buttonDpadDown) ? 1 : 0;
        controllerYAxis += controller.getButton(controllerMapping.buttonDpadUp) ? -1 : 0;
        if (Math.abs(controllerYAxis) > deadzone) {
            return -MathUtils.clamp(controllerYAxis, -1, 1);
        }
        return 0;
    }

    static int controllerYAxisInt() {
        float controllerYAxis = controller.getAxis(controllerMapping.axisLeftY);
        controllerYAxis += controller.getButton(controllerMapping.buttonDpadDown) ? 1 : 0;
        controllerYAxis += controller.getButton(controllerMapping.buttonDpadUp) ? -1 : 0;
        if (controllerYAxis > deadzone) {
            return -1;
        }
        if (controllerYAxis < -deadzone) {
            return 1;
        }
        return 0;
    }

    // Left stick only (no D-pad contribution) for menu navigation logic
    public static int controllerXAxisIntNoDpad() {
        float x = controller.getAxis(controllerMapping.axisLeftX);
        if (x > deadzone) return 1;
        if (x < -deadzone) return -1;
        return 0;
    }

    public static int controllerYAxisIntNoDpad() {
        float y = controller.getAxis(controllerMapping.axisLeftY);
        if (y > deadzone) return -1; // up is negative axis
        if (y < -deadzone) return 1; // down is positive axis
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
