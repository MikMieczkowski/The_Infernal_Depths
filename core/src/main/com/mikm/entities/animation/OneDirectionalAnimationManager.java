package com.mikm.entities.animation;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.entities.Entity;

public class OneDirectionalAnimationManager extends AnimationManager {
    public Animation<TextureRegion> animation;

    public OneDirectionalAnimationManager(Entity entity) {
        super(entity, null);
    }

    @Override
    public void setCurrentAnimation() {
        currentAnimation = animation;
    }
}
