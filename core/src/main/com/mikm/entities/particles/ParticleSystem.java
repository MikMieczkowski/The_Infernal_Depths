package com.mikm.entities.particles;

import com.badlogic.gdx.math.MathUtils;
import com.mikm.ExtraMathUtils;
import com.mikm.rendering.screens.Application;

public class ParticleSystem  {
    int positionOffsetRadius;
    Particle[] particles;

    public ParticleSystem(ParticleParameters parameters, float x, float y) {
        createParticles(parameters, 0, x, y);
    }

    public ParticleSystem(ParticleParameters parameters, float angleOffset, float x, float y) {
        createParticles(parameters, angleOffset, x, y);
    }

    public void createParticles(ParticleParameters parameters, float angleOffset, float x, float y) {
        int amount = ExtraMathUtils.randomInt(parameters.amountMin, parameters.amountMax);
        particles = new Particle[amount];
        positionOffsetRadius = parameters.positionOffsetRadius;

        for (int i = 0; i < amount; i++) {
            particles[i] = new Particle(parameters, parameters.usesColor ? ExtraMathUtils.randomColor(parameters.startColorMin, parameters.startColorMax) : null,
                    parameters.usesColor ? ExtraMathUtils.randomColor(parameters.endColorMin, parameters.endColorMax) :null, ExtraMathUtils.randomFloat(parameters.sizeMin, parameters.sizeMax),
                    ExtraMathUtils.randomFloat(parameters.angleMin, parameters.angleMax) + angleOffset, ExtraMathUtils.randomFloat(parameters.speedMin, parameters.speedMax));
        }

        spawnParticlesAt(x, y);
    }


    public void spawnParticlesAt(float x, float y) {
        for (Particle particle : particles) {
            final float offsetAngle = ExtraMathUtils.randomFloat(0, MathUtils.PI2);
            final float xOffset = MathUtils.cos(offsetAngle) * positionOffsetRadius;
            final float yOffset = MathUtils.sin(offsetAngle) * positionOffsetRadius;
            particle.setPosition(x + xOffset, y + yOffset);
            Application.currentScreen.inanimateEntities.add(particle);
        }
    }


}
