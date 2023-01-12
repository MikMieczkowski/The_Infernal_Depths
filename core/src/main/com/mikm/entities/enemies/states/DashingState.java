package com.mikm.entities.enemies.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.MathUtils;
import com.mikm.entities.Entity;
import com.mikm.entities.State;
import com.mikm.entities.animation.ActionAnimationAllDirections;
import com.mikm.entities.animation.AnimationManager;
import com.mikm.entities.player.Player;
import com.mikm.rendering.screens.Application;

public class DashingState extends State {
    private float dashTimer;
    private final float MAX_DASH_TIME = 3f;
    private final float DASH_SPEED = 10f;
    private final float DASH_DAMAGE = 1;
    public static final float TIME_BETWEEN_DASHES = 2f;

    private final Player player;
    private float angleToPlayer;

    public DashingState(Entity entity) {
        super(entity);
        ActionAnimationAllDirections actionAnimationAllDirections = new ActionAnimationAllDirections(.33f, Animation.PlayMode.LOOP, entity.entityActionSpritesheets.walking);
        animationManager = new AnimationManager(entity, actionAnimationAllDirections);
        this.player = Application.player;
    }

    @Override
    public void enter() {
        super.enter();
        angleToPlayer = MathUtils.atan2(player.getCenteredPosition().y - entity.y, player.getCenteredPosition().x - entity.x);
    }

    @Override
    public void update() {
        super.update();
        dashTimer += Gdx.graphics.getDeltaTime();
        entity.xVel = MathUtils.cos(angleToPlayer) * DASH_SPEED;
        entity.yVel = MathUtils.sin(angleToPlayer) * DASH_SPEED;
    }

    @Override
    public void checkForStateTransition() {
        if (dashTimer > MAX_DASH_TIME) {
            entity.standingState.enter();
        }
        StandingState.checkIfDamagedPlayer(entity, DASH_DAMAGE, 0);
    }
}
