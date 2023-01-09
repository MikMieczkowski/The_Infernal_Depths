package com.mikm.entities.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.mikm.entities.DamageInformation;
import com.mikm.entities.Entity;
import com.mikm.entities.State;
import com.mikm.entities.animation.AnimationManager;
import com.mikm.entities.animation.ActionAnimationAllDirections;

public class DamagedState extends State {
    private DamageInformation damageInformation;
    public DamagedState(Entity entity) {
        super(entity);
        ActionAnimationAllDirections actionAnimationAllDirections = new ActionAnimationAllDirections(1, Animation.PlayMode.NORMAL, entity.entityActionSpritesheets.hit);
        animationManager = new AnimationManager(entity, actionAnimationAllDirections);
    }

    @Override
    public void enter() {
        throw new RuntimeException("please provide parameters for entering damagedstate");
    }

    public void enter(DamageInformation damageInformation) {
        this.damageInformation = damageInformation;
    }

    @Override
    public void checkForStateTransition() {

    }
}
