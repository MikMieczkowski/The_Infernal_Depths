package com.mikm.entities.player.states;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.State;
import com.mikm.entities.animation.OneDirectionalAnimationManager;
import com.mikm.entities.player.Player;
import com.mikm.rendering.screens.Application;

public class PlayerFallingState extends State {
    private Player player;
    private final float FALLING_TIME = 1, ACCELERATION = .9f;

    public PlayerFallingState(Player player) {
        super(player);
        this.player = player;
        OneDirectionalAnimationManager oneDirectionalAnimationManager = new OneDirectionalAnimationManager(entity);
        oneDirectionalAnimationManager.animation = new Animation<>(1, player.entityActionSpritesheets.hit);
        animationManager = oneDirectionalAnimationManager;
    }

    @Override
    public void enter() {
        super.enter();
        player.isAttackable = false;
    }

    @Override
    public void update() {
        super.update();
        float scale = ExtraMathUtils.lerp(timeElapsedInState, FALLING_TIME, 1,0);
        player.xScale = scale;
        player.yScale = scale;
        player.xVel *= ACCELERATION;
        player.yVel *= ACCELERATION;
    }

    @Override
    public void checkForStateTransition() {
        if (timeElapsedInState > FALLING_TIME) {
            Application.caveScreen.increaseFloor();
            player.isAttackable = true;
            player.walkingState.enter();
        }
    }
}
