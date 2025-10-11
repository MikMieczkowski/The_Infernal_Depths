package com.mikm.entities.routineHandler;

import com.mikm.entities.Entity;

import java.util.Map;
import java.util.function.Predicate;

public class ConditionTransition {
    public Predicate<Entity> pred;
    private Routine goTo;
    private Cycle current;
    private String goToString;

    public ConditionTransition(Predicate<Entity> pred, String goToString) {
        this.pred = pred;
        this.goToString = goToString;
    }

    // copy constructor for safe deep copy when cloning routines
    public ConditionTransition(ConditionTransition other) {
        this.pred = other.pred;
        this.goToString = other.goToString;
    }

    public String getGoToString() {
        return goToString;
    }

    //First pass: collect routine names, then once all are loaded call init to update the string references into Routine references
    public void init(Map<String, Routine> nameToRoutine) {
        if (!nameToRoutine.containsKey(goToString)) {
            throw new RuntimeException("CAN NOT GO TO ROUTINE " + goToString + " in condition transition");
        }
        goTo = nameToRoutine.get(goToString);
    }

    public Routine getGoTo() {
        if (goTo == null) {
            throw new RuntimeException("Must first call init before getGoTo()");
        }
        return goTo;
    }

    public boolean getCondition(Entity entity) {
        return pred.test(entity);
    }
}
