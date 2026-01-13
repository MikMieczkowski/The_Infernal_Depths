package com.mikm.entities.animation;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.Vector2Int;
import com.mikm._components.CopyReference;
import com.mikm._components.Copyable;

import java.util.ArrayList;

//turns Libgdx animations into a playable set of animations representing an action like Walk, (any direction)
public abstract class SuperAnimation {
    protected float frameDuration;
    protected Animation.PlayMode playMode;

    protected ArrayList<Animation<TextureRegion>> animations;
    protected Animation<TextureRegion> currentAnimation;

    public SuperAnimation copy() {
        SuperAnimation empty;
        try {
            empty = this.getClass().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        empty.frameDuration = this.frameDuration;
        empty.playMode = this.playMode;
        empty.animations = this.animations;
        return empty;
    }

    public boolean isFinished(float animationTime) {
        return currentAnimation.isAnimationFinished(animationTime);
    }

    public void update(Vector2Int entityDirection) {
        if (animations.size() == 5) {
            currentAnimation = animations.get(Directions.getAnimationIndexOfDirection(entityDirection));
        } else if (animations.size() != 1) {
            throw new RuntimeException("Wrong amount of animations, had " + animations.size());
        }
    }

    public TextureRegion getKeyFrame(float animationTime) {
        return currentAnimation.getKeyFrame(animationTime);
    }

    protected ArrayList<Animation<TextureRegion>> textureRegionsToAnimations(ArrayList<TextureRegion[]> textureRegions) {
        ArrayList<Animation<TextureRegion>> output = new ArrayList<>();
        for (TextureRegion[] textureRegion : textureRegions) {
            Animation<TextureRegion> animation =  new Animation<>(frameDuration, textureRegion);
            animation.setPlayMode(playMode);
            output.add(animation);
        }
        return output;
    }

    protected ArrayList<Animation<TextureRegion>> createAnimationsFromFirstFramesOf(SuperAnimation superAnimation) {
        ArrayList<Animation<TextureRegion>> output = new ArrayList<>();
        for (Animation<TextureRegion> animation : superAnimation.animations) {
            Animation<TextureRegion> toAdd = new Animation<>(superAnimation.frameDuration, animation.getKeyFrames()[0]);
            toAdd.setPlayMode(superAnimation.playMode);
            output.add(toAdd);
        }
        return output;
    }

    public float getFrameDuration() {
        return frameDuration;
    }
}
