package com.mikm.entities.enemies.states;

import com.badlogic.gdx.math.MathUtils;
import com.mikm.entities.State;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.enemies.Slime;
import com.mikm.entities.player.Player;
import com.mikm.rendering.screens.Application;

public class DashingState extends State {
    private final float MAX_DASH_TIME = .3f;
    private final float DASH_SPEED = 6f;
    private final float DASH_DAMAGE = 1;
    public static final float TIME_BETWEEN_DASHES = 2f;

    private final Player player;
    private float angleToPlayer;
    private boolean slimeBossMinion;
    private Slime slime;

    public DashingState(Slime slime) {
        super(slime);
        this.player = Application.player;
        this.slime = slime;
    }

    @Override
    public void enter() {
        super.enter();
        angleToPlayer = MathUtils.atan2(player.getCenteredPosition().y - slime.y, player.getCenteredPosition().x - slime.x);
    }

    public void enter(float angle) {
        super.enter();
        angleToPlayer = angle;
    }

    @Override
    public void update() {
        super.update();
        slime.xVel = MathUtils.cos(angleToPlayer) * DASH_SPEED;
        slime.yVel = MathUtils.sin(angleToPlayer) * DASH_SPEED;
    }

    @Override
    protected AnimationName getAnimationName() {
        return AnimationName.ENTITY_WALK;
    }

    @Override
    public void checkForStateTransition() {
        if (timeElapsedInState > MAX_DASH_TIME) {
            slime.standingState.enter();
        }
        handlePlayerCollision(DASH_DAMAGE, true);
    }
}
