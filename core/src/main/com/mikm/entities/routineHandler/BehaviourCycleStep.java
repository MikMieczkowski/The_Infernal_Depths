package com.mikm.entities.enemies.cyclestep;

import com.mikm.entities.enemies.Behaviour;

public class BehaviourCycleStep implements CycleStep {
    Behaviour behaviour;

    public BehaviourCycleStep(Behaviour behaviour) {
        this.behaviour = behaviour;
    }

    @Override
    public Behaviour getBehaviour() {
        return behaviour;
    }
}
