package com.mikm.entities.enemies.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.mikm.entities.Entity;
import com.mikm.entities.State;
import com.mikm.rendering.screens.Application;

public abstract class DashInducingState extends State {
    public static final float DETECTION_CIRCLE_RADIUS = 100f;
    private float contactDamage;
    public float timeSinceLastDash = DashingState.TIME_BETWEEN_DASHES;


    public DashInducingState(Entity entity, float contactDamage) {
        super(entity);
        this.contactDamage = contactDamage;
    }

    @Override
    public void enter() {
        super.enter();
    }

    public void enter(float dashTimer) {
        super.enter();
        this.timeSinceLastDash = dashTimer;
    }

    @Override
    public void update() {
        super.update();
        timeSinceLastDash += Gdx.graphics.getDeltaTime();
    }

    boolean isPlayerInDetectionCircle() {
        return Intersector.overlaps(new Circle(entity.x, entity.y, DETECTION_CIRCLE_RADIUS), Application.player.getHitbox());
    }

    @Override
    public void checkForStateTransition() {
        if (timeSinceLastDash > DashingState.TIME_BETWEEN_DASHES && isPlayerInDetectionCircle()) {
            timeSinceLastDash = 0;
            entity.detectedPlayerBuildUpState.enter();
        }
        handlePlayerCollision(contactDamage, true);
    }
}
