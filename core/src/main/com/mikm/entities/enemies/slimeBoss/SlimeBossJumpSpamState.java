package com.mikm.entities.enemies.slimeBoss;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.mikm.entities.State;
import com.mikm.entities.animation.OneDirectionalAnimationManager;
import com.mikm.entities.player.Player;
import com.mikm.rendering.screens.Application;

public class SlimeBossJumpSpamState extends State {
    private SlimeBoss slimeBoss;
    private Player player;

    public SlimeBossJumpSpamState(SlimeBoss slimeBoss) {
        super(slimeBoss);
        this.slimeBoss = slimeBoss;
        this.player = Application.player;
        OneDirectionalAnimationManager oneDirectionalAnimationManager = new OneDirectionalAnimationManager(entity);
        oneDirectionalAnimationManager.animation = new Animation<>(1, slimeBoss.entityActionSpritesheets.hit);
        animationManager = oneDirectionalAnimationManager;
    }

    @Override
    public void checkForStateTransition() {

    }
}
