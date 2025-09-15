package com.mikm.entities.enemies.slimeBoss;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.enemies.states.State;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.player.Player;
import com.mikm.rendering.SoundEffects;
import com.mikm.rendering.screens.Application;

public class SB_DashAttack extends State {
    private SlimeBoss slimeBoss;
    private Player player;
    private float angle;

    private final float JUMP_HEIGHT = 5f;
    private final float DASH_DISTANCE = 130;
    private final float MAX_TIME_SPENT_DASHING = .3f;
    private final float ANGLE_OF_APPROACH_FROM_PERPENDICULAR = 15 * MathUtils.degRad;
    private final float TIME_BETWEEN_DASH_EFFECT_IMAGES = .05f;
    float distanceToPlayer = 0;


    private boolean firstDash;
    private float timeSinceLastDashEffectImage;

    private boolean dashingRightOfAngleToPlayer;

    public SB_DashAttack(SlimeBoss slimeBoss) {
        super(slimeBoss);
        this.slimeBoss = slimeBoss;
        this.player = Application.player;
    }

    @Override
    public void enter() {
        throw new RuntimeException("provide parameters");
    }

    public void enter(Vector2 originalPosition, boolean firstDash) {
        super.enter();
        timeElapsedInState = 0;
        this.firstDash = firstDash;

        float angleToPlayer = MathUtils.atan2(player.y - originalPosition.y, player.x - originalPosition.x);
        if (dashingRightOfAngleToPlayer) {
            angle = angleToPlayer +MathUtils.PI/2f - ANGLE_OF_APPROACH_FROM_PERPENDICULAR;
        } else {
            angle = angleToPlayer -MathUtils.PI/2f + ANGLE_OF_APPROACH_FROM_PERPENDICULAR;
        }
        dashingRightOfAngleToPlayer = !dashingRightOfAngleToPlayer;
        SoundEffects.play(SoundEffects.dash);
        distanceToPlayer = ExtraMathUtils.distance(slimeBoss.x, slimeBoss.y, player.x, player.y);
    }

    @Override
    public void update() {
        super.update();
        float speed = (distanceToPlayer/100f) * DASH_DISTANCE / (MAX_TIME_SPENT_DASHING * 60);
        slimeBoss.xVel = speed * MathUtils.cos(angle);
        slimeBoss.yVel = speed * MathUtils.sin(angle);

        slimeBoss.height = ExtraMathUtils.sinLerp(timeElapsedInState, MAX_TIME_SPENT_DASHING, JUMP_HEIGHT);

        timeSinceLastDashEffectImage += Gdx.graphics.getDeltaTime();
        if (timeSinceLastDashEffectImage > TIME_BETWEEN_DASH_EFFECT_IMAGES) {
            timeSinceLastDashEffectImage -= TIME_BETWEEN_DASH_EFFECT_IMAGES;
            Application.getInstance().currentScreen.addInanimateEntity(new AfterImageEffect(SlimeBoss.slimeBossImage, slimeBoss.x, slimeBoss.y+slimeBoss.height, slimeBoss.xScale, slimeBoss.yScale));
        }
        handlePlayerCollision(1, false);
    }

    @Override
    public void checkForStateTransition() {
        if (firstDash && timeElapsedInState > MAX_TIME_SPENT_DASHING/2f ||
        !firstDash && timeElapsedInState > MAX_TIME_SPENT_DASHING) {
            slimeBoss.startSquish(0, 1.5f, .2f, true);
            slimeBoss.stateManager.updateState();
        }
    }

    @Override
    protected AnimationName getAnimationName() {
        return AnimationName.STAND;
    }
}
