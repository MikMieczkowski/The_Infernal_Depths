package com.mikm.entities.enemies.states;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.MathUtils;
import com.mikm.entities.State;
import com.mikm.entities.animation.ActionAnimationAllDirections;
import com.mikm.entities.animation.AnimationManager;
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
        ActionAnimationAllDirections actionAnimationAllDirections = new ActionAnimationAllDirections(.33f, Animation.PlayMode.LOOP, Slime.entityActionSpritesheets.walking);
        animationManager = new AnimationManager(slime, actionAnimationAllDirections);
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
        System.out.println(angleToPlayer);
    }

    @Override
    public void update() {
        super.update();
        slime.xVel = MathUtils.cos(angleToPlayer) * DASH_SPEED;
        slime.yVel = MathUtils.sin(angleToPlayer) * DASH_SPEED;
    }

    @Override
    public void checkForStateTransition() {
        if (timeElapsedInState > MAX_DASH_TIME) {
            slime.standingState.enter();
        }
        checkIfCollidedWithPlayer(DASH_DAMAGE, true);
    }
}
