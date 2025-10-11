package com.mikm.entities.enemies.cyclestep;

import com.badlogic.gdx.math.MathUtils;
import com.mikm.ExtraMathUtils;
import com.mikm.RandomUtils;
import com.mikm.entities.enemies.Behaviour;

import java.util.Collection;
import java.util.Map;
import java.util.List;

public class WeightedRandomCycleStep implements CycleStep {
    Map<Behaviour, Float> weightedRandom;
    public WeightedRandomCycleStep(Map<Behaviour, Float> weightedRandom) {
        this.weightedRandom = weightedRandom;
    }

    @Override
    public Behaviour getBehaviour() {
        int r = RandomUtils.getInt(1, 100);
        for (Map.Entry<Behaviour, Float> entry : weightedRandom.entrySet()) {
            Behaviour behaviour = entry.getKey();
            int probability = MathUtils.round(entry.getValue() * 100);
            if (r <= probability) {
                return behaviour;
            }
            r -= probability;
        }
        throw new RuntimeException("Error in code");
    }
}
