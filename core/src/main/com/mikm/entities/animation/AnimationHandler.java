package com.mikm.entities.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.mikm.entities.Entity;
import com.mikm.rendering.BatchUtils;

public class AnimationHandler {
    private float animationTime;
    private boolean animationIsFlipped = false;
    private DirectionalAnimation currentDirectionalAnimation;

    private Entity entity;

    public AnimationHandler(Entity entity) {
        this.entity = entity;
    }

    public void update() {
        checkIfFlipped();
        currentDirectionalAnimation.update(entity.direction);
    }

    public void draw(Batch batch) {
        animationTime += Gdx.graphics.getDeltaTime();
        if (animationIsFlipped) {
            BatchUtils.drawFlipped(batch, currentDirectionalAnimation.getKeyFrame(animationTime), entity.x, entity.y+ entity.height,
                    entity.getOriginX(), entity.getOriginY(), entity.getFullBounds().width, entity.getFullBounds().height, entity.xScale, entity.yScale, entity.rotation, true);
        } else {
            batch.draw(currentDirectionalAnimation.getKeyFrame(animationTime), entity.x, entity.y+ entity.height, entity.getOriginX(),
                    entity.getOriginY(), entity.getFullBounds().width, entity.getFullBounds().height, entity.xScale, entity.yScale, entity.rotation);
        }
    }

    public void resetTimer() {
        animationTime = 0;
    }

    public void setCurrentDirectionalAnimation(DirectionalAnimation directionalAnimation) {
        currentDirectionalAnimation = directionalAnimation;
    }

    private void checkIfFlipped() {
        animationIsFlipped = entity.direction.x < 0;
    }
}
