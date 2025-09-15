package com.mikm.entities.enemies.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.mikm.RandomUtils;
import com.mikm.entities.Entity;
import com.mikm.rendering.screens.Application;

public abstract class DashInducingState extends State {
    public float detectionCircleRadius;
    private int contactDamage;
    public float timeBetweenDashes;
    private float timeBetweenDashesCenter;
    public float timeSinceLastDash = timeBetweenDashes;


    public DashInducingState(Entity entity, int contactDamage, float detectionCircleRadius, float timeBetweenDashesCenter) {
        super(entity);
        this.contactDamage = contactDamage;
        this.detectionCircleRadius = detectionCircleRadius;
        this.timeBetweenDashesCenter = timeBetweenDashesCenter;
    }

    @Override
    public void enter() {
        super.enter();
        timeBetweenDashes = timeBetweenDashesCenter + RandomUtils.getFloat(-.5f, .5f) * timeBetweenDashesCenter;
    }

    public void enter(float dashTimer) {
        super.enter();
        this.timeSinceLastDash = dashTimer;
        timeBetweenDashes = timeBetweenDashesCenter + RandomUtils.getFloat(-.5f, .5f) * timeBetweenDashesCenter;
    }

    @Override
    public void update() {
        super.update();
        timeSinceLastDash += Gdx.graphics.getDeltaTime();
    }

    boolean isPlayerInDetectionCircle() {
        return Intersector.overlaps(new Circle(entity.x, entity.y, detectionCircleRadius), Application.player.getHitbox());
    }

    @Override
    public void checkForStateTransition() {
        if (timeSinceLastDash > timeBetweenDashes && isPlayerInDetectionCircle()) {
            timeSinceLastDash = 0;
            entity.detectedPlayerBuildUpState.enter();
        }
        handlePlayerCollision(contactDamage, true);
    }
}
