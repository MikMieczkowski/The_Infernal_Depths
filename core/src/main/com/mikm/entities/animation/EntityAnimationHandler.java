package com.mikm.entities.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.Vector2Int;
import com.mikm.entities.Entity;
import com.mikm.rendering.BatchUtils;
import com.mikm.rendering.screens.Application;

public class EntityAnimationHandler extends AnimationHandler {
    private boolean animationIsFlipped = false;

    private Entity entity;

    public EntityAnimationHandler(Entity entity) {
        super(entity);
        this.entity = entity;
    }

    @Override
    public void update() {
        checkIfFlipped();
        // Pull animation from routine handler each frame; don't cache locally
        SuperAnimation anim = entity.routineHandler.getCurrentAnimation();
        anim.update(entity.direction);
    }

    @Override
    public void draw() {
        SuperAnimation anim = entity.routineHandler.getCurrentAnimation();
        animationTime += Gdx.graphics.getDeltaTime();
        if (animationIsFlipped) {
            BatchUtils.drawFlipped(Application.batch, anim.getKeyFrame(animationTime), entity.x, entity.y+ entity.height,
                    entity.ORIGIN_X, entity.ORIGIN_Y, entity.getFullBounds().width, entity.getFullBounds().height, entity.xScale, entity.yScale, entity.rotation, true);
        } else {
            Application.batch.draw(anim.getKeyFrame(animationTime), entity.x, entity.y+ entity.height, entity.ORIGIN_X,
                    entity.ORIGIN_Y, entity.getFullBounds().width, entity.getFullBounds().height, entity.xScale, entity.yScale, entity.rotation);
        }
    }

    public TextureRegion getCurrentFrame() {
        SuperAnimation anim = entity.routineHandler.getCurrentAnimation();
        return anim.getKeyFrame(animationTime);
    }

    public boolean isFinished() {
        SuperAnimation anim = entity.routineHandler.getCurrentAnimation();
        return anim.isFinished(animationTime);
    }

    private void checkIfFlipped() {
        animationIsFlipped = entity.direction.x < 0;
    }
}
