package com.mikm.entities.player.states;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.Vector2Int;
import com.mikm.entities.player.Player;

import java.util.ArrayList;

public abstract class State {
    public ArrayList<Animation<TextureRegion>> animations = new ArrayList<>();
    boolean shouldFlip = true;

    public Animation<TextureRegion> currentAnimation;
    public float animationTime;
    public boolean animationIsFlipped = false;

    Player player;

    public State(Player player) {
        this.player = player;
        createAnimations();
    }

    public void checkIfFlipped() {
        if (player.direction.x >= 0) {
            animationIsFlipped = false;
        }
        if (player.direction.x < 0) {
            animationIsFlipped = true;
        }
    }

    public void enter() {
        player.currentState = this;

    }

    public void update() {
        if (shouldFlip) {
            checkIfFlipped();
        }
        setCurrentAnimation();
    }

    abstract void createAnimations();

    public void setCurrentAnimation() {
        for (Vector2Int direction : Vector2Int.DIRECTIONS) {
            if (player.direction.equals(direction)) {
                System.out.println(direction.animationIndex);
                currentAnimation = animations.get(direction.animationIndex);
            }
        }
    }

    public abstract void handleInput();
}
