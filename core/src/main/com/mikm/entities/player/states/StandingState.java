package com.mikm.entities.player.states;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.entities.player.InputAxis;
import com.mikm.entities.player.Player;

public class StandingState extends State{
    public StandingState(Player player) {
        super(player);
    }

    @Override
    void createAnimation() {
        animation = new Animation<>(2, player.spritesheet[0][0]);
    }

    @Override
    public void update() {

    }

    @Override
    public void handleInput() {
        if (InputAxis.isMoving()) {
            player.walkingState.enter();
        }
    }
}
