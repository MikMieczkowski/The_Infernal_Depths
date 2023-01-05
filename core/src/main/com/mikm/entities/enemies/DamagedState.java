package com.mikm.entities.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.mikm.entities.Entity;
import com.mikm.entities.State;
import com.mikm.entities.animation.AnimationManager;
import com.mikm.entities.animation.DirectionalAnimationSet;

public class DamagedState extends State {

    public DamagedState(Entity entity) {
        super(entity);
        DirectionalAnimationSet directionalAnimationSet = new DirectionalAnimationSet(1, Animation.PlayMode.NORMAL, entity.spritesheets, 3, 2);
        animationManager = new AnimationManager(entity, directionalAnimationSet);
    }

    @Override
    public void checkForStateTransition() {

    }
}
