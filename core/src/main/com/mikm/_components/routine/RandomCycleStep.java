package com.mikm._components.routine;

import com.mikm.utils.RandomUtils;
import com.mikm._components.Copyable;
import com.mikm.entities.actions.Action;

import java.util.ArrayList;
import java.util.List;

public class RandomCycleStep implements CycleStep {
    @Copyable List<Action> actions = new ArrayList<>();

    private RandomCycleStep() {

    }
    public RandomCycleStep(List<Action> actions) {
        this.actions = actions;
    }

    @Override
    public Action getAction() {
        return actions.get(RandomUtils.getInt(actions.size()-1));
    }

}
