package com.mikm.entities.player.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.mikm.entities.particles.ParticleTypes;
import com.mikm.entities.projectiles.DamageInformation;
import com.mikm.entities.projectiles.Projectile;
import com.mikm.input.GameInput;
import com.mikm.rendering.screens.Application;

public class Bow extends Weapon{
    private TextureRegion[] images;
    private TextureRegion arrowImage;

    private float timeHeld, timeSinceLastAttack;
    private boolean attacking;
    private int powerLevel;
    private final float TIME_PER_POWER_LEVEL = .3f;
    private final float ARROW_SPEED = 2;
    private final int ARROW_DAMAGE = 1;
    public final float ARROW_KNOCKBACK_FORCE = 1;
    private final float COOLDOWN = .4f;

    public Bow(TextureRegion[] images, TextureRegion arrowImage) {
        super(images[0]);
        this.images = images;
        this.arrowImage = arrowImage;
    }

    @Override
    public void checkForHit() {
        hurtbox.checkIfHitEntities();
    }

    @Override
    public void enterAttackState() {

    }

    @Override
    public void updateDuringAttackState() {
        if (timeSinceLastAttack > COOLDOWN && !attacking) {
            attacking = true;
            powerLevel = 0;
            timeHeld = 0;
        }
        if (attacking) {
            timeHeld += Gdx.graphics.getDeltaTime();
            powerLevel = Math.min(3, (int) (timeHeld / TIME_PER_POWER_LEVEL) + 1);
        }
    }

    @Override
    public void update() {
        if (!attacking) {
            timeSinceLastAttack+=Gdx.graphics.getDeltaTime();
        }
        angleToMouse = GameInput.getAttackingAngle();
        weaponRotation = angleToMouse + .75f*MathUtils.PI;
        x = player.getCenteredPosition().x + orbitDistance * MathUtils.cos(angleToMouse) - getFullBounds().width/2;
        y = player.getCenteredPosition().y + orbitDistance * MathUtils.sin(angleToMouse) - getFullBounds().height/2 - 6;
        image = images[powerLevel];
    }

    @Override
    public void checkForStateTransition() {
        if (!GameInput.isAttackButtonPressed()) {
            exitAttackState();
            player.walkingState.enter();
        }
    }

    @Override
    public void exitAttackState() {
        if (powerLevel > 0) {
            Projectile arrow = new Projectile(arrowImage, ParticleTypes.getArrowParameters(),.4f, x, y);
            arrow.setMovementAndDamageInformation(angleToMouse, ARROW_SPEED * powerLevel, getDamageInformation());
            Application.currentScreen.addInanimateEntity(arrow);
            timeHeld = 0;
            powerLevel = 0;
            attacking = false;
            timeSinceLastAttack = 0;
        }
    }

    @Override
    public float getTotalAttackTime() {
        return 0;
    }

    @Override
    public DamageInformation getDamageInformation() {
        return new DamageInformation(angleToMouse, ARROW_KNOCKBACK_FORCE, ARROW_DAMAGE * powerLevel);
    }
}
