package com.mikm.entities.enemies.moti;

import com.badlogic.gdx.math.MathUtils;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.enemies.states.State;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.player.Player;
import com.mikm.rendering.screens.Application;

public class Moti_TripleDashState extends State {
    public int dash = 0;
    private final float MAX_TIME_SPENT_DASHING = Moti.secPerBeat;
    private final float JUMP_HEIGHT = 5f;
    private final float DASH_DISTANCE_INITIAL = 40;
    private final float DISTANCE_MULTIPLIER = 1f;
    private Player player;
    private float angle = 0;
    float distanceToPlayer = 0;
    private Moti moti;
    public Moti_TripleDashState(Moti moti) {
        super(moti);
        this.moti = moti;
        this.player = Application.player;
    }

    @Override
    public void enter() {
        super.enter();
        moti.damagedState.interruptState = false;
        distanceToPlayer = ExtraMathUtils.distance(moti.getCenteredPosition().x, moti.getCenteredPosition().y, player.getCenteredPosition().x, player.getCenteredPosition().y);
        angle = MathUtils.atan2(player.y - moti.y, player.x - moti.x);
        System.out.println("entered");
    }

    @Override
    public void update() {
        super.update();
        float speed = ((float)Math.pow(DISTANCE_MULTIPLIER, dash)) * (distanceToPlayer+DASH_DISTANCE_INITIAL)/ (MAX_TIME_SPENT_DASHING * 60);
        moti.xVel = speed * MathUtils.cos(angle);
        moti.yVel = speed * MathUtils.sin(angle);

        moti.height = ExtraMathUtils.sinLerp(timeElapsedInState, MAX_TIME_SPENT_DASHING, JUMP_HEIGHT);
        handlePlayerCollision(1, false);
        System.out.println(dash);
    }

    @Override
    protected AnimationName getAnimationName() {
        return AnimationName.STAND;
    }

    @Override
    public void checkForStateTransition() {
        if (timeElapsedInState > MAX_TIME_SPENT_DASHING) {
            dash++;
            enterSingleDash();
            moti.startSquish(0, 1.5f, .2f, true);
            if (dash > 2) {
                moti.damagedState.interruptState = true;
                dash = 0;
                moti.stateManager.updateState();
            }
        }
    }

    private void enterSingleDash() {
        timeElapsedInState = 0;
        entity.setDirectionalAnimation(getAnimationName());
        entity.animationManager.resetTimer();
        entity.animationManager.update();
        entity.currentState = this;
        distanceToPlayer = ExtraMathUtils.distance(moti.x, moti.y, player.x, player.y);
        angle = MathUtils.atan2(player.y - moti.y, player.x - moti.x);
    }
}
