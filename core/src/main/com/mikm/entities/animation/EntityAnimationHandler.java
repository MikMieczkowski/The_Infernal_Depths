package com.mikm.entities.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.entities.Entity;
import com.mikm.rendering.BatchUtils;

public class AnimationHandler {
    private float animationTime;
    private boolean animationIsFlipped = false;
    private SuperAnimation currentAnimation;

    private Entity entity;

    public AnimationHandler(Entity entity) {
        this.entity = entity;
    }

    public void update() {
        checkIfFlipped();
        currentAnimation.update(entity.direction);
    }

    public void draw(Batch batch) {
        animationTime += Gdx.graphics.getDeltaTime();
        if (animationIsFlipped) {
            BatchUtils.drawFlipped(batch, currentAnimation.getKeyFrame(animationTime), entity.x, entity.y+ entity.height,
                    entity.ORIGIN_X, entity.ORIGIN_Y, entity.getFullBounds().width, entity.getFullBounds().height, entity.xScale, entity.yScale, entity.rotation, true);
        } else {
            batch.draw(currentAnimation.getKeyFrame(animationTime), entity.x, entity.y+ entity.height, entity.ORIGIN_X,
                    entity.ORIGIN_Y, entity.getFullBounds().width, entity.getFullBounds().height, entity.xScale, entity.yScale, entity.rotation);
        }
    }

    public void changeAnimation(SuperAnimation animation) {
        currentAnimation = animation;
        animationTime = 0;
        update();
    }

    public float getFPS() {
        return currentAnimation.fps;
    }

    public TextureRegion getCurrentFrame() {
        return currentAnimation.getKeyFrame(animationTime);
    }

    public boolean isFinished() {
        return currentAnimation.isFinished(animationTime);
    }

    private void checkIfFlipped() {
        animationIsFlipped = entity.direction.x < 0;
    }
}
