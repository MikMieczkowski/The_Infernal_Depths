package com.mikm.entities.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.mikm.Vector2Int;
import com.mikm.entities.inanimateEntities.InanimateEntity;
import com.mikm.rendering.screens.Application;

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

  public void draw() {
    animationTime += Gdx.graphics.getDeltaTime();
    Application.batch.draw(currentAnimation.getKeyFrame(animationTime), entity.x, entity.y + entity.height, 0,
        0, entity.getFullBounds().width, entity.getFullBounds().height, entity.xScale, entity.yScale, entity.rotation);
  }

  public void changeAnimation(SuperAnimation animation) {
    currentAnimation = animation;
    animationTime = 0;
    currentAnimation.update(entity.direction);
  }
}
