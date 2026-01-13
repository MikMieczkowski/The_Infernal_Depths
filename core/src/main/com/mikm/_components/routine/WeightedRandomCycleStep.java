package com.mikm._components.routine;

import com.badlogic.gdx.math.MathUtils;
import com.mikm.utils.RandomUtils;
import com.mikm._components.Copyable;
import com.mikm.entities.actions.Action;

import java.util.HashMap;
import java.util.Map;

public class WeightedRandomCycleStep implements CycleStep {
    @Copyable Map<Action, Float> weightedRandom = new HashMap<>();

    private WeightedRandomCycleStep() {

    }
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
}
