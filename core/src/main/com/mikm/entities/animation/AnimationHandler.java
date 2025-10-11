package com.mikm.entities.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.mikm.Vector2Int;
import com.mikm.entities.inanimateEntities.InanimateEntity;
import sun.text.SupplementaryCharacterData;

public class AnimationHandler {
    private InanimateEntity entity;
    private SuperAnimation currentAnimation;
    protected float animationTime;

    public AnimationHandler(InanimateEntity entity) {
        this.entity = entity;
    }

    public void update() {
        currentAnimation.update(entity.direction);
    }

    public void draw(Batch batch) {
        animationTime += Gdx.graphics.getDeltaTime();
        batch.draw(currentAnimation.getKeyFrame(animationTime), entity.x, entity.y+ entity.height, 0,
                0, entity.getFullBounds().width, entity.getFullBounds().height, entity.xScale, entity.yScale, 0);
    }

    public void changeAnimation(SuperAnimation animation) {
        currentAnimation = animation;
        animationTime = 0;
        currentAnimation.update(entity.direction);
    }
}
