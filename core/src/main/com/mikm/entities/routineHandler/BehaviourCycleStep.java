package com.mikm.entities.routineHandler;

import com.mikm.entities.Entity;
import com.mikm.entities.actions.Action;

public class BehaviourCycleStep implements CycleStep {
    Action action;

    public BehaviourCycleStep(Action action) {
        this.action = action;
    }

    @Override
    public Action getAction() {
        return action;
    }

    @Override
    public CycleStep copy(Entity entity) {
        return new BehaviourCycleStep(action.copy(entity));
    }
}
