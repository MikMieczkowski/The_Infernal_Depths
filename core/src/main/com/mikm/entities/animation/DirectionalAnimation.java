package com.mikm.entities.animation;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.utils.Assets;

import java.util.ArrayList;

//turns Libgdx animations into a playable set of animations representing an action like Walk, (any direction)
public class DirectionalAnimation extends SuperAnimation {
    public DirectionalAnimation() {

    }
    //These Look for startswith + Down, Up, Left, Right
    public DirectionalAnimation(String startsWith, float fps, Animation.PlayMode playMode) {
        this.frameDuration = 1/fps;
        this.playMode = playMode;
        ArrayList<TextureRegion[]> spriteSheetsRaw = Assets.getInstance().findImagesStartingWith(startsWith, 32, 32);
        animations = textureRegionsToAnimations(spriteSheetsRaw);
        currentAnimation = animations.get(0);
    }

    public DirectionalAnimation(String startsWith, int imageWidth, int imageHeight, float fps, Animation.PlayMode playMode) {
        this.frameDuration = 1/fps;
        this.playMode = playMode;
        ArrayList<TextureRegion[]> spriteSheetsRaw = Assets.getInstance().findImagesStartingWith(startsWith, imageWidth, imageHeight);
        animations = textureRegionsToAnimations(spriteSheetsRaw);
        currentAnimation = animations.get(0);
    }

    public DirectionalAnimation createDirectionalAnimationFromFirstFrames() {
        DirectionalAnimation output = new DirectionalAnimation();
        output.frameDuration = frameDuration;
        output.playMode = playMode;
        output.animations = createAnimationsFromFirstFramesOf(this);
        output.currentAnimation = animations.get(0);
        return output;
    }
}
