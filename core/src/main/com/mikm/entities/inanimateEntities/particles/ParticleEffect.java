package com.mikm.entities.inanimateEntities.particles;

import com.badlogic.gdx.math.MathUtils;
import com.mikm.RandomUtils;
import com.mikm.rendering.screens.Application;

public class ParticleEffect {
    int positionOffsetRadius;
    public Particle[] particles;

    public ParticleEffect(ParticleTypes parameters, float x, float y) {
        createParticles(parameters, 0, x, y);
    }

    public ParticleEffect(ParticleTypes parameters, float angleOffset, float x, float y) {
        createParticles(parameters, angleOffset, x, y);
    }

    public void createParticles(ParticleTypes parameters, float angleOffset, float x, float y) {
        int amount = RandomUtils.getInt(parameters.amountMin, parameters.amountMax);
        particles = new Particle[amount];
        positionOffsetRadius = parameters.positionOffsetRadius;

        for (int i = 0; i < amount; i++) {
            particles[i] = new Particle(parameters, parameters.usesColor ? RandomUtils.getColor(parameters.startColorMin, parameters.startColorMax) : null,
                    parameters.usesColor ? RandomUtils.getColor(parameters.endColorMin, parameters.endColorMax) :null, RandomUtils.getFloat(parameters.sizeMin, parameters.sizeMax),
                    RandomUtils.getFloat(parameters.angleMin, parameters.angleMax) + angleOffset, RandomUtils.getFloat(parameters.speedMin, parameters.speedMax));
        }

        spawnParticlesAt(x, y);
    }


    public void spawnParticlesAt(float x, float y) {
        for (Particle particle : particles) {
            final float offsetAngle = RandomUtils.getFloat(0, MathUtils.PI2);
            final float xOffset = MathUtils.cos(offsetAngle) * RandomUtils.getFloat(0, positionOffsetRadius);
            final float yOffset = MathUtils.sin(offsetAngle) * RandomUtils.getFloat(0, positionOffsetRadius);
            particle.setPosition(x + xOffset, y + yOffset);
            
            //smokeparticles hard code
            if (Application.getInstance().currentScreen == Application.getInstance().townScreen) {
                Application.getInstance().townScreen.addSmokeParticle(particle);
            } else {
                Application.getInstance().currentScreen.addInanimateEntity(particle);
            }
        }
    }


}
