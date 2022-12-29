package com.mikm.entities.player.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.mikm.entities.player.ANIMATIONS;
import com.mikm.entities.player.InputAxis;
import com.mikm.entities.player.Player;
import com.mikm.rendering.screens.Application;


public class WalkingState extends State{
    public WalkingState(Player player) {
        super(player);
    }

    @Override
    void createAnimations() {
        for (int i = 0; i < 5; i++) {
            int indexOfAnimation = i + ANIMATIONS.Character_WalkDown.ordinal();
            animations.add(new Animation<>(.33f, player.spritesheets.get(indexOfAnimation)));
            animations.get(i).setPlayMode(Animation.PlayMode.LOOP);
        }
    }


    @Override
    public void update() {
        super.update();
        player.xVel = InputAxis.getHorizontalAxis() * player.speed;
        player.yVel = InputAxis.getVerticalAxis() * player.speed;
    }

    @Override
    public void handleInput() {
        if (InputAxis.isDiveButtonPressed()) {
            player.divingState.enter();
        }
        if (!InputAxis.isMoving()) {
            player.standingState.enter();
        }
    }
}
