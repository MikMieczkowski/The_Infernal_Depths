package com.mikm._systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.mikm._components.SpriteComponent;
import com.mikm._components.Transform;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm.entities.animation.SuperAnimation;
import com.mikm.rendering.screens.Application;

//only if Application !timestop and !paused
public class AnimationSystem extends IteratingSystem {
    public AnimationSystem() {
        super(Family.all(SpriteComponent.class, RoutineListComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float v) {
        if (!Application.getInstance().systemShouldTick()) {
            return;
        }
        SpriteComponent spriteComponent = SpriteComponent.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);
        RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(entity);

        spriteComponent.animationTime += Gdx.graphics.getDeltaTime();
        SuperAnimation animation = routineListComponent.getCurrentActionsAnimation();
        animation.update(transform.direction);
        spriteComponent.textureRegion = animation.getKeyFrame(spriteComponent.animationTime);
    }
}
