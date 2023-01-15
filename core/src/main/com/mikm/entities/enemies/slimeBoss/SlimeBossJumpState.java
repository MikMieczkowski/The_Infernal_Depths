package com.mikm.entities.enemies.slimeBoss;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.MathUtils;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.State;
import com.mikm.entities.animation.OneDirectionalAnimationManager;
import com.mikm.entities.player.Player;
import com.mikm.rendering.screens.Application;

public class SlimeBossJumpState extends State {
    private SlimeBoss slimeBoss;
    private Player player;
    private final float TIME_SPENT_JUMPING = 1f;
    private final float JUMP_HEIGHT = 15f;
    private float jumpDistance;
    private float jumpTimer;
    private float angleToPlayer;

    public SlimeBossJumpState(SlimeBoss slimeBoss, float jumpDistance) {
        super(slimeBoss);
        this.jumpDistance = jumpDistance;
        this.slimeBoss = slimeBoss;
        this.player = Application.player;
        OneDirectionalAnimationManager oneDirectionalAnimationManager = new OneDirectionalAnimationManager(entity);
        oneDirectionalAnimationManager.animation = new Animation<>(1, slimeBoss.entityActionSpritesheets.hit);
        animationManager = oneDirectionalAnimationManager;
    }

    @Override
    public void enter() {
        super.enter();
        jumpTimer = 0;
        angleToPlayer = MathUtils.atan2(player.y - slimeBoss.y, player.x - slimeBoss.x);
    }

    @Override
    public void update() {
        jumpTimer+=Gdx.graphics.getDeltaTime();

        float speed = jumpDistance / (TIME_SPENT_JUMPING * 60);
        slimeBoss.xVel = speed * MathUtils.cos(angleToPlayer);
        slimeBoss.yVel = speed * MathUtils.sin(angleToPlayer);

        slimeBoss.height = ExtraMathUtils.sinLerp(jumpTimer,TIME_SPENT_JUMPING, JUMP_HEIGHT);
    }

    @Override
    public void checkForStateTransition() {
        if (jumpTimer > TIME_SPENT_JUMPING) {
            slimeBoss.simmerState.enter();
        }
    }
}
