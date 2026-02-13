package com.mikm._systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Circle;
import com.mikm._components.ProjectileConfigComponent;
import com.mikm._components.Transform;
import com.mikm.utils.debug.DebugRenderer;

/**
 * Debug system that draws projectile hitboxes when active.
 * Reads directly from {@link ProjectileConfigComponent#isHitboxActive()} and
 * {@link ProjectileConfigComponent#hitboxRadius} — the same data the gameplay
 * hit detection uses. No separate debug hitbox state to keep in sync.
 */
public class ProjectileHitboxDebugSystem extends IteratingSystem {

    public ProjectileHitboxDebugSystem() {
        super(Family.all(ProjectileConfigComponent.class, Transform.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        if (!DebugRenderer.DRAW_PROJECTILE_HITBOXES) return;

        ProjectileConfigComponent config = ProjectileConfigComponent.MAPPER.get(entity);
        if (!config.isHitboxActive()) return;

        Transform transform = Transform.MAPPER.get(entity);
        DebugRenderer.getInstance().drawHitboxes(
                new Circle(transform.getCenteredX(), transform.getCenteredY(), config.hitboxRadius)
        );
    }
}
