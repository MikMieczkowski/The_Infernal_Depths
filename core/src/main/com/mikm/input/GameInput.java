package com.mikm.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mikm.Assets;
import com.mikm.Vector2Int;
import com.mikm.ExtraMathUtils;
import com.mikm.rendering.Camera;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.BlacksmithScreen;

import static com.mikm.input.InputRaw.controllerMapping;

public class GameInput {

    private static float lastControllerLeftStickAngle;

    public static float getHorizontalAxis() {
        Vector2 normalizedMovementVector = ExtraMathUtils.normalizeAndScale(movementVector());
        return normalizedMovementVector.x;
    }

    public static float getVerticalAxis() {
        Vector2 normalizedMovementVector = ExtraMathUtils.normalizeAndScale(movementVector());
        return normalizedMovementVector.y;
    }

    public static int getHorizontalAxisInt() {
        if (InputRaw.usingController) {
            return InputRaw.controllerXAxisInt();
        }
        return keyboardHorizontalAxisInt();
    }

    public static int getVerticalAxisInt() {
        if (InputRaw.usingController) {
            return InputRaw.controllerYAxisInt();
        }
        return keyboardVerticalAxisInt();
    }

    private static int keyboardHorizontalAxisInt() {
        if (InputRaw.isKeyPressed(Input.Keys.D) || InputRaw.isKeyPressed(Input.Keys.RIGHT)) {
            return 1;
        }
        if (InputRaw.isKeyPressed(Input.Keys.A) || InputRaw.isKeyPressed(Input.Keys.LEFT)) {
            return -1;
        }
        return 0;
    }

    private static int keyboardVerticalAxisInt() {
        if (InputRaw.isKeyPressed(Input.Keys.W) || InputRaw.isKeyPressed(Input.Keys.UP)) {
            return 1;
        }
        if (InputRaw.isKeyPressed(Input.Keys.S) || InputRaw.isKeyPressed(Input.Keys.DOWN)) {
            return -1;
        }
        return 0;
    }

    private static Vector2 movementVector() {
        if (InputRaw.usingController) {
            if (Application.getInstance().currentScreen == Application.getInstance().blacksmithScreen && BlacksmithScreen.showMenu) {
                return Vector2.Zero;
            }
            return new Vector2(InputRaw.controllerXAxis(), InputRaw.controllerYAxis());
        }
        return new Vector2(keyboardHorizontalAxisInt(), keyboardVerticalAxisInt());
    }

    public static boolean isMoving() {
        return getHorizontalAxisInt() != 0 || getVerticalAxisInt() != 0;
    }

    public static boolean isDiveButtonJustPressed() {
        if (InputRaw.usingController) {
            return InputRaw.isControllerButtonJustPressed(controllerMapping.buttonA);
        }
        return Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
    }

    public static boolean isAttackButtonPressed() {
        if (InputRaw.usingController) {
            return InputRaw.isControllerButtonPressed(controllerMapping.buttonR2);
        }
        return Gdx.input.isTouched();
    }

    public static boolean isAttackButtonJustPressed() {
        if (InputRaw.usingController) {
            return InputRaw.isControllerButtonJustPressed(controllerMapping.buttonR2);
        }
        return Gdx.input.isButtonJustPressed(0)||Gdx.input.isButtonJustPressed(1);
    }

    public static boolean isSwitchButtonJustPressed() {
        if (InputRaw.usingController) {
            return InputRaw.isControllerButtonJustPressed(controllerMapping.buttonB);
        }
        return InputRaw.isKeyJustPressed(Input.Keys.Q);
    }

    public static boolean isMenuCancelButtonJustPressed() {
        if (InputRaw.usingController) {
            return InputRaw.isControllerButtonJustPressed(controllerMapping.buttonB);
        }
        return InputRaw.isKeyJustPressed(Input.Keys.ESCAPE);
    }

    public static boolean isDiveButtonPressed() {
        if (Application.getInstance().currentScreen == Application.getInstance().blacksmithScreen && BlacksmithScreen.showMenu) {
            return false;
        }
        if (InputRaw.usingController) {
            return InputRaw.isControllerButtonPressed(controllerMapping.buttonA);
        }
        return Gdx.input.isKeyPressed(Input.Keys.SPACE);
    }

    public static boolean isPickaxeButtonJustPressed() {
        if (!InputRaw.usingController) {
            return InputRaw.isKeyJustPressed(Input.Keys.NUM_1);
        }
        return false;
    }

    public static boolean isWeaponButtonJustPressed() {
        if (!InputRaw.usingController) {
            return InputRaw.isKeyJustPressed(Input.Keys.NUM_2);
        }
        return false;
    }

    public static boolean isTalkButtonPressed() {
        if (InputRaw.usingController) {
            return InputRaw.isControllerButtonPressed(controllerMapping.buttonX);
        }
        return Gdx.input.isKeyPressed(Input.Keys.E);
    }

    public static boolean isTalkButtonJustPressed() {
        if (InputRaw.usingController) {
            return InputRaw.isControllerButtonJustPressed(controllerMapping.buttonX);
        }
        return Gdx.input.isKeyJustPressed(Input.Keys.E);
    }

    public static boolean isPauseButtonJustPressed() {
        if (InputRaw.usingController) {
            return InputRaw.isControllerButtonJustPressed(controllerMapping.buttonStart);
        }
        return Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE);
    }

    

    public static boolean isDpadUpJustPressed() {
        if (InputRaw.usingController) {
            return InputRaw.isControllerButtonJustPressed(controllerMapping.buttonDpadUp);
        }
        return Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W);
    }

    public static boolean isDpadDownJustPressed() {
        if (InputRaw.usingController) {
            return InputRaw.isControllerButtonJustPressed(controllerMapping.buttonDpadDown);
        }
        return Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S);
    }

    public static boolean isDpadLeftJustPressed() {
        if (InputRaw.usingController) {
            return InputRaw.isControllerButtonJustPressed(controllerMapping.buttonDpadLeft);
        }
        return Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A);
    }

    public static boolean isDpadRightJustPressed() {
        if (InputRaw.usingController) {
            return InputRaw.isControllerButtonJustPressed(controllerMapping.buttonDpadRight);
        }
        return Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D);
    }

    private static TextureRegion square = Assets.getInstance().getTextureRegion("SQUARE"),
    E = Assets.getInstance().getTextureRegion("E");
    public static TextureRegion getTalkButtonImage() {
        return InputRaw.usingController ? square : E;
    }

    public static Vector2Int getAttackingDirectionInt() {
        if (InputRaw.usingController) {
            return controllerAxisToDirectionVectorInt();
        }
        return mousePositionToDirectionVectorInt();
    }

    public static Vector2 getAttackingVector() {
        if (InputRaw.usingController) {
            if (isControllerRightStickMoving()) {
                return new Vector2(InputRaw.controllerXAxisRight(), InputRaw.controllerYAxisRight()).scl(.5f);
            }
            return Vector2.Zero;
        }
        final float pixelsPerOneDistance = 400;
        return new Vector2(mousePosRelativeToPlayer().x/pixelsPerOneDistance, mousePosRelativeToPlayer().y/pixelsPerOneDistance);
    }

    public static Vector2 getMousePos() {
        return new Vector2(InputRaw.mouseXPosition(), InputRaw.mouseYPosition());
    }
    public static Vector2 mousePosRelativeToPlayer() {
        return new Vector2(InputRaw.mouseXPosition() + Camera.x - Application.player.getHitbox().x - Gdx.graphics.getWidth()/2f*Camera.VIEWPORT_ZOOM,
                InputRaw.mouseYPosition() + Camera.y - Application.player.getHitbox().y - Gdx.graphics.getHeight()/2f * Camera.VIEWPORT_ZOOM);
    }

    public static float getAttackingAngle() {
        if (InputRaw.usingController) {
            if (isControllerRightStickMoving()) {
                return controllerRightStickAngle();
            } else if (isControllerLeftStickMoving()) {
                float controllerAngle = controllerLeftStickAngle();
                lastControllerLeftStickAngle = controllerAngle;
                return controllerAngle;
            } else {
                return lastControllerLeftStickAngle;
            }
        }
        return mouseAngle();
    }


    private static float mouseAngle() {
        return MathUtils.atan2(mousePosRelativeToPlayer().y, mousePosRelativeToPlayer().x);
    }

    private static boolean isControllerLeftStickMoving() {
        return InputRaw.controllerXAxis() != 0 || InputRaw.controllerYAxis() != 0;
    }

    private static boolean isControllerRightStickMoving() {
        return InputRaw.controllerXAxisRight() != 0 || InputRaw.controllerYAxisRight() != 0;
    }

    private static float controllerLeftStickAngle() {
        return MathUtils.atan2(InputRaw.controllerYAxis(), InputRaw.controllerXAxis());
    }

    private static float controllerRightStickAngle() {
        return MathUtils.atan2(InputRaw.controllerYAxisRight(), InputRaw.controllerXAxisRight());
    }

    private static Vector2Int controllerAxisToDirectionVectorInt() {
        return ExtraMathUtils.angleToVector2Int(getAttackingAngle());
    }

    private static Vector2Int mousePositionToDirectionVectorInt() {
        return ExtraMathUtils.angleToVector2Int(mouseAngle());
    }

    public static boolean isActionPressed(String action) {
        switch (action) {
            case "TALK": return isTalkButtonPressed();
            case "ATTACK": return isAttackButtonPressed();
            case "DIVE": return isDiveButtonPressed();
            default: return false;
        }
    }

    public static String[] inputActions = {"TALK", "ATTACK", "DPAD_UP", "DPAD_DOWN", "DPAD_LEFT", "DPAD_RIGHT", "DIVE", "SWITCH"};
    public static boolean isActionJustPressed(String action) {
        switch (action) {
            case "TALK": return isTalkButtonJustPressed();
            case "ATTACK": return isAttackButtonJustPressed();
            case "DPAD_UP": return isDpadUpJustPressed();
            case "DPAD_DOWN": return isDpadDownJustPressed();
            case "DPAD_LEFT": return isDpadLeftJustPressed();
            case "DPAD_RIGHT": return isDpadRightJustPressed();
            case "DIVE": return isDiveButtonJustPressed();
            case "SWITCH": return isSwitchButtonJustPressed() || isPickaxeButtonJustPressed() || isWeaponButtonJustPressed();
            default: return false;
        }
    }
}
