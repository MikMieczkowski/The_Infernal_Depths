package com.mikm.entities.animation;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.Assets;
import com.mikm.Vector2Int;

import java.util.ArrayList;

//turns Libgdx animations into a playable set of animations representing an action like Walk, (any direction)
public class DirectionalAnimation {
    private float frameDuration;
    private Animation.PlayMode playMode;

    private ArrayList<Animation<TextureRegion>> animations;
    private Animation<TextureRegion> currentAnimation;

    //copy constructor
    private DirectionalAnimation(DirectionalAnimation animation) {
        frameDuration = animation.frameDuration;
        playMode = animation.playMode;
        animations = createAnimationsFromFirstFramesOf(animation);
    }

    //Multiple direction animation (default width/height = 32)
    //These Look for startswith + Down, Up, Left, Right
    public DirectionalAnimation(String startsWith, float frameDuration, Animation.PlayMode playMode) {
        this.frameDuration = frameDuration;
        this.playMode = playMode;
        ArrayList<TextureRegion[]> spriteSheetsRaw = Assets.getInstance().findImagesStartingWith(startsWith, 32, 32);
        animations = textureRegionsToAnimations(spriteSheetsRaw);
    }

    //Multiple direction animation with width/height
    public DirectionalAnimation(String startsWith, int imageWidth, int imageHeight, float frameDuration, Animation.PlayMode playMode) {
        this.frameDuration = frameDuration;
        this.playMode = playMode;
        ArrayList<TextureRegion[]> spriteSheetsRaw = Assets.getInstance().findImagesStartingWith(startsWith, imageWidth, imageHeight);
        animations = textureRegionsToAnimations(spriteSheetsRaw);
    }

    //Single direction animation
    public DirectionalAnimation(String spritesheetName, int width, int height, float frameDuration, int frames, Animation.PlayMode playMode) {
        this.frameDuration = frameDuration;
        animations = new ArrayList<>();
        TextureRegion[][] t = Assets.getInstance().getSplitTextureRegion(spritesheetName, width, height);
        TextureRegion[] flattened = new TextureRegion[frames];
        for (int i = 0; i < t.length; i++) {
            for (int j = 0; j < t[0].length; j++) {
                if (i*t[0].length + j > frames-1) {
                    continue;
                }
                flattened[i*t[0].length + j] = t[i][j];
            }
        }
        Animation<TextureRegion> animation = new Animation<>(frameDuration, flattened);
        animation.setPlayMode(playMode);
        animations.add(animation);
        animations.add(animation);
        animations.add(animation);
        animations.add(animation);
        animations.add(animation);
        currentAnimation = animation;
    }

    //Static animation (one image only)
    public DirectionalAnimation(String staticImageName, int width, int height) {
        this.frameDuration = 10;
        this.playMode = Animation.PlayMode.LOOP;
        animations = new ArrayList<>();
        Animation<TextureRegion> animation = new Animation<>(frameDuration, Assets.getInstance().getTextureRegion(staticImageName, width, height));
        animation.setPlayMode(playMode);
        animations.add(animation);
        currentAnimation = animation;
    }
    public boolean isFinished(float animationTime) {
        return currentAnimation.isAnimationFinished(animationTime);
    }


    public DirectionalAnimation createDirectionalAnimationFromFirstFrames() {
        return new DirectionalAnimation(this);
    }

    void update(Vector2Int entityDirection) {
        if (animations.size() == 1) {
            return;
        }
        if (animations.size() == 5) {
            currentAnimation = animations.get(Directions.getAnimationIndexOfDirection(entityDirection));
        } else {
            throw new RuntimeException("Wrong amount of animations, had " + animations.size());
        }
    }

    TextureRegion getKeyFrame(float animationTime) {
        return currentAnimation.getKeyFrame(animationTime);
    }

    private ArrayList<Animation<TextureRegion>> textureRegionsToAnimations(ArrayList<TextureRegion[]> textureRegions) {
        ArrayList<Animation<TextureRegion>> output = new ArrayList<>();
        for (TextureRegion[] textureRegion : textureRegions) {
            Animation<TextureRegion> animation =  new Animation<>(frameDuration, textureRegion);
            animation.setPlayMode(playMode);
            output.add(animation);
        }
        return output;
    }

    private ArrayList<Animation<TextureRegion>> createAnimationsFromFirstFramesOf(DirectionalAnimation directionalAnimation) {
        ArrayList<Animation<TextureRegion>> output = new ArrayList<>();
        for (Animation<TextureRegion> animation : directionalAnimation.animations) {
            Animation<TextureRegion> toAdd = new Animation<>(directionalAnimation.frameDuration, animation.getKeyFrames()[0]);
            toAdd.setPlayMode(directionalAnimation.playMode);
            output.add(toAdd);
        }
        return output;
    }
}
