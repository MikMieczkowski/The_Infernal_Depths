package com.mikm.entities.enemies.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.Entity;
import com.mikm.entities.State;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.enemies.Slime;
import com.mikm.entities.player.Player;
import com.mikm.rendering.SoundEffects;
import com.mikm.rendering.screens.Application;

public class DashingState extends State {
    private final float MAX_DASH_TIME = .3f;
    private  float dashSpeed;
    private final int DASH_DAMAGE = 1;

    private final Player player;
    private float angleToPlayer;
    private boolean slimeBossMinion;
    private Entity entity;
    private float bounceTimer;
    private float startHeight = 0;

    public DashingState(Entity entity, float dashSpeed) {
        super(entity);
        this.dashSpeed = dashSpeed;
        this.player = Application.player;
        this.entity = entity;
    }

    @Override
    public void enter() {
        super.enter();
        startHeight = entity.height;
        bounceTimer = 0;
        entity.xVel = 0;
        entity.yVel = 0;
        angleToPlayer = MathUtils.atan2(player.getCenteredPosition().y - entity.y, player.getCenteredPosition().x - entity.x);
        if (entity.getClass() == Slime.class) {
            SoundEffects.play(SoundEffects.slimeJump);
        }
    }

    public void enter(float angle) {
        super.enter();
        bounceTimer = 0;
        angleToPlayer = angle;
        if (entity.getClass() == Slime.class) {
            SoundEffects.play(SoundEffects.slimeJump);
        } else {
            SoundEffects.play(SoundEffects.dash);
        }
    }

    @Override
    public void update() {
        super.update();
        entity.height = startHeight+ExtraMathUtils.bounceLerp(bounceTimer, MAX_DASH_TIME, 15f, 1, 10.6f);
        bounceTimer += Gdx.graphics.getDeltaTime();
        entity.xVel = MathUtils.cos(angleToPlayer) * dashSpeed* (1-timeElapsedInState/ MAX_DASH_TIME);
        entity.yVel = MathUtils.sin(angleToPlayer) * dashSpeed * (1-timeElapsedInState/ MAX_DASH_TIME);
    }

    @Override
    protected AnimationName getAnimationName() {
        return AnimationName.WALK;
    }

    @Override
    public void checkForStateTransition() {
        boolean changed = false;
        if (timeElapsedInState > MAX_DASH_TIME) {
            entity.standingState.enter();
            changed = true;
        }
        changed = changed || handlePlayerCollision(DASH_DAMAGE, true);
        if (changed) {
            if (entity.getClass() == Slime.class) {
                SoundEffects.playLoud(SoundEffects.slimeLand);
            }
            entity.height = startHeight;
            entity.xVel = 0;
            entity.yVel = 0;
        }
    }
}
