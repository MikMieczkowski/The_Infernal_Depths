package com.mikm.entities.inanimateEntities.projectiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.DamageInformation;
import com.mikm.entities.inanimateEntities.InanimateEntity;
import com.mikm.entities.inanimateEntities.particles.ParticleTypes;
import com.mikm.entities.inanimateEntities.particles.ParticleEffect;
import com.mikm.rendering.sound.SoundEffects;

public class Projectile extends InanimateEntity {
    private TextureRegion image;
    public float speed, angle, rotation, finalDipRotation;
    private Hurtbox hurtbox;
    private final ParticleTypes particleParameters;
    private final float lifeTime;
    private float time;
    private final float STARTING_HEIGHT = 10;
    private final float DIP_ROTATION_MULTIPLIER = .7f;
    private boolean playerProjectile = false;

    private String IMPACT_SOUND_EFFECT = "bowImpact";

    public Projectile(TextureRegion image, ParticleTypes deathParticleParameters, float lifeTime, float x, float y, boolean playerProjectile) {
        super(x, y);
        this.lifeTime = lifeTime;
        xScale = .5f;
        yScale = .5f;
        height = STARTING_HEIGHT;
        hurtbox = new Hurtbox(4f, false);
        this.image = image;
        this.particleParameters = deathParticleParameters;
        this.playerProjectile = playerProjectile;
    }

    public void setMovementAndDamageInformation(float angle, float speed, DamageInformation damageInformation) {
        this.angle = angle;
        finalDipRotation = -MathUtils.cos(angle) * DIP_ROTATION_MULTIPLIER;
        this.speed = speed;
        hurtbox.setDamageInformation(damageInformation);
    }

    @Override
    public void update() {
        xVel = MathUtils.cos(angle) * speed;
        yVel = MathUtils.sin(angle) * speed;
        time += Gdx.graphics.getDeltaTime();
        if (playerProjectile) {
            height = ExtraMathUtils.sinLerp(time, lifeTime * 2, .5f, 1f, STARTING_HEIGHT);
            hurtbox.setPosition(x + getFullBounds().width / 2, y + getFullBounds().width / 2 + height - STARTING_HEIGHT);
            hurtbox.checkIfHitEntities(playerProjectile);
            rotation = angle + 1.25f * MathUtils.PI + ExtraMathUtils.lerpAngle(time, lifeTime, 0, finalDipRotation);
        }
        moveAndCheckCollisions();
        if (collider.inWall() || time > lifeTime) {
            die();
        }
    }

    @Override
    public Rectangle getBounds() {
        final float hurtboxDiameter = hurtbox.getHurtbox().radius*2;
        return new Rectangle(x + (16-hurtboxDiameter), y+(16-hurtboxDiameter), hurtboxDiameter, hurtboxDiameter);
    }

    @Override
    public Rectangle getFullBounds() {
        return new Rectangle(x, y, 16, 16);
    }

    @Override
    public Rectangle getShadowBounds() {
        return new Rectangle(x, y-3, 16, 16);
    }

    @Override
    public void draw(Batch batch) {
        batch.draw(image, x, y+height-STARTING_HEIGHT, getFullBounds().width/2f, getFullBounds().height/2f, getFullBounds().width, getFullBounds().height, xScale, yScale, rotation*MathUtils.radDeg);
    }

    @Override
    public void onWallCollision() {
        //die();
    }

    @Override
    public void die() {
        SoundEffects.playLoud(IMPACT_SOUND_EFFECT);
        new ParticleEffect(particleParameters, x, y);
        super.die();
    }
}
