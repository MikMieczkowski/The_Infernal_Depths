package com.mikm._components.routine;

import com.badlogic.ashley.core.Entity;

import java.util.Map;

public class NoRepeatTransition implements Transition {
    private Routine routine;
    private String routineString;

    public NoRepeatTransition() {

    }
    public NoRepeatTransition(String routineString) {
        this.routineString = routineString;
    }
    public NoRepeatTransition(Routine routine) {
        this.routine = routine;
    }
    @Override
    public void init(Map<String, Routine> nameToRoutine) {
        routine = nameToRoutine.get(routineString);
    }

    @Override
    public Routine getGoToRoutine() {
        return routine;
    }

    @Override
    public boolean shouldEnter(Entity entity, Routine currentRoutine) {
        return currentRoutine.i >= currentRoutine.cycleSteps.size();
    }
}
