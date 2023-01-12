package com.mikm.entities.player.states;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.mikm.entities.State;
import com.mikm.entities.animation.ActionAnimationAllDirections;
import com.mikm.entities.animation.AnimationManager;
import com.mikm.entities.player.Player;
import com.mikm.input.GameInput;


public class PlayerStandingState extends State {
    private final Player player;
    public PlayerStandingState(Player player) {
        super(player);
        this.player = player;
        ActionAnimationAllDirections actionAnimationAllDirections = new ActionAnimationAllDirections(2f, Animation.PlayMode.LOOP,
                player.entityActionSpritesheets.standing);
        animationManager = new AnimationManager(player, actionAnimationAllDirections);
    }

    @Override
    public void enter() {
        super.enter();
        //Prevents frame of being in wrong state
        checkForStateTransition();
    }

    @Override
    public void checkForStateTransition() {
        if (GameInput.isMoving()) {
            player.walkingState.enter();
        }
        if (GameInput.isAttackButtonPressed()) {
            player.attackingState.enter();
        }
    }
}
