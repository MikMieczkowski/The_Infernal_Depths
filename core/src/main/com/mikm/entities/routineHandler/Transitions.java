package com.mikm.entities.enemies;

import com.mikm.entities.enemies.cyclestep.ConditionTransition;

import java.util.ArrayList;

public class Transitions {

    ArrayList<ConditionTransition> conditionTransitions;
    Routine noRepeatGoTo;
    boolean hasNoRepeatTransition;

    public Transitions(ArrayList<ConditionTransition> conditionTransitions, Routine noRepeatGoTo) {
        hasNoRepeatTransition = true;
        this.conditionTransitions = conditionTransitions;
        this.noRepeatGoTo = noRepeatGoTo;

    }

    public Transitions(ArrayList<ConditionTransition> conditionTransitions) {
        hasNoRepeatTransition = false;
        this.conditionTransitions = conditionTransitions;
    }

    public Transitions(Routine noRepeatGoTo) {
        hasNoRepeatTransition = true;
        this.noRepeatGoTo = noRepeatGoTo;
    }
}
