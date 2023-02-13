package com.mikm;

import com.badlogic.gdx.Gdx;

public class DeltaTime {
    public static float deltaTime() {
        return Gdx.graphics.getDeltaTime() * 60;
    }
}
