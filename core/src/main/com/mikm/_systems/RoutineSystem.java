package com.mikm._systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mikm._components.CombatComponent;
import com.mikm._components.Transform;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm.entities.actions.DamagedAction;
import com.mikm.rendering.screens.Application;

//only if Application !timestop and !paused
public class RoutineSystem extends IteratingSystem {
    private static final Family family = Family.all(RoutineListComponent.class, Transform.class).get();
    public RoutineSystem() {
        super(family);
    }

    @Override
    protected void processEntity(Entity entity, float v) {
        if (!Application.getInstance().systemShouldTick()) {
            return;
        }
        RoutineListComponent r = RoutineListComponent.MAPPER.get(entity);
        DamagedAction.DamagedActionComponent damagedActionComponent = DamagedAction.MAPPER.get(entity);

        //Can be cleaned up
        if (r.active) {
            if (damagedActionComponent != null && damagedActionComponent.active) {
                r.damagedAction.update(entity);
            } else {
                r.update(entity);
            }
        }
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);

        engine.addEntityListener(family, new EntityListener() {
            @Override
            public void entityAdded(Entity entity) {
                //System.out.println(Transform.MAPPER.get(entity).ENTITY_NAME);
                RoutineListComponent.MAPPER.get(entity).runtimeInit(entity);
            }

            @Override
            public void entityRemoved(Entity entity) { }
        });
    }

}
