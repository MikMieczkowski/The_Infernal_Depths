package com.mikm.entities.enemies.slimeBoss;

import com.badlogic.gdx.math.MathUtils;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.enemies.states.State;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.player.Player;
import com.mikm.rendering.screens.Application;

public class SB_JumpAttack extends State {
    private SlimeBoss slimeBoss;
    private Player player;
    private final float JUMP_HEIGHT = 15f;
    private float jumpDistance;
    private float timeSpentJumping;
    private float angleToPlayer;
    float distanceToPlayer = 0;

    public SB_JumpAttack(SlimeBoss slimeBoss) {
        super(slimeBoss);
        this.slimeBoss = slimeBoss;
        this.player = Application.player;
    }

    @Override
    public void enter() {
        throw new RuntimeException("provide parameters");
    }

    public void enter(float jumpDistance, float timeSpentJumping) {
        super.enter();
        this.jumpDistance = jumpDistance;
        this.timeSpentJumping = timeSpentJumping;
        angleToPlayer = MathUtils.atan2(player.y - slimeBoss.y, player.x - slimeBoss.x);
        distanceToPlayer = ExtraMathUtils.distance(slimeBoss.x, slimeBoss.y, player.x, player.y);
    }

    @Override
    public void update() {
        super.update();
        float speed = (distanceToPlayer /100f) * jumpDistance / (timeSpentJumping * 60);
        slimeBoss.xVel = speed * MathUtils.cos(angleToPlayer);
        slimeBoss.yVel = speed * MathUtils.sin(angleToPlayer);

        slimeBoss.height = ExtraMathUtils.sinLerp(timeElapsedInState, timeSpentJumping, JUMP_HEIGHT);
        handlePlayerCollision(1, false);
    }

    @Override
    public void checkForStateTransition() {
        if (timeElapsedInState > timeSpentJumping) {
            slimeBoss.startSquish(0, 1.5f, .2f, true);
            slimeBoss.stateManager.updateState();
        }
    }

    @Override
    protected AnimationName getAnimationName() {
        return AnimationName.STAND;
    }
}
