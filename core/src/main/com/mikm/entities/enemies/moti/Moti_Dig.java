package com.mikm.entities.enemies.moti;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.math.MathUtils;
import com.mikm.Assets;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.State;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.particles.ParticleTypes;
import com.mikm.entities.player.Player;
import com.mikm.entities.projectiles.DamageInformation;
import com.mikm.entities.projectiles.StaticProjectile;
import com.mikm.rendering.screens.Application;

public class Moti_Dig extends State {
    private Moti moti;
    private Player player;

    private boolean buildup;
    private float buildupTimer;
    private final float BUILDUP_TIME = 1.5f;
    private final float MAX_TIME = 1.5f;
    public static final int DIG_DAMAGE = 2, DIG_KNOCKBACK = 2;

    private float angle;

    private boolean shot = false;


    public Moti_Dig(Moti moti) {
        super(moti);
        this.moti = moti;
        this.player = Application.player;
    }

    @Override
    public void enter() {
        //System.out.println("Dig enter");
        super.enter();
        moti.xVel = 0;
        moti.yVel = 0;
        angle = MathUtils.atan2(player.y - moti.y, player.x - moti.x);
        shot = false;
        buildup = true;
        buildupTimer = 0;
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
                // Dig attack shoots more projectiles in a wider spread
                addProjectile(angle);
                addProjectile(angle-20*MathUtils.degRad);
                addProjectile(angle+20*MathUtils.degRad);
                addProjectile(angle-40*MathUtils.degRad);
                addProjectile(angle+40*MathUtils.degRad);
            }
        }

        handlePlayerCollision(1, false);
    }

    private void addProjectile(float angle) {
        Moti_WebProjectile p = new Moti_WebProjectile(Assets.testTexture, ParticleTypes.getKnockbackDustParameters(), 1.5f, moti.getCenteredPosition().x, moti.getCenteredPosition().y, false);
        p.setMovementAndDamageInformation(angle, 3, new DamageInformation(angle, DIG_KNOCKBACK, DIG_DAMAGE));
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
