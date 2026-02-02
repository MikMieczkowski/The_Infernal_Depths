package com.mikm.utils;

import com.badlogic.gdx.Gdx;

public class DeltaTime {
    public static float deltaTimeMultiplier() {
        return Gdx.graphics.getDeltaTime() * 60;
    }
}
