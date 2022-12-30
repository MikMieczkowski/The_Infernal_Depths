package com.mikm.entities.animation;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.mikm.Vector2Int;
import com.mikm.entities.Entity;

public class EightDirectionalAnimationSet extends AnimationSet {
    public EightDirectionalAnimationSet(Entity entity, float frameDuration, Animation.PlayMode playMode) {
        super(entity, frameDuration, playMode);
    }

    public void setCurrentAnimation() {
        checkIfFlipped();
        for (Vector2Int direction : Vector2Int.DIRECTIONS) {
            if (entity.direction.equals(direction)) {
                currentAnimation = animations.get(direction.animationIndex);
            }
        }
    }
}
