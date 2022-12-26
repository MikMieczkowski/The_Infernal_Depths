package com.mikm.entities.player.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.entities.player.InputAxis;
import com.mikm.entities.player.Player;

public class WalkingState extends State{
    public WalkingState(Player player) {
        super(player);
    }


    @Override
    void createAnimation() {
        TextureRegion[] walkingAnimationFrames = new TextureRegion[2];
        walkingAnimationFrames[0] = player.spritesheet[3][0];
        walkingAnimationFrames[1] = player.spritesheet[3][1];
        animation = new Animation<>(.33f, walkingAnimationFrames);
        animation.setPlayMode(Animation.PlayMode.LOOP);
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
        if (!InputAxis.isMoving()) {
            player.standingState.enter();
        }
    }
}
