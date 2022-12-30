package com.mikm.entities.player.states;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.mikm.entities.animation.EightDirectionalAnimationSet;
import com.mikm.entities.player.InputAxis;
import com.mikm.entities.player.Player;
import com.mikm.entities.player.PlayerAnimationNames;
import com.mikm.entities.states.State;

public class PlayerStandingState extends State<Player> {
    private final Player player;
    public PlayerStandingState(Player player) {
        super(player);
        this.player = player;
        animationSet = new EightDirectionalAnimationSet(player, 2f, Animation.PlayMode.LOOP);
        animationSet.createAnimationsFromSpritesheetRangeFirstImages(5, PlayerAnimationNames.WALK_DOWN.ordinal());
    }

    @Override
    public void enter() {
        super.enter();
        //Prevents frame of being in wrong state
        handleInput();
    }

    @Override
    public void handleInput() {
        if (InputAxis.isMoving()) {
            player.walkingState.enter();
        }
    }
}
