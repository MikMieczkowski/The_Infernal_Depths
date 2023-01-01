package com.mikm.entities.player.states;

import com.mikm.entities.player.Player;
import com.mikm.entities.states.State;

public class PlayerAttackingState extends State {
    Player player;
    public PlayerAttackingState(Player player) {
        super(player);
        this.player = player;
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void handleInput() {

    }
}
