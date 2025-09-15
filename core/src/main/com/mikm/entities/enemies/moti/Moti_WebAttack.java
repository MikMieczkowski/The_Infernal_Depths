package com.mikm.entities.enemies.moti;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.mikm.Assets;
import com.mikm.entities.enemies.states.State;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.particles.ParticleTypes;
import com.mikm.entities.player.Player;
import com.mikm.entities.projectiles.DamageInformation;
import com.mikm.rendering.screens.Application;

public class Moti_WebAttack extends State {
    private Moti moti;
    private Player player;

    private boolean buildup;
    private float buildupTimer;
    private final float BUILDUP_TIME = .5f;
    private final float MAX_TIME = 1f;
    public static final int WEB_DAMAGE = 1, WEB_KNOCKBACK = 1;

    private float angle;

    private boolean shot = false;


    public Moti_WebAttack(Moti moti) {
        super(moti);
        this.moti = moti;
        this.player = Application.player;
    }

    @Override
    public void enter() {
        super.enter();
        moti.xVel = 0;
        moti.yVel = 0;
        angle = MathUtils.atan2(player.y - moti.y, player.x - moti.x);
        shot = false;
        moti.startSquish(0, 2, BUILDUP_TIME, true);
    }

    @Override
    public void update() {
        super.update();
        if (buildup) {
            buildupTimer += Gdx.graphics.getDeltaTime();
            if (buildupTimer > BUILDUP_TIME) {
                buildupTimer = 0;
                buildup = false;
            }
        } else {
            if (!shot) {
                shot = true;
                addProjectile(angle);
                addProjectile(angle-15*MathUtils.degRad);
                addProjectile(angle+15*MathUtils.degRad);
            }
        }

        handlePlayerCollision(1, false);
    }

    private void addProjectile(float angle) {
        Moti_WebProjectile p = new Moti_WebProjectile(Assets.testTexture, ParticleTypes.getKnockbackDustParameters(), 1, moti.getCenteredPosition().x, moti.getCenteredPosition().y, false);
        p.setMovementAndDamageInformation(angle, 2, new DamageInformation(angle, WEB_KNOCKBACK, WEB_DAMAGE));
        Application.getInstance().currentScreen.addInanimateEntityInstantly(p);
    }

    @Override
    public void checkForStateTransition() {
        if (timeElapsedInState > MAX_TIME) {
            moti.stateManager.updateState();
        }
    }

    @Override
    protected AnimationName getAnimationName() {
        return AnimationName.STAND;
    }
}
