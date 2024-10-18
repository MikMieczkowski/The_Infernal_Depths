package com.mikm.entities.player.states;

import com.badlogic.gdx.math.Vector2;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.player.Player;
import com.mikm.input.GameInput;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.BlacksmithScreen;


public class PlayerWalkingState extends PlayerWalking {
    private final Player player;

    public PlayerWalkingState(Player player) {
        super(player);
        this.player = player;
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
            if (!(Application.getInstance().currentScreen == Application.getInstance().blacksmithScreen && BlacksmithScreen.showMenu)) {
                player.divingState.enter();
            }
        }
        if (!GameInput.isMoving()) {
            player.standingState.enter();
        }
        if (GameInput.isAttackButtonPressed()) {
            player.attackingState.enter();
        }
    }

    @Override
    protected AnimationName getAnimationName() {
        return AnimationName.WALK;
    }
}
