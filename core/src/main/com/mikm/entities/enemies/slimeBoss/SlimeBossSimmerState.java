package com.mikm.entities.enemies.slimeBoss;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.MathUtils;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.State;
import com.mikm.entities.animation.OneDirectionalAnimationManager;
import com.mikm.entities.particles.ParticleParameters;
import com.mikm.entities.particles.ParticleSystem;
import com.mikm.entities.player.Player;
import com.mikm.rendering.screens.Application;

public class SlimeBossSimmerState extends State {
    private SlimeBoss slimeBoss;
    private Player player;

    private float angle;
    private final float TIME_SPENT_SIMMERING = 7f;
    private float timer;
    private final float SIMMER_MOVE_SPEED_MAX = 10f;
    private final float SIMMER_MOVE_SPEED_MIN = .2f;
    private float startingDistance;

    public SlimeBossSimmerState(SlimeBoss slimeBoss) {
        super(slimeBoss);
        this.slimeBoss = slimeBoss;
        this.player = Application.player;
        OneDirectionalAnimationManager oneDirectionalAnimationManager = new OneDirectionalAnimationManager(entity);
        oneDirectionalAnimationManager.animation = new Animation<>(1, slimeBoss.entityActionSpritesheets.hit);
        animationManager = oneDirectionalAnimationManager;
    }

    @Override
    public void enter() {
        super.enter();
        timer = 0;
        startingDistance = ExtraMathUtils.distance(slimeBoss.x, slimeBoss.y, player.x, player.y);
        angle = MathUtils.atan2(player.y - slimeBoss.y, player.x - slimeBoss.x);
    }

    @Override
    public void update() {
        super.update();
        timer += Gdx.graphics.getDeltaTime();
        new ParticleSystem(ParticleParameters.getDiveDustParameters(), slimeBoss.x+ slimeBoss.getBounds().width /2f, slimeBoss.y);

        float angleToPlayer = MathUtils.atan2(player.y - slimeBoss.y, player.x - slimeBoss.x);
        float normalizedPlayerDistance = ExtraMathUtils.distance(slimeBoss.x, slimeBoss.y, player.x, player.y) / startingDistance;
        if (normalizedPlayerDistance > .7f) {
            normalizedPlayerDistance = .7f;
        }
        if (normalizedPlayerDistance < 0.35f) {
            normalizedPlayerDistance = 0.35f;
        }
        angle = ExtraMathUtils.lerpAngle(.5f, TIME_SPENT_SIMMERING, angle, angleToPlayer);
        float moveSpeed = ExtraMathUtils.lerp(timer, TIME_SPENT_SIMMERING, .3f, 1, SIMMER_MOVE_SPEED_MAX, SIMMER_MOVE_SPEED_MIN);
        slimeBoss.xVel = MathUtils.cos(angle) * moveSpeed;
        slimeBoss.yVel =  MathUtils.sin(angle) * moveSpeed;
        slimeBoss.xScale = ExtraMathUtils.lerp(timer, TIME_SPENT_SIMMERING, 1, .2f);
        slimeBoss.yScale = ExtraMathUtils.lerp(timer, TIME_SPENT_SIMMERING, 1, .2f);
    }

    @Override
    public void checkForStateTransition() {
        if (timer > TIME_SPENT_SIMMERING) {
            slimeBoss.jumpBuildUpState.enter();
        }
    }
}
