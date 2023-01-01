package com.mikm.entities.player.states;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.mikm.entities.animation.AnimationManager;
import com.mikm.entities.animation.DirectionalAnimationSet;
import com.mikm.entities.player.InputAxis;
import com.mikm.entities.player.Player;
import com.mikm.entities.player.PlayerAnimationNames;
import com.mikm.entities.states.State;


public class PlayerWalkingState extends State {
    private final Player player;
    public PlayerWalkingState(Player player) {
        super(player);
        this.player = player;
        DirectionalAnimationSet directionalAnimationSet = new DirectionalAnimationSet(.33f, Animation.PlayMode.LOOP, player.spritesheets, 5, PlayerAnimationNames.WALK_DOWN.ordinal());
        animationManager = new AnimationManager(player, directionalAnimationSet);
    }

    @Override
    public void enter() {
        super.enter();
        //Prevents frame of being in wrong state
        handleInput();
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
