package com.mikm.entities.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.entities.Entity;

import java.util.ArrayList;

public abstract class AnimationSet {
    public ArrayList<Animation<TextureRegion>> animations = new ArrayList<>();
    private float animationTime;
    private boolean animationIsFlipped = false;
    Animation<TextureRegion> currentAnimation;

    Entity entity;
    private final float frameDuration;
    private final Animation.PlayMode playMode;

    public AnimationSet(Entity entity, float frameDuration, Animation.PlayMode playMode) {
        this.entity = entity;
        this.frameDuration = frameDuration;
        this.playMode = playMode;
    }

    void checkIfFlipped() {
        if (entity.direction.x >= 0) {
            animationIsFlipped = false;
        }
        if (entity.direction.x < 0) {
            animationIsFlipped = true;
        }
    }

    public void draw(Batch batch) {
        animationTime += Gdx.graphics.getDeltaTime();
        if (currentAnimation != null) {
            if (animationIsFlipped) {
                batch.draw(currentAnimation.getKeyFrame(animationTime), entity.x + entity.getFullBounds().width, entity.y+entity.height, -entity.getFullBounds().width, entity.getFullBounds().height);
            } else {
                batch.draw(currentAnimation.getKeyFrame(animationTime), entity.x, entity.y+entity.height, entity.getFullBounds().width, entity.getFullBounds().height);
            }
        }
    }

    public void resetTimer() {
        animationTime = 0;
    }

    public void createAnimationsFromSpritesheetRange(int numberOfAnimations, int startingAnimationIndex) {
        for (int i = 0; i < numberOfAnimations; i++) {
            int animationIndex = i + startingAnimationIndex;
            animations.add(new Animation<>(frameDuration, entity.spritesheets.get(animationIndex)));
            animations.get(i).setPlayMode(playMode);
        }
    }

    public void createAnimationsFromSpritesheetRangeFirstImages(int numberOfAnimations, int startingAnimationIndex) {
        for (int i = 0; i < numberOfAnimations; i++) {
            int animationIndex = i + startingAnimationIndex;
            TextureRegion firstImage = entity.spritesheets.get(animationIndex)[0];
            animations.add(new Animation<>(frameDuration, firstImage));
            animations.get(i).setPlayMode(playMode);
        }
    }

    public abstract void setCurrentAnimation();
}
