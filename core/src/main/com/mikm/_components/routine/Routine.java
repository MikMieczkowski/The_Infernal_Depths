package com.mikm._components.routine;

import com.mikm._components.Copyable;
import com.mikm.entities.actions.Action;

import java.util.ArrayList;

public class Routine {
    @Copyable public String name;

    //cycle
    @Copyable ArrayList<CycleStep> cycleSteps;
    Action currentAction;
    int i;

    //transitions
    @Copyable ArrayList<Transition> transitions;

    public Routine() {

    }

    public Routine(ArrayList<CycleStep> cycleSteps, ArrayList<Transition> transitions) {
        init(cycleSteps, transitions);
    }

    public void init(ArrayList<CycleStep> cycleSteps, ArrayList<Transition> transitions) {
        this.cycleSteps = cycleSteps;
        this.transitions = transitions;
    }
}
