package com.mikm.entities.player.states;

import com.mikm.entities.player.Player;

public abstract class State {
    Player player;
    public State(Player player) {
        this.player = player;
    }

    public void enter() {
        player.currentState = this;
    }

    public abstract void update();

    public abstract void handleInput();
}
