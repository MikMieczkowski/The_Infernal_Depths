package com.mikm.entities.actions;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.mikm._components.CopyReference;
import com.mikm._components.Copyable;
import com.mikm._components.Transform;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm.entities.animation.SuperAnimation;
import com.mikm.entities.prefabLoader.Blackboard;

import java.util.Map;


//doing nothing bug may mean the enemy is running a different entities routines or actions
//Actions are kind of a stateless class, except for the @Copyable parameters.
public abstract class Action {

    @Copyable public String name;
    @CopyReference public SuperAnimation animation;
    @Copyable public Float MAX_TIME;
    @Copyable public com.mikm._components.routine.Routine ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO;

    // Optional per-behaviour control: when this entity is hit, choose which routine to enter.
    // If null/empty, default to re-entering the current routine (reset cycle).
    // If set to "NONE", no routine is entered and current action continues (no interrupt).
    @Copyable public String POST_HIT_ROUTINE;

    //only used by entity loader
    public abstract Component createActionComponent();

    public Action () {

    }

    public void enter(Entity entity) {
        Transform transform = Transform.MAPPER.get(entity);
        RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(entity);

        transform.height = 0;
        routineListComponent.CURRENT_ACTION_IS_DONE = false;
        routineListComponent.timeElapsedInCurrentAction = 0;

        if (transform.ENTITY_NAME.equals("player")) {
            System.out.println(transform.ENTITY_NAME + " Entered " + name);
        }
    }

    public void postConfigRead(Entity entity) {
        Blackboard.getInstance().bind("timeSince" + name, entity, 0f);
    }

    public void onExit(Entity entity) {

    }

    public void update(Entity entity) {
        RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(entity);
        routineListComponent.timeElapsedInCurrentAction += Gdx.graphics.getDeltaTime();
    }
}
