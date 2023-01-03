package com.mikm.entities.player.states;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.mikm.entities.animation.AnimationManager;
import com.mikm.entities.animation.DirectionalAnimationSet;
import com.mikm.input.InputAxis;
import com.mikm.entities.player.Player;
import com.mikm.entities.player.PlayerAnimationNames;
import com.mikm.entities.states.State;


public class PlayerStandingState extends State {
    private final Player player;
    public PlayerStandingState(Player player) {
        super(player);
        this.player = player;
        DirectionalAnimationSet directionalAnimationSet = new DirectionalAnimationSet(2f, Animation.PlayMode.LOOP,
                player.spritesheets, 5, PlayerAnimationNames.WALK_DOWN.ordinal(), true);
        animationManager = new AnimationManager(player, directionalAnimationSet);
    }

    @Override
    public void enter() {
        super.enter();
        player.xVel = 0;
        player.yVel = 0;
        //Prevents frame of being in wrong state
        checkForStateTransition();
    }

    @Override
    public void checkForStateTransition() {
        if (InputAxis.isMoving()) {
            player.walkingState.enter();
        }
        if (InputAxis.isAttackButtonPressed()) {
            player.attackingState.enter();
        }
    }
}
