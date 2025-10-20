package com.mikm.entities.animation;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.Assets;
import com.mikm.rendering.screens.Application;

import java.util.ArrayList;

public class SingleAnimation extends SuperAnimation {
    public SingleAnimation() {

    }
    public SingleAnimation(String spritesheetName, float frameDuration, Animation.PlayMode playMode) {
        create(spritesheetName, Application.TILE_WIDTH, Application.TILE_HEIGHT, frameDuration, playMode, 0);
    }

    public SingleAnimation(String spritesheetName, int width, int height, float fps, Animation.PlayMode playMode) {
        create(spritesheetName, width, height, fps, playMode,0);
    }

    public SingleAnimation(String spritesheetName, int width, int height, float fps, Animation.PlayMode playMode, int frames) {
        create(spritesheetName, width, height, fps, playMode, frames);
    }

    public SingleAnimation(TextureRegion[] t, float fps, Animation.PlayMode playMode) {
        animations = new ArrayList<>();
        create(t, fps, playMode);
    }


    private void create(String spritesheetName, int width, int height, float fps, Animation.PlayMode playMode, int frames) {
        this.frameDuration = 1/fps;
        animations = new ArrayList<>();
        TextureRegion[][] t = Assets.getInstance().getSplitTextureRegion(spritesheetName, width, height);
        if (frames == 0) {
            //Read how many frames are in this spritesheet
            frames = t.length * t[0].length;
        }

        TextureRegion[] flattened = new TextureRegion[frames];
        for (int i = 0; i < t.length; i++) {
            for (int j = 0; j < t[0].length; j++) {
                if (i * t[0].length + j > frames - 1) {
                    continue;
                }
                flattened[i * t[0].length + j] = t[i][j];
            }
        }
        create(flattened, fps, playMode);
    }

    private void create(TextureRegion[] t, float fps, Animation.PlayMode playMode) {
        Animation<TextureRegion> animation = new Animation<>(1/fps, t);
        animation.setPlayMode(playMode);
        animations.add(animation);
        animations.add(animation);
        animations.add(animation);
        animations.add(animation);
        animations.add(animation);
        currentAnimation = animation;
    }
}
