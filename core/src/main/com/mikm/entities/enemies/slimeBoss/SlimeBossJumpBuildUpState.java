package com.mikm.entities.enemies.slimeBoss;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.mikm.entities.State;
import com.mikm.entities.animation.OneDirectionalAnimationManager;
import com.mikm.entities.player.Player;
import com.mikm.rendering.screens.Application;

public class SlimeBossJumpBuildUpState extends State {
    private SlimeBoss slimeBoss;
    private Player player;
    private float buildUpTimer;
    private final float MAX_BUILDUP_TIME = 1;

    public SlimeBossJumpBuildUpState(SlimeBoss slimeBoss) {
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
        buildUpTimer = 0;
        slimeBoss.xVel = 0;
        slimeBoss.yVel = 0;
        slimeBoss.startSquish(0, 1.5f, MAX_BUILDUP_TIME, false);
    }

    @Override
    public void update() {
        buildUpTimer+= Gdx.graphics.getDeltaTime();

    }

    @Override
    public void checkForStateTransition() {
        if (buildUpTimer > MAX_BUILDUP_TIME) {
            slimeBoss.jumpState.enter();
        }
    }
}
