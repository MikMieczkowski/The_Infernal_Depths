package com.mikm.entities.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.Vector2Int;
import com.mikm.entities.Entity;
import com.mikm.rendering.BatchUtils;

import java.util.HashMap;
import java.util.Map;

public class AnimationManager {
    private float animationTime;
    private boolean animationIsFlipped = false;
    Animation<TextureRegion> currentAnimation;

    Entity entity;
    private final DirectionalAnimationSet directionalAnimationSet;

    public AnimationManager(Entity entity, DirectionalAnimationSet directionalAnimationSet) {
        this.entity = entity;
        this.directionalAnimationSet = directionalAnimationSet;
    }

    void checkIfFlipped() {
        animationIsFlipped = entity.direction.x < 0;
    }

    public void draw(Batch batch) {
        animationTime += Gdx.graphics.getDeltaTime();
        if (animationIsFlipped) {
            BatchUtils.drawFlipped(batch, currentAnimation.getKeyFrame(animationTime), entity.x, entity.y+entity.height, entity.getFullBounds().width, entity.getFullBounds().height);
        } else {
            batch.draw(currentAnimation.getKeyFrame(animationTime), entity.x, entity.y+entity.height, entity.getFullBounds().width, entity.getFullBounds().height);
        }
    }

    public void resetTimer() {
        animationTime = 0;
    }

    public void setCurrentAnimation() {
        checkIfFlipped();
        HashMap<Vector2Int, Integer> directionToAnimationIndexMap = directionalAnimationSet.getDirectionToAnimationIndexMap();
        for (Map.Entry<Vector2Int, Integer> mapping : directionToAnimationIndexMap.entrySet()) {
            Vector2Int direction = mapping.getKey();
            Integer animationIndex = mapping.getValue();
            if (entity.direction.equals(direction)) {
                currentAnimation = directionalAnimationSet.getAnimation(animationIndex);
            }
        }
    }
}
