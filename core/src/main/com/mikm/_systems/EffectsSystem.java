package com.mikm._systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.mikm._components.EffectsComponent;
import com.mikm._components.SpriteComponent;
import com.mikm._components.Transform;
import com.mikm.rendering.screens.Application;
import com.mikm.utils.DeltaTime;
import com.mikm.utils.ExtraMathUtils;

public class EffectsSystem extends IteratingSystem {
    public EffectsSystem() {
        super(Family.all(EffectsComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        boolean isParticle = com.mikm._components.ParticleComponent.MAPPER.get(entity) != null;
        if (!Application.getInstance().systemShouldTick() && !isParticle) {
            return;
        }
        EffectsComponent effectsComponent = EffectsComponent.MAPPER.get(entity);
        handleFlash(effectsComponent);
        handleSquish(entity);
        handleBounce(entity);
        handleSizeChange(entity);
        handleColorChange(entity);
    }
    private void handleFlash(EffectsComponent effectsComponent) {
        if (effectsComponent.shouldFlash) {
            effectsComponent.flashTimer += Gdx.graphics.getDeltaTime();
            if (effectsComponent.flashTimer >= effectsComponent.MAX_FLASH_TIME) {
                effectsComponent.shouldFlash = false;
                effectsComponent.flashTimer = 0;
            }
        }
    }
    private void handleSquish(Entity entity) {
        EffectsComponent effectsComponent = EffectsComponent.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);

        if (effectsComponent.triggerSquish) {
            if (effectsComponent.preSquishTimer > effectsComponent.squishDelay) {
                effectsComponent.preSquishTimer = 0;
                effectsComponent.triggerSquish = false;
                effectsComponent.squishing = true;
            } else {
                effectsComponent.preSquishTimer += Gdx.graphics.getDeltaTime();
            }
        }
        if (effectsComponent.squishing) {
            transform.xScale = ExtraMathUtils.lerp(effectsComponent.squishTimer, effectsComponent.maxSquishTime, 1, effectsComponent.squishAmount);
            transform.yScale = ExtraMathUtils.lerp(effectsComponent.squishTimer, effectsComponent.maxSquishTime, 1, 1 / effectsComponent.squishAmount);
            if (effectsComponent.squishTimer < effectsComponent.maxSquishTime) {
                effectsComponent.squishTimer += Gdx.graphics.getDeltaTime();
            } else {
                effectsComponent.squishTimer = 0;
                effectsComponent.squishing = false;
            }
        } else {
            transform.xScale = MathUtils.lerp(transform.xScale, 1, .5f);
            transform.yScale = MathUtils.lerp(transform.yScale, 1, .5f);
        }
    }

    private void handleBounce(Entity entity) {
        EffectsComponent effectsComponent = EffectsComponent.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);
        if (effectsComponent.shouldBounce) {
            effectsComponent.bounceTimer += Gdx.graphics.getDeltaTime();
            if (effectsComponent.bounceTimer > effectsComponent.maxBounceTime) {
                effectsComponent.bounceTimer = effectsComponent.maxBounceTime;
                effectsComponent.shouldBounce = false;
            }

            transform.height = ExtraMathUtils.bounceLerp(effectsComponent.bounceTimer, effectsComponent.maxBounceTime, effectsComponent.peakHeight,effectsComponent.bounceCoefficient, effectsComponent.BOUNCE_FREQUENCY);
        }
    }

    private void handleColorChange(Entity entity) {
        EffectsComponent effectsComponent = EffectsComponent.MAPPER.get(entity);
        SpriteComponent spriteComponent = SpriteComponent.MAPPER.get(entity);

        if (effectsComponent.shouldChangeColor && spriteComponent != null) {
            effectsComponent.colorTimer += Gdx.graphics.getDeltaTime();
            if (effectsComponent.colorTimer > effectsComponent.maxColorTime) {
                effectsComponent.colorTimer = effectsComponent.maxColorTime;
                effectsComponent.shouldChangeColor = false;
            }

            spriteComponent.color = ExtraMathUtils.lerpColor(effectsComponent.colorTimer, effectsComponent.maxColorTime, effectsComponent.startColor, effectsComponent.endColor);
        }
    }

    private void handleSizeChange(Entity entity) {
        EffectsComponent effectsComponent = EffectsComponent.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);

        if (effectsComponent.shouldChangeSize) {
            effectsComponent.sizeTimer += Gdx.graphics.getDeltaTime();
            if (effectsComponent.sizeTimer > effectsComponent.maxSizeTime) {
                effectsComponent.sizeTimer = effectsComponent.maxSizeTime;
                effectsComponent.shouldChangeSize = false;
                // Particle has reached end of life, remove it
                Application.getInstance().currentScreen.removeEntity(entity);
                return;
            }

            float t = effectsComponent.sizeTimer / effectsComponent.maxSizeTime;
            transform.xScale = MathUtils.lerp(effectsComponent.startSize, effectsComponent.endSize, t);
            transform.yScale = transform.xScale;

            if (transform.xScale <= 0) {
                Application.getInstance().currentScreen.removeEntity(entity);
            }
        }
    }

}
