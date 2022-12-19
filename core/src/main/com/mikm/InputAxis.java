package com.mikm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class InputAxis {
    public static int getHorizontalAxis() {
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            return 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            return -1;
        }
        return 0;
    }

    public static int getVerticalAxis() {
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            return 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            return -1;
        }
        return 0;
    }

    public static float movementVectorNormalizationMultiplier() {
        final float oneOverSqrtTwo = 1 / (float)Math.sqrt(2);
        if (getHorizontalAxis() != 0 && getVerticalAxis() != 0) {
            return oneOverSqrtTwo;
        } else {
            return 1;
        }
    }
}
