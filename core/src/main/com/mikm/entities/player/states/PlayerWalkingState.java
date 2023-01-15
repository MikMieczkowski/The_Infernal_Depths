package com.mikm.entities.player.states;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.mikm.entities.animation.ActionAnimationAllDirections;
import com.mikm.entities.animation.AnimationManager;
import com.mikm.entities.player.Player;
import com.mikm.input.GameInput;


public class PlayerWalkingState extends PlayerWalking {
    private final Player player;
    public ActionAnimationAllDirections actionAnimationAllDirections;

    public PlayerWalkingState(Player player) {
        super(player);
        this.player = player;
        actionAnimationAllDirections = new ActionAnimationAllDirections(.33f, Animation.PlayMode.LOOP, player.entityActionSpritesheets.walking);
        animationManager = new AnimationManager(player, actionAnimationAllDirections);
    }

    @Override
    public void enter() {
        super.enter();
        //Prevents frame of being in wrong state
        checkForStateTransition();
    }


    @Override
    public void update() {
        super.update();
        checkIfWalking();
    }

    @Override
    public void checkForStateTransition() {
        if (GameInput.isDiveButtonJustPressed()) {
            player.divingState.enter();
        }
        if (!GameInput.isMoving()) {
            player.standingState.enter();
        }
        if (GameInput.isAttackButtonPressed()) {
            player.attackingState.enter();
        }
    }
}
