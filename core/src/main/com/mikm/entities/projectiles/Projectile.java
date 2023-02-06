package com.mikm.entities.projectiles;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.mikm.entities.InanimateEntity;
import com.mikm.entities.particles.ParticleParameters;
import com.mikm.entities.particles.ParticleSystem;

public class Projectile extends InanimateEntity {
    private TextureRegion image;
    public float speed, angle, rotation;
    private Hurtbox hurtbox;
    private ParticleParameters particleParameters;

    public Projectile(TextureRegion image, ParticleParameters deathParticleParameters, float x, float y) {
        super(x, y);
        xScale = .5f;
        yScale = .5f;
        hurtbox = new Hurtbox(4f, false);
        this.image = image;
        this.particleParameters = deathParticleParameters;
    }

    public void setMovementAndDamageInformation(float angle, float speed, DamageInformation damageInformation) {
        this.angle = angle;
        this.speed = speed;
        hurtbox.setDamageInformation(damageInformation);
    }

    @Override
    public void update() {
        xVel = MathUtils.cos(angle) * speed;
        yVel = MathUtils.sin(angle) * speed;
        hurtbox.setPosition(x + getFullBounds().width/2, y + getFullBounds().width/2);
        hurtbox.checkIfHitEntities();
        rotation = angle + 1.25f * MathUtils.PI;
        moveAndCheckCollisions();
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
        return new Rectangle(x+4, y, 3, 3);
    }

    @Override
    public void draw(Batch batch) {
        batch.draw(image, x, y, getFullBounds().width/2f, getFullBounds().height/2f, getFullBounds().width, getFullBounds().height, xScale, yScale, rotation*MathUtils.radDeg);
    }

    @Override
    public void onWallCollision() {
        new ParticleSystem(particleParameters, x, y);
        die();
    }
}
