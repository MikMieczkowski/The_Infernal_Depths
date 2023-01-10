package com.mikm.entities.enemies.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Vector2;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.Entity;
import com.mikm.entities.State;
import com.mikm.entities.animation.AnimationManager;
import com.mikm.entities.animation.ActionAnimationAllDirections;

public class WanderingState extends State {
    private float wanderTimer;
    private final float TOTAL_WANDER_TIME = 1f;
    private Vector2 wanderForce;
    private final float MIN_WANDER_FORCE = .2f;

    public WanderingState(Entity entity) {
        super(entity);
        ActionAnimationAllDirections actionAnimationAllDirections = new ActionAnimationAllDirections(.33f, Animation.PlayMode.LOOP, entity.entityActionSpritesheets.walking);
        animationManager = new AnimationManager(entity, actionAnimationAllDirections);
    }

    @Override
    public void enter() {
        super.enter();
        wanderForce = new Vector2(getRandomWanderFloat(), getRandomWanderFloat());
    }

    @Override
    public void update() {
        super.update();
        wanderTimer += Gdx.graphics.getDeltaTime();
        entity.xVel = wanderForce.x;
        entity.yVel = wanderForce.y;
    }

    @Override
    public void checkForStateTransition() {
        if (wanderTimer > TOTAL_WANDER_TIME) {
            wanderTimer = 0;
            entity.standingState.enter();
        }
    }

    private float getRandomWanderFloat() {
        float randomForcePositive = entity.speed * ExtraMathUtils.randomFloat(MIN_WANDER_FORCE, 1);
        int randomSign = ExtraMathUtils.randomBoolean() ? 1 : -1;
        return randomSign * randomForcePositive;
    }
}
