package com.mikm.entities.enemies;

public class Routine {
    Cycle cycle;
    Transitions transitions;

    public Routine(Cycle cycle, Transitions transitions) {
        this.cycle = cycle;
        this.transitions = transitions;
    }
}
