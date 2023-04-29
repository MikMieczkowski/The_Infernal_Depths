package com.mikm.entities.animation;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.Vector2Int;

import java.util.ArrayList;
import java.util.HashMap;

public class ActionAnimationAllDirections {
    public float frameDuration;
    public Animation.PlayMode playMode;
    private ArrayList<Animation<TextureRegion>> animations;

    private ActionAnimationAllDirections() {

    }

    public ActionAnimationAllDirections(float frameDuration, Animation.PlayMode playMode, ActionSpritesheetsAllDirections actionSpritesheet) {
        this.frameDuration = frameDuration;
        this.playMode = playMode;
        this.animations = createAnimationsFromActionSpritesheet(actionSpritesheet);
    }

    public Animation<TextureRegion> getAnimation(int index) {
        return animations.get(index);
    }

    ArrayList<Animation<TextureRegion>> createAnimationsFromActionSpritesheet(ActionSpritesheetsAllDirections actionSpritesheet) {
        ArrayList<Animation<TextureRegion>> animations = new ArrayList<>();
        for (int i = 0; i < Directions.TOTAL; i++) {
            animations.add(new Animation<>(frameDuration, actionSpritesheet.getSpritesheetByDirection(i)));
            animations.get(i).setPlayMode(playMode);
        }
        return animations;
    }

    public HashMap<Vector2Int, Integer> getDirectionToAnimationIndexMap() {
        if (animations.size() == Directions.TOTAL) {
            return Vector2Int.EIGHT_DIRECTIONAL_MAPPINGS;
        }
        return Vector2Int.TWO_DIRECTIONAL_MAPPINGS;
    }
}
