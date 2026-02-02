package com.mikm._systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Circle;
import com.mikm._components.EffectsComponent;
import com.mikm._components.ProjectileComponent;
import com.mikm._components.ProjectileConfigComponent;
import com.mikm._components.SpriteComponent;
import com.mikm._components.Transform;
import com.mikm.rendering.BatchUtils;
import com.mikm.rendering.screens.Application;
import com.mikm.utils.debug.DebugRenderer;

import java.util.Comparator;

public class RenderingSystem extends SortedIteratingSystem {
    public RenderingSystem() {
        super(Family.all(Transform.class, SpriteComponent.class).get(), Comparator.comparing(Transform.MAPPER::get));
    }

    @Override
    protected void processEntity(Entity entity, float v) {
        SpriteComponent sprite = SpriteComponent.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);

        if (sprite.visible) {
            // Apply flash shader if entity is flashing
            EffectsComponent effects = EffectsComponent.MAPPER.get(entity);
            if (effects != null && effects.shouldFlash) {
                Application.getInstance().setFillColorShader(Application.batch, effects.flashColor);
            }

            Application.batch.setColor(sprite.color);
            BatchUtils.draw(sprite.textureRegion, transform.x, transform.y + transform.height, transform.ORIGIN_X, transform.ORIGIN_Y,
                    transform.getFullBounds().width, transform.getFullBounds().height, transform.xScale, transform.yScale, transform.rotation,
                    true, sprite.flipped);
            Application.batch.setColor(Color.WHITE);

            // Reset shader after drawing flashing entity
            if (effects != null && effects.shouldFlash) {
                Application.batch.setShader(null);
            }

            // Debug: draw player projectile hitbox
            ProjectileComponent projComp = ProjectileComponent.MAPPER.get(entity);
            if (projComp != null && projComp.isPlayer) {
                ProjectileConfigComponent config = ProjectileConfigComponent.MAPPER.get(entity);
                if (config == null || config.isHitboxActive()) {
                    float radius = (config != null) ? config.hitboxRadius : 16f;
                    Circle hitbox = new Circle(transform.getCenteredX(), transform.getCenteredY(), radius);
                    DebugRenderer.getInstance().drawHitboxes(hitbox);
                }
            }
        }
    }
}
