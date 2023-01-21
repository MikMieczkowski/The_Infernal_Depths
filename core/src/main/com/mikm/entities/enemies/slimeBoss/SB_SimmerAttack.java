package com.mikm.entities.enemies.slimeBoss;

import com.badlogic.gdx.math.MathUtils;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.State;
import com.mikm.entities.player.Player;
import com.mikm.entities.projectiles.StaticProjectile;
import com.mikm.rendering.screens.Application;

public class SB_SimmerAttack extends State {
    private SlimeBoss slimeBoss;
    private Player player;

    private float angle;
    private final float TIME_SPENT_SIMMERING = 7f;
    private final float SIMMER_MOVE_SPEED_MAX = 5f;
    private final float SIMMER_MOVE_SPEED_MIN = .2f;
    private float startingDistance;
    private float distanceTraveledSinceLastProjectile;

    public static final float SLIME_TRAIL_DAMAGE = 1, SLIME_TRAIL_KNOCKBACK = 1;

    public SB_SimmerAttack(SlimeBoss slimeBoss) {
        super(slimeBoss);
        this.slimeBoss = slimeBoss;
        this.player = Application.player;
        animationManager = slimeBoss.jumpState.animationManager;
    }

    @Override
    public void enter() {
        super.enter();
        startingDistance = ExtraMathUtils.distance(slimeBoss.x, slimeBoss.y, player.x, player.y);
        angle = MathUtils.atan2(player.y - slimeBoss.y, player.x - slimeBoss.x);
    }

    @Override
    public void update() {
        super.update();

        float angleToPlayer = MathUtils.atan2(player.y - slimeBoss.y, player.x - slimeBoss.x);
        angle = ExtraMathUtils.lerpAngle(.5f, TIME_SPENT_SIMMERING, angle, angleToPlayer);
        float moveSpeed = ExtraMathUtils.lerp(timeElapsedInState, TIME_SPENT_SIMMERING, .3f, 1, SIMMER_MOVE_SPEED_MAX, SIMMER_MOVE_SPEED_MIN);
        slimeBoss.xVel = MathUtils.cos(angle) * moveSpeed;
        slimeBoss.yVel =  MathUtils.sin(angle) * moveSpeed;
        //slimeBoss.yScale = ExtraMathUtils.lerp(timeElapsedInState, TIME_SPENT_SIMMERING, 1, .5f);


        distanceTraveledSinceLastProjectile += moveSpeed;
        if (distanceTraveledSinceLastProjectile > 20) {
            distanceTraveledSinceLastProjectile -= 20;
            Application.currentScreen.addInanimateEntity(new StaticProjectile(null, false, slimeBoss.getCenteredPosition().x, slimeBoss.getCenteredPosition().y));
        }
        checkIfCollidedWithPlayer(1, false);
    }

    @Override
    public void checkForStateTransition() {
        if (timeElapsedInState > TIME_SPENT_SIMMERING) {
            slimeBoss.stateManager.updateState();
        }
    }
}
