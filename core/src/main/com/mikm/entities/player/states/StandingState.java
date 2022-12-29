package com.mikm.entities.player.states;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.entities.player.ANIMATIONS;
import com.mikm.entities.player.InputAxis;
import com.mikm.entities.player.Player;

public class StandingState extends State{
    public StandingState(Player player) {
        super(player);
    }

    @Override
    public void enter() {
        player.currentState = this;
    }

    @Override
    void createAnimations() {
        for (int i = 0; i < 8; i++) {
            int indexOfAnimation = i + ANIMATIONS.Character_WalkDown.ordinal();
            TextureRegion firstImageInAnimation = player.spritesheets.get(indexOfAnimation)[0];
            animations.add(new Animation<>(2f, firstImageInAnimation));
            animations.get(i).setPlayMode(Animation.PlayMode.LOOP);
        }
    }

    @Override
    public void handleInput() {
        if (InputAxis.isMoving()) {
            player.walkingState.enter();
        }
    }
}
