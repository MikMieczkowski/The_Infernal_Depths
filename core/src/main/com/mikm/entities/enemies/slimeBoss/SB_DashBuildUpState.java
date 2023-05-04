package com.mikm.entities.enemies.slimeBoss;

import com.badlogic.gdx.math.Vector2;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.State;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.player.Player;
import com.mikm.rendering.screens.Application;

public class SB_DashBuildUpState extends State {
    private SlimeBoss slimeBoss;
    private Player player;
    public static final float BUILDUP_DASH_HEIGHT = 30;
    public static final float BUILDUP_DASH_TIME = 1.5f;
    private Vector2 originalPosition;

    public SB_DashBuildUpState(SlimeBoss slimeBoss) {
        super(slimeBoss);
        this.slimeBoss = slimeBoss;
        this.player = Application.player;
    }

    @Override
    public void enter() {
        throw new RuntimeException("provide parameters");
    }

    public void enter(Vector2 originalPosition) {
        super.enter();
        slimeBoss.xVel = 0;
        slimeBoss.yVel = 0;
        this.originalPosition = originalPosition;
    }

    @Override
    public void update() {
        super.update();
        slimeBoss.height = ExtraMathUtils.sinLerp(timeElapsedInState, BUILDUP_DASH_TIME, BUILDUP_DASH_HEIGHT);
        handlePlayerCollision(1, false);
    }

    @Override
    public void checkForStateTransition() {
        if (timeElapsedInState > BUILDUP_DASH_TIME) {
            slimeBoss.startSquish(0, 1.5f, .2f, true);
            slimeBoss.dashState.enter(originalPosition, true);
        }
    }

    @Override
    protected AnimationName getAnimationName() {
        return AnimationName.SLIMEBOSS_STAND;
    }
}
