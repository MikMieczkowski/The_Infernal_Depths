package com.mikm.entities.routineHandler;

import com.mikm.RandomUtils;
import com.mikm.entities.Entity;
import com.mikm.entities.actions.Action;

import java.util.ArrayList;
import java.util.List;

public class RandomCycleStep implements CycleStep {
    List<Action> actions;
    public RandomCycleStep(List<Action> actions) {
        this.actions = actions;
    }

    @Override
    public Action getAction() {
        return actions.get(RandomUtils.getInt(actions.size()-1));
    }

    @Override
    public CycleStep copy(Entity entity) {
        List<Action> actions = new ArrayList<>();
        for(Action action : this.actions) {
            actions.add(action.copy(entity));
        }
        return new RandomCycleStep(actions);
    }
}
