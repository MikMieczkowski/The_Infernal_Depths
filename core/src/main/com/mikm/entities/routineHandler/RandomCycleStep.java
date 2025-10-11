package com.mikm.entities.enemies.cyclestep;

import com.mikm.RandomUtils;
import com.mikm.entities.enemies.Behaviour;

import java.util.List;

public class RandomCycleStep implements CycleStep {
    List<Behaviour> behaviours;
    public RandomCycleStep(List<Behaviour> behaviours) {
        this.behaviours = behaviours;
    }

    @Override
    public Behaviour getBehaviour() {
        return behaviours.get(RandomUtils.getInt(behaviours.size()-1));
    }
}
