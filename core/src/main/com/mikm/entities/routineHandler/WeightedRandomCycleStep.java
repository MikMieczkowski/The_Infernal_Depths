package com.mikm.entities.routineHandler;

import com.badlogic.gdx.math.MathUtils;
import com.mikm.RandomUtils;
import com.mikm.entities.Entity;
import com.mikm.entities.actions.Action;

import java.util.HashMap;
import java.util.Map;

public class WeightedRandomCycleStep implements CycleStep {
    Map<Action, Float> weightedRandom;
    public WeightedRandomCycleStep(Map<Action, Float> weightedRandom) {
        this.weightedRandom = weightedRandom;
    }

    @Override
    public Action getAction() {
        int r = RandomUtils.getInt(1, 100);
        for (Map.Entry<Action, Float> entry : weightedRandom.entrySet()) {
            Action action = entry.getKey();
            int probability = MathUtils.round(entry.getValue() * 100);
            if (r <= probability) {
                return action;
            }
            r -= probability;
        }
        throw new RuntimeException("Error in code");
    }

    @Override
    public CycleStep copy(Entity entity) {
        Map<Action, Float> weightedRandom = new HashMap<>();
        for (Map.Entry<Action, Float> entry : this.weightedRandom.entrySet()) {
            Action action = entry.getKey();
            float probability = entry.getValue();
            weightedRandom.put(action.copy(entity), probability);
        }
        return new WeightedRandomCycleStep(weightedRandom);
    }
}
