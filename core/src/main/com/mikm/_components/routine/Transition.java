package com.mikm._components.routine;

import com.badlogic.ashley.core.Entity;

import java.util.Map;

public interface Transition {
    Routine getGoToRoutine();
    boolean shouldEnter(Entity entity, Routine currentRoutine);
    //TODO how its made idea for this entity system
    //First pass: collect routine names, then once all are loaded call init to update the string references into Routine references
    void init(Map<String, Routine> nameToRoutine);
}
