package com.mikm.entities.player.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.mikm.entities.DamageInformation;
import com.mikm.entities.actions.DamagedAction;
import com.mikm.entities.inanimateEntities.particles.ParticleTypes;
import com.mikm.entities.inanimateEntities.projectiles.Projectile;
import com.mikm.input.GameInput;
import com.mikm.rendering.sound.SoundEffects;
import com.mikm.rendering.screens.Application;

public class Bow extends Weapon{
    private TextureRegion[] stringImages;
    private TextureRegion arrowImage;

    private float timeHeld, timeSinceLastAttack;
    private boolean attacking;
    private int powerLevel;

    private float timePerPowerLevel;
    private float arrowSpeed;
    private float cooldown;
    private boolean powerLevelWasZero = true;
    private int arrowDamage;
    public float arrowKnockback;

    private String BOW_READY_SOUND_EFFECT = "bowReady.ogg";
    private String BOW_SHOOT_SOUND_EFFECT = "bowShoot.ogg";

    public Bow(TextureRegion image, TextureRegion[] stringImages, TextureRegion arrowImage, int arrowDamage, int arrowKnockback, float cooldown, float timePerPowerLevel, float arrowSpeed) {
        super(image);
        this.stringImages = stringImages;
        this.arrowImage = arrowImage;
        this.arrowDamage = arrowDamage;
        this.arrowKnockback = arrowKnockback;
        this.cooldown = cooldown;
        this.timePerPowerLevel = timePerPowerLevel;
        this.arrowSpeed = arrowSpeed;
    }

    @Override
    public void checkForHit() {
        hurtbox.checkIfHitEntities(true);
    }

    @Override
    public void enterAttackState() {

    }

    @Override
    public void updateDuringAttackState() {
        if (timeSinceLastAttack > cooldown && !attacking) {
            attacking = true;
            powerLevel = 0;
            powerLevelWasZero = true;
            timeHeld = 0;
        }
        if (attacking) {
            timeHeld += Gdx.graphics.getDeltaTime();
            powerLevel = Math.min(3, (int) (timeHeld / timePerPowerLevel) + 1);
            if (powerLevelWasZero && powerLevel == 1) {
                powerLevelWasZero = false;
                SoundEffects.playQuiet(BOW_READY_SOUND_EFFECT);
            }
        }
    }

    @Override
    public void update() {
        if (!attacking) {
            timeSinceLastAttack+=Gdx.graphics.getDeltaTime();
        }
        angleToMouse = GameInput.getAttackingAngle();
        weaponRotation = angleToMouse + .75f*MathUtils.PI;
        x = player.getHitbox().x + orbitDistance * MathUtils.cos(angleToMouse) - getFullBounds().width/2;
        y = player.getHitbox().y + orbitDistance * MathUtils.sin(angleToMouse) - getFullBounds().height/2 - 6;
    }


    public boolean isReleased() {
        return !GameInput.isAttackButtonPressed() || powerLevel == 3 && timeHeld>timePerPowerLevel*3;
    }

    @Override
    public void exitAttackState() {
        if (powerLevel > 0) {
            SoundEffects.play(BOW_SHOOT_SOUND_EFFECT);
            Projectile arrow = new Projectile(arrowImage, ParticleTypes.getArrowParameters(),.4f, x, y, true);
            arrow.setMovementAndDamageInformation(angleToMouse, arrowSpeed * powerLevel, getDamageInformation());
            Application.getInstance().currentScreen.addInanimateEntity(arrow);
            timeHeld = 0;
            powerLevel = 0;
            powerLevelWasZero = true;
            attacking = false;
            timeSinceLastAttack = 0;
        }
    }

    @Override
    public void draw(Batch batch) {
        super.draw(batch);
        batch.draw(stringImages[powerLevel], x, y, 8, 8, getFullBounds().width, getFullBounds().height, 1, 1, weaponRotation * MathUtils.radDeg);
    }

    @Override
    public float getTotalAttackTime() {
        return 0;
    }

    @Override
    public DamageInformation getDamageInformation() {
        return new DamageInformation(angleToMouse, arrowKnockback, arrowDamage);
    }
}
