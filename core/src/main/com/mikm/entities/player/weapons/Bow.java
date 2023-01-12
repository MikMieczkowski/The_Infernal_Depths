package com.mikm.entities.player.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.mikm.entities.particles.ParticleParameters;
import com.mikm.entities.projectiles.DamageInformation;
import com.mikm.entities.projectiles.Projectile;
import com.mikm.input.GameInput;
import com.mikm.rendering.screens.Application;

public class Bow extends Weapon{
    private TextureRegion[] images;
    private TextureRegion arrowImage;

    private float timeHeld;
    private int powerLevel;
    private final float TIME_PER_POWER_LEVEL = .25f;
    private final float ARROW_SPEED = 2;
    private final int ARROW_DAMAGE = 1;
    public final float ARROW_KNOCKBACK_FORCE = 1;

    public Bow(TextureRegion[] images, TextureRegion arrowImage) {
        super(images[0]);
        this.images = images;
        this.arrowImage = arrowImage;
    }

    @Override
    public void checkForHit() {
        hurtbox.checkForHit();
    }

    @Override
    public void checkForStateTransition() {
        if (!GameInput.isAttackButtonPressed()) {
            if (powerLevel > 0) {
                Projectile arrow = new Projectile(arrowImage, ParticleParameters.getArrowParameters(), x, y);
                arrow.setMovementAndDamageInformation(angleToMouse, ARROW_SPEED * powerLevel, getDamageInformation());
                Application.currentScreen.inanimateEntities.add(arrow);
            }
            powerLevel = 0;
            player.walkingState.enter();
        }
    }

    @Override
    public void enterAttackState() {
        powerLevel = 0;
        timeHeld = 0;
    }

    @Override
    public void updateDuringAttackState() {
        timeHeld += Gdx.graphics.getDeltaTime();
        powerLevel = Math.min(3, (int) (timeHeld/TIME_PER_POWER_LEVEL));
    }

    @Override
    public void update() {
        angleToMouse = GameInput.getAttackingAngle();
        weaponRotation = angleToMouse + .75f*MathUtils.PI;
        x = player.getCenteredPosition().x + orbitDistance * MathUtils.cos(angleToMouse) - getFullBounds().width/2;
        y = player.getCenteredPosition().y + orbitDistance * MathUtils.sin(angleToMouse) - getFullBounds().height/2 - 6;
        image = images[powerLevel];
    }

    @Override
    public float getTotalAttackTime() {
        return 0;
    }

    @Override
    public DamageInformation getDamageInformation() {
        return new DamageInformation(angleToMouse, ARROW_KNOCKBACK_FORCE, ARROW_DAMAGE);
    }
}
