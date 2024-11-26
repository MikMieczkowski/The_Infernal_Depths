package com.mikm.entities.player.states;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mikm.DeltaTime;
import com.mikm.RandomUtils;
import com.mikm.entities.State;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.particles.ParticleEffect;
import com.mikm.entities.particles.ParticleTypes;
import com.mikm.entities.player.Player;
import com.mikm.entities.projectiles.DamageInformation;
import com.mikm.entities.projectiles.Hurtbox;
import com.mikm.input.GameInput;
import com.mikm.rendering.SoundEffects;

public class PlayerRollingState extends State {
    private final Player player;
    private Vector2 rollForce = new Vector2();
    private float rollSpeedSinCounter, heightSinCounter;
    private boolean jumpDone = false;

    private Hurtbox hurtbox;
    private final int DAMAGE = 1;
    private final int KNOCKBACK_MULTIPLIER = 2;


    public PlayerRollingState(Player player) {
        super(player);
        this.player = player;
        hurtbox = new Hurtbox(20, false);
    }

    @Override
    public void enter() {
        super.enter();
        player.xVel = 0;
        player.yVel = 0;
        heightSinCounter = 0;
        jumpDone = false;
        rollSpeedSinCounter = player.ROLL_STARTING_SIN_COUNT;
    }

    @Override
    public void update() {
        super.update();
        setRollForce();
        setJumpHeight();
        player.xVel = rollForce.x;
        player.yVel = rollForce.y;
    }

    @Override
    public void checkForStateTransition() {

    }

    private void setRollForce() {
        if (rollSpeedSinCounter < MathUtils.PI - player.ROLL_ENDING_TIME) {
            rollSpeedSinCounter += (player.ROLL_FRICTION - (player.ROLL_FRICTION_SPEED * player.ROLL_FRICTION * rollSpeedSinCounter)) * DeltaTime.deltaTime();
        } else {
            player.height = 0;
            player.walkingState.enter();
            return;
        }
        if (rollSpeedSinCounter >= MathUtils.PI) {
            rollSpeedSinCounter = MathUtils.PI;
        }

        rollForce = new Vector2(player.ROLL_SPEED * MathUtils.sin(rollSpeedSinCounter) * GameInput.getHorizontalAxis(),
                player.ROLL_SPEED * MathUtils.sin(rollSpeedSinCounter) * GameInput.getVerticalAxis());
    }

    private void setJumpHeight() {
        if (!jumpDone) {
            if (heightSinCounter < MathUtils.PI) {
                heightSinCounter += player.ROLL_JUMP_SPEED * DeltaTime.deltaTime();
            }
            if (heightSinCounter >= MathUtils.PI) {
                heightSinCounter = 0;
                hurtbox.setPosition(player.getCenteredPosition().x, player.getCenteredPosition().y, 0, 0);
                hurtbox.setDamageInformation(new DamageInformation(RandomUtils.getFloat(0, MathUtils.PI2), KNOCKBACK_MULTIPLIER, DAMAGE));
                hurtbox.checkIfHitEntities(true);
                new ParticleEffect(ParticleTypes.getDiveDustParameters(), player.getCenteredPosition().x, player.getBounds().y - 3);
                SoundEffects.playLoud(SoundEffects.step);
                player.startSquish(0.01f, 1.2f);
                jumpDone = true;
            }
            player.height = player.ROLL_JUMP_HEIGHT * MathUtils.sin(heightSinCounter);
        }
    }

    @Override
    protected AnimationName getAnimationName() {
        return AnimationName.PLAYER_ROLL;
    }
}
