package com.mikm.entities.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Vector2;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.Entity;
import com.mikm.entities.State;
import com.mikm.entities.animation.AnimationManager;
import com.mikm.entities.animation.DirectionalAnimationSet;

public class WanderingState extends State {
    private float timeSpentWandering;
    private final float maxWanderTime = 1f;
    private Vector2 wanderForce;
    private final float minWanderForce = .2f;

    public WanderingState(Entity entity) {
        super(entity);
        DirectionalAnimationSet directionalAnimationSet = new DirectionalAnimationSet(.33f, Animation.PlayMode.LOOP, entity.spritesheets, 1, 1);
        animationManager = new AnimationManager(entity, directionalAnimationSet);
    }

    @Override
    public void enter() {
        super.enter();
        wanderForce = new Vector2(getRandomWanderFloat(), getRandomWanderFloat());
    }

    @Override
    public void update() {
        super.update();
        timeSpentWandering += Gdx.graphics.getDeltaTime();
        entity.xVel = wanderForce.x;
        entity.yVel = wanderForce.y;
    }

    @Override
    public void checkForStateTransition() {
        if (timeSpentWandering > maxWanderTime) {
            timeSpentWandering = 0;
            entity.standingState.enter();
        }
    }

    private float getRandomWanderFloat() {
        float randomForcePositive = entity.speed * ExtraMathUtils.randomFloat(minWanderForce, 1);
        int randomSign = ExtraMathUtils.randomBoolean() ? 1 : -1;
        return randomSign * randomForcePositive;
    }
}
