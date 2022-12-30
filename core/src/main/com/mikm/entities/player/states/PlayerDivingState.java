package com.mikm.entities.player.states;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mikm.Vector2Int;
import com.mikm.Vector2Utils;
import com.mikm.entities.animation.EightDirectionalAnimationSet;
import com.mikm.entities.player.PlayerAnimationNames;
import com.mikm.entities.player.InputAxis;
import com.mikm.entities.player.Player;
import com.mikm.entities.states.State;

public class PlayerDivingState extends State<Player> {
    private final Player player;
    private Vector2 diveForce = new Vector2();
    private Vector2Int diveDirection = new Vector2Int();
    private float sinCounter;

    public PlayerDivingState(Player player) {
        super(player);
        this.player = player;
        animationSet = new EightDirectionalAnimationSet(player, .1f, Animation.PlayMode.NORMAL);
        animationSet.createAnimationsFromSpritesheetRange(5, PlayerAnimationNames.DIVE_DOWN.ordinal());
    }

    @Override
    public void enter() {
        super.enter();
        player.xVel = 0;
        player.yVel = 0;
        animationSet.resetTimer();
        sinCounter = player.diveStartingSinCount;

        diveForce = new Vector2(player.diveSpeed * MathUtils.sin(sinCounter) * InputAxis.getHorizontalAxis(),
                player.diveSpeed * MathUtils.sin(sinCounter) * InputAxis.getVerticalAxis());
        diveDirection = new Vector2Int(player.direction.x, player.direction.y);
        super.update();
    }

    @Override
    public void update() {
        player.xVel = diveForce.x;
        player.yVel = diveForce.y;
        setDiveForce();
    }

    @Override
    public void handleInput() {
        if (InputAxis.isDiveButtonPressed() && sinCounter > MathUtils.PI - player.diveEndTimeFrame) {
            player.rollingState.enter();
        }
    }

    private void setDiveForce() {
        if (sinCounter < MathUtils.PI) {
            sinCounter += player.diveFriction - (player.diveFrictionSpeed * player.diveFriction * sinCounter);
        } else {
            player.rollingState.enter();
            return;
        }
        if (sinCounter >= MathUtils.PI) {
            sinCounter = MathUtils.PI;
        }

        Vector2 normalizedDiveDirection = Vector2Utils.normalizeAndScale(diveDirection);
        diveForce = new Vector2(player.diveSpeed * MathUtils.sin(sinCounter) * normalizedDiveDirection.x,
                player.diveSpeed * MathUtils.sin(sinCounter) * normalizedDiveDirection.y);
    }
}
