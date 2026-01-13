package com.mikm._components.routine;

import com.mikm._components.Copyable;
import com.mikm._components.routine.ActionDescriptor;
import com.mikm._components.routine.ActionFactory;
import com.mikm._components.routine.CycleStep;
import com.mikm.entities.actions.Action;

public class BehaviourCycleStep implements CycleStep {
    @Copyable Action action;

    private BehaviourCycleStep() {

    }

    public BehaviourCycleStep(Action action) {
        this.action = action;
    }

    @Override
    public Action getAction() {
        return action;
    }
}
