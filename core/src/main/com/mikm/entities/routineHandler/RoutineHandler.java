package com.mikm.entities.enemies;

import com.mikm.entities.enemies.cyclestep.ConditionTransition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RoutineHandler {
    Entity entity;
    private ArrayList<Routine> routines;

    Routine currentRoutine;
    Map<Behaviour, Float> timeSinceBehaviour = new HashMap<>();

    public RoutineHandler(Entity entity) {
        this.entity = entity;
    }

    //Expects cycles.get(0) to be the cycle named start
    public void init(ArrayList<Routine> routines) {
        this.routines = routines;
    }

    public void update() {
        //Calls checkForTransition
        currentRoutine.cycle.update();
    }

    //Gets called after any behaviour is complete
    public boolean checkForTransition() {
        //check NoRepeatTransition
        if (currentRoutine.transitions.hasNoRepeatTransition) {
            if (currentRoutine.cycle.i >= currentRoutine.cycle.cycleSteps.size()) {
                currentRoutine = currentRoutine.transitions.noRepeatGoTo;
                return true;
            }
        }
        //check conditionTransitions
        for (ConditionTransition conditionTransition : currentRoutine.transitions.conditionTransitions) {
            if (conditionTransition.condition(entity)) {
                currentRoutine = conditionTransition.goTo;
                return true;
            }
        }
        return false;
    }

    public float getTimeSinceBehaviour(Behaviour behaviour) {
        return timeSinceBehaviour.get(behaviour);
    }
}
