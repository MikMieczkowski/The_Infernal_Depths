package com.mikm.entities.player.states;

import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.player.Player;
import com.mikm.input.GameInput;
import com.mikm.rendering.SoundEffects;

public class PlayerAttackingAndWalkingState extends PlayerWalking {
    private final Player player;
    private int swingSoundIndex = 0;
    private long lastTimeInState;
    public static boolean ready = false;

    public PlayerAttackingAndWalkingState(Player player) {
        super(player);
        this.player = player;
    }

    @Override
    public void enter() {
        super.enter();
        if (System.currentTimeMillis() - lastTimeInState > 2000) {
            swingSoundIndex = 0;
        }
        lastTimeInState = System.currentTimeMillis();
        if (ready) {
            SoundEffects.play(SoundEffects.swing[swingSoundIndex]);
            ready = false;
        }
        swingSoundIndex++;
        if (swingSoundIndex == SoundEffects.swing.length) {
            swingSoundIndex = 0;
        }
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
