package com.mikm.entities.animation;

public enum AnimationName {
    PLAYER_STAND(0),
    PLAYER_WALK(2),
    HIT(2),
    PLAYER_DIVE(0),
    PLAYER_ROLL(5),
    PLAYER_SLASH(0),
    ENTITY_STAND(0),
    ENTITY_WALK(0),
    SLIMEBOSS_STAND(0);

    public final int startingIndex;

    AnimationName(int startingIndex) {
        this.startingIndex = startingIndex;
    }
}
