package com.mikm.entities.actions;

import com.badlogic.gdx.math.MathUtils;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.inanimateEntities.particles.ParticleEffect;
import com.mikm.entities.inanimateEntities.particles.ParticleTypes;
import com.mikm.entities.inanimateEntities.projectiles.DamageInformation;
import com.mikm.entities.inanimateEntities.projectiles.StaticProjectile;
import com.mikm.entities.old.slimeBoss.SB_SimmerAttack;
import com.mikm.entities.old.slimeBoss.SlimeBoss;
import com.mikm.entities.player.Player;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.sound.SoundEffects;

public class SimmerAction {

    private SlimeBoss slimeBoss;
    private Player player;

    private final float TIME_SPENT_SIMMERING = 4f;
    private final float SIMMER_MOVE_SPEED_MAX = .2f;
    private final float SIMMER_MOVE_SPEED_MIN = 1.7f;
    public static final int SLIME_TRAIL_DAMAGE = 1, SLIME_TRAIL_KNOCKBACK = 1;
    private String SLIME_TRAIL_SOUND_EFFECT;

    private float distanceTraveledSinceLastProjectile;
    private float angle;


    public SB_SimmerAttack(SlimeBoss slimeBoss) {
        super(slimeBoss);
        this.slimeBoss = slimeBoss;
        this.player = Application.player;
    }

    @Override
    public void enter() {
        super.enter();
        angle = MathUtils.atan2(player.y - slimeBoss.y, player.x - slimeBoss.x);
    }

    @Override
    public void update() {
        super.update();

        float moveSpeed = ExtraMathUtils.lerp(timeElapsedInState, TIME_SPENT_SIMMERING, .3f, 1, SIMMER_MOVE_SPEED_MAX, SIMMER_MOVE_SPEED_MIN);
        moveTowardsPlayer(moveSpeed);
        handleSlimeTrail(moveSpeed);
        handlePlayerCollision(1, false);
    }

    private void moveTowardsPlayer(float moveSpeed) {
        float angleToPlayer = MathUtils.atan2(player.y - slimeBoss.y, player.x - slimeBoss.x);
        angle = ExtraMathUtils.lerpAngle(.5f, TIME_SPENT_SIMMERING, angle, angleToPlayer);
        slimeBoss.xVel = MathUtils.cos(angle) * moveSpeed;
        slimeBoss.yVel =  MathUtils.sin(angle) * moveSpeed;
    }

    private void handleSlimeTrail(float moveSpeed) {
        distanceTraveledSinceLastProjectile += moveSpeed;
        if (distanceTraveledSinceLastProjectile > 20) {
            SoundEffects.play(SLIME_TRAIL_SOUND_EFFECT);
            distanceTraveledSinceLastProjectile -= 20;
            Application.getInstance().currentScreen.addInanimateEntity(new StaticProjectile(null, false, new DamageInformation(0, SB_SimmerAttack.SLIME_TRAIL_KNOCKBACK, SB_SimmerAttack.SLIME_TRAIL_DAMAGE), slimeBoss.getCenteredPosition().x, slimeBoss.getCenteredPosition().y));
            new ParticleEffect(ParticleTypes.getSlimeTrailParameters(), slimeBoss.getHitbox().x, slimeBoss.getHitbox().y);
        }
    }
}
