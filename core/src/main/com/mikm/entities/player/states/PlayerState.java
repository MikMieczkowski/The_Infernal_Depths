package com.mikm.entities.player.states;

import com.mikm.entities.player.Player;
import com.mikm.entities.State;

public class PlayerState extends State {
    final Player player;
    public PlayerState(Player player) {
        super(player);
        this.player = player;
    }

    @Override
    public void checkForStateTransition() {
        //handle damaging
    }
}
