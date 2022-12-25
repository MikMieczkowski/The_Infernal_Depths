package com.mikm.entities.player.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.mikm.entities.player.InputAxis;
import com.mikm.entities.player.Player;

public class WalkingState extends State{
    public WalkingState(Player player) {
        super(player);
    }

    @Override
    public void update() {
        player.xVel = InputAxis.getHorizontalAxis() * player.speed * InputAxis.movementVectorNormalizationMultiplier();
        player.yVel = InputAxis.getVerticalAxis() * player.speed * InputAxis.movementVectorNormalizationMultiplier();
    }

    @Override
    public void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            player.divingState.enter();
        }
    }
}
