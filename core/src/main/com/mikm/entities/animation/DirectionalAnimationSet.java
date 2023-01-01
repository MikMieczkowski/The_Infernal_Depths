package com.mikm.entities.animation;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.Vector2Int;

import java.util.ArrayList;
import java.util.HashMap;

public class DirectionalAnimationSet {
    public final float frameDuration;
    public final Animation.PlayMode playMode;
    private final ArrayList<Animation<TextureRegion>> animations;

    public DirectionalAnimationSet(float frameDuration, Animation.PlayMode playMode, ArrayList<TextureRegion[]> spritesheets, int numberOfAnimations, int startingAnimationIndex) {
        this.frameDuration = frameDuration;
        this.playMode = playMode;
        this.animations = createAnimationsFromSpritesheetRange(spritesheets, numberOfAnimations, startingAnimationIndex);
    }

    public DirectionalAnimationSet(float frameDuration, Animation.PlayMode playMode, ArrayList<TextureRegion[]> spritesheets, int numberOfAnimations, int startingAnimationIndex, boolean useFirstImages) {
        this.frameDuration = frameDuration;
        this.playMode = playMode;
        this.animations = createAnimationsFromSpritesheetRange(spritesheets, numberOfAnimations, startingAnimationIndex, useFirstImages);
    }

    public Animation<TextureRegion> getAnimation(int index) {
        return animations.get(index);
    }

    public ArrayList<Animation<TextureRegion>> createAnimationsFromSpritesheetRange(ArrayList<TextureRegion[]> spritesheets, int numberOfAnimations, int startingAnimationIndex) {
        ArrayList<Animation<TextureRegion>> animations = new ArrayList<>();
        for (int i = 0; i < numberOfAnimations; i++) {
            int animationIndex = i + startingAnimationIndex;
            animations.add(new Animation<>(frameDuration, spritesheets.get(animationIndex)));
            animations.get(i).setPlayMode(playMode);
        }
        return animations;
    }

    public ArrayList<Animation<TextureRegion>> createAnimationsFromSpritesheetRange(ArrayList<TextureRegion[]> spritesheets, int numberOfAnimations, int startingAnimationIndex, boolean useFirstImages) {
        ArrayList<Animation<TextureRegion>> animations = new ArrayList<>();
        for (int i = 0; i < numberOfAnimations; i++) {
            int animationIndex = i + startingAnimationIndex;
            TextureRegion[] textureRegions;
            if (useFirstImages) {
                textureRegions = new TextureRegion[]{spritesheets.get(animationIndex)[0]};
            } else {
                textureRegions = spritesheets.get(animationIndex);
            }
            animations.add(new Animation<>(frameDuration, textureRegions));
            animations.get(i).setPlayMode(playMode);
        }
        return animations;
    }

    public HashMap<Vector2Int, Integer> getDirectionToAnimationIndexMap() {
        if (animations.size() == 5) {
            return Vector2Int.EIGHT_DIRECTIONAL_MAPPINGS;
        }
        return Vector2Int.FOUR_DIRECTIONAL_MAPPINGS;
    }
}
