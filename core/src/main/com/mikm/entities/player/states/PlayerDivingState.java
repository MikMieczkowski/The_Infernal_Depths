package com.mikm.entities.player.states;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mikm.DeltaTime;
import com.mikm.ExtraMathUtils;
import com.mikm.Vector2Int;
import com.mikm.entities.enemies.states.State;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.player.Player;
import com.mikm.input.GameInput;
import com.mikm.rendering.SoundEffects;

public class PlayerDivingState extends State {
    private final Player player;
    private Vector2 diveForce = new Vector2();
    private Vector2Int diveDirection = new Vector2Int();
    private float sinCounter;

    public PlayerDivingState(Player player) {
        super(player);
        this.player = player;
    }

    @Override
    public void enter() {
        super.enter();
        SoundEffects.play(SoundEffects.dash);
        player.xVel = 0;
        player.yVel = 0;
        sinCounter = player.DIVE_STARTING_SIN_COUNT;

        diveForce = new Vector2(player.DIVE_SPEED * MathUtils.sin(sinCounter) * GameInput.getHorizontalAxis(),
                player.DIVE_SPEED * MathUtils.sin(sinCounter) * GameInput.getVerticalAxis());
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
    public void checkForStateTransition() {
        if (GameInput.isDiveButtonJustPressed() && sinCounter > MathUtils.PI - player.DIVE_END_TIME_FRAME) {
            player.rollingState.enter();
        }
    }

    private void setDiveForce() {
        if (sinCounter < MathUtils.PI) {
            sinCounter += (player.DIVE_FRICTION - (player.DIVE_FRICTION_SPEED * player.DIVE_FRICTION * sinCounter)) * DeltaTime.deltaTime();
        } else {
            player.rollingState.enter();
            return;
        }
        if (sinCounter >= MathUtils.PI) {
            sinCounter = MathUtils.PI;
        }

        Vector2 normalizedDiveDirection = ExtraMathUtils.normalizeAndScale(diveDirection);
        diveForce = new Vector2(player.DIVE_SPEED * MathUtils.sin(sinCounter) * normalizedDiveDirection.x,
                player.DIVE_SPEED * MathUtils.sin(sinCounter) * normalizedDiveDirection.y);
    }

    @Override
    protected AnimationName getAnimationName() {
        return AnimationName.PLAYER_DIVE;
    }
}
