package com.mikm.entities.player.states;

import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.player.Player;
import com.mikm.input.GameInput;

public class PlayerAttackingAndWalkingState extends PlayerWalking {
    private final Player player;

    public PlayerAttackingAndWalkingState(Player player) {
        super(player);
        this.player = player;
    }

    @Override
    public void enter() {
        super.enter();
        player.currentHeldItem.enterAttackState();
        player.direction = GameInput.getAttackingDirectionInt();
        super.update();
    }

    @Override
    public void update() {
        player.currentHeldItem.updateDuringAttackState();
        checkIfWalking();
    }

    @Override
    public void checkForStateTransition() {
        player.currentHeldItem.checkForStateTransition();
    }

    @Override
    protected AnimationName getAnimationName() {
        return AnimationName.WALK;
    }
}
