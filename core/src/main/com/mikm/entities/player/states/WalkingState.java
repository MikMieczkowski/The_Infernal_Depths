package com.mikm.entities.player.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.mikm.entities.player.ANIMATIONS;
import com.mikm.entities.player.InputAxis;
import com.mikm.entities.player.Player;


public class WalkingState extends State{
    public WalkingState(Player player) {
        super(player);
    }

    @Override
    void createAnimations() {
        for (int i = 0; i < 8; i++) {
            int indexOfAnimation = i + ANIMATIONS.Character_WalkDown.ordinal();
            animations.add(new Animation<>(.33f, player.spritesheets.get(indexOfAnimation)));
            animations.get(i).setPlayMode(Animation.PlayMode.LOOP);
        }
    }


    @Override
    public void update() {
        super.update();
        player.xVel = InputAxis.getHorizontalAxis() * player.speed * InputAxis.movementVectorNormalizationMultiplier();
        player.yVel = InputAxis.getVerticalAxis() * player.speed * InputAxis.movementVectorNormalizationMultiplier();
    }

    @Override
    public void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            player.divingState.enter();
        }
        if (!InputAxis.isMoving()) {
            player.standingState.enter();
        }
    }
}
