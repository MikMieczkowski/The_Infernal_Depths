package com.mikm.entities.enemies.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.Entity;
import com.mikm.entities.State;
import com.mikm.entities.animation.AnimationManager;
import com.mikm.entities.animation.ActionAnimationAllDirections;
import com.mikm.entities.projectiles.DamageInformation;
import com.mikm.rendering.screens.Application;

public class StandingState extends State {
    private float wanderTimer;
    private float timeBetweenWanders;
    private final float TIME_BETWEEN_WANDERS_MIN = 1f, TIME_BETWEEN_WANDERS_MAX = 4f;
    public static final float DETECTION_CIRCLE_RADIUS = 100f;
    private int contactDamage;
    public static final int CONTACT_KNOCKBACK_FORCE = 2;
    private float dashTimer;

    public StandingState(Entity entity, int contactDamage) {
        super(entity);
        this.contactDamage = contactDamage;
        ActionAnimationAllDirections actionAnimationAllDirections = new ActionAnimationAllDirections(.33f, Animation.PlayMode.LOOP, entity.entityActionSpritesheets.standing);
        animationManager = new AnimationManager(entity, actionAnimationAllDirections);
    }

    @Override
    public void enter() {
        super.enter();
        dashTimer = 0;
        entity.xVel = 0;
        entity.yVel = 0;
        wanderTimer = 0;
        timeBetweenWanders = ExtraMathUtils.randomFloat(TIME_BETWEEN_WANDERS_MIN, TIME_BETWEEN_WANDERS_MAX);
    }

    @Override
    public void update() {
        super.update();
        dashTimer += Gdx.graphics.getDeltaTime();
        wanderTimer += Gdx.graphics.getDeltaTime();
    }

    private static Circle getPlayerDetectionCircle(Entity entity) {
        return new Circle(entity.x, entity.y, DETECTION_CIRCLE_RADIUS);
    }

    @Override
    public void checkForStateTransition() {
        if (wanderTimer > timeBetweenWanders) {
            entity.walkingState.enter();
        }
        checkIfDamagedPlayer(entity, contactDamage, dashTimer);
    }

    public static void checkIfDamagedPlayer(Entity entity, float contactDamage, float dashTimer) {
        if (entity.damagesPlayer) {
            checkIfCollidedWithPlayerDetectionRadius(entity, dashTimer);
            checkIfCollidedWithPlayer(entity, contactDamage);
        }
    }

    private static void checkIfCollidedWithPlayerDetectionRadius(Entity entity, float dashTimer) {
        boolean inDetectionRange = Intersector.overlaps(getPlayerDetectionCircle(entity), Application.player.getHitbox());
        if (dashTimer > DashingState.TIME_BETWEEN_DASHES && inDetectionRange) {
            entity.detectedPlayerState.enter();
        }
    }

    private static void checkIfCollidedWithPlayer(Entity entity, float contactDamage) {
        boolean hitboxesOverlap = Intersector.overlaps(entity.getHitbox(), Application.player.getHitbox());
        if (hitboxesOverlap) {
            float angleToPlayer = MathUtils.atan2(Application.player.getCenteredPosition().y - entity.y, Application.player.getCenteredPosition().x - entity.x);
            entity.standingState.enter();
            Application.player.damagedState.enter(new DamageInformation(angleToPlayer, CONTACT_KNOCKBACK_FORCE, contactDamage));
        }
    }
}
