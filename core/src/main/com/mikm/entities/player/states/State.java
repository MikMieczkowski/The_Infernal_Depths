package com.mikm.entities.player.states;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.entities.player.Player;

public abstract class State {
    public Animation<TextureRegion> animation;
    public float animationTime;
    public boolean isAnimationFlipped = false;

    Player player;

    public State(Player player) {
        this.player = player;
        createAnimation();
    }

    public void enter() {
        player.currentState = this;
    }

    abstract void createAnimation();

    public abstract void update();

    public abstract void handleInput();
}
