package com.mikm.entities.animation;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.utils.Assets;
import com.mikm.rendering.screens.Application;

import java.util.ArrayList;

public class SingleFrame extends SuperAnimation {
    public SingleFrame() {

    }
    public SingleFrame(TextureRegion textureRegion) {
        create(textureRegion);
    }

    public SingleFrame(String imageName) {
        create(Assets.getInstance().getTextureRegion(imageName, Application.TILE_WIDTH, Application.TILE_HEIGHT));
    }
    public SingleFrame(String imageName, int width, int height) {
        create(Assets.getInstance().getTextureRegion(imageName, width, height));
    }

    private void create(TextureRegion textureRegion) {
        this.frameDuration = 10f;
        this.playMode = Animation.PlayMode.LOOP;
        animations = new ArrayList<>();
        Animation<TextureRegion> animation = new Animation<>(frameDuration, textureRegion);
        animation.setPlayMode(playMode);
        animations.add(animation);
        currentAnimation = animation;
    }
}
