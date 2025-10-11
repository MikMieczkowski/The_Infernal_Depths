package com.mikm.entities.routineHandler;

import com.mikm.entities.Entity;

public class Routine {
    public String name;
    //TODO remove public
    public Cycle cycle;

    //TODO remove public
    public Transitions transitions;

    public Routine() {

    }
    //copy constructor
    public Routine(Entity entity, Routine routine) {
        this.name = routine.name;
        this.cycle = new Cycle(entity, routine.cycle);
        this.transitions = new Transitions(routine.transitions);
    }

    public Routine(Cycle cycle, Transitions transitions) {
        init(cycle, transitions);
    }

    public void init(Cycle cycle, Transitions transitions) {
        this.cycle = cycle;
        this.transitions = transitions;
    }
}
