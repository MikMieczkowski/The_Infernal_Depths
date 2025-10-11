package com.mikm.entities.routineHandler;

import java.util.ArrayList;
import java.util.Map;

public class Transitions {

    //TODO remove public
    public ArrayList<ConditionTransition> conditionTransitions;
    Routine noRepeatGoTo;
    String noRepeatGoToString;
    boolean hasNoRepeatTransition;


    //copy constructor
    public Transitions(Transitions transitions) {
        this.hasNoRepeatTransition = transitions.hasNoRepeatTransition;
        this.noRepeatGoToString = transitions.noRepeatGoToString;
        // Recreate conditionTransitions list with shallow-copied predicates and preserved goToString;
        // goTo references will be re-initialized in init(nameToRoutine)
        if (transitions.conditionTransitions != null) {
            this.conditionTransitions = new ArrayList<>();
            for (ConditionTransition ct : transitions.conditionTransitions) {
                this.conditionTransitions.add(new ConditionTransition(ct));
            }
        }
    }

    public Transitions(ArrayList<ConditionTransition> conditionTransitions, String noRepeatGoToString) {
        hasNoRepeatTransition = true;
        this.conditionTransitions = conditionTransitions;
        this.noRepeatGoToString = noRepeatGoToString;

    }

    public Transitions(ArrayList<ConditionTransition> conditionTransitions) {
        hasNoRepeatTransition = false;
        this.conditionTransitions = conditionTransitions;
    }

    public Transitions(String noRepeatGoToString) {
        hasNoRepeatTransition = true;
        this.noRepeatGoToString = noRepeatGoToString;
    }

    //TODO how its made idea for this entity system
    //First pass: collect routine names, then once all are loaded call init to update the string references into Routine references
    public void init(Map<String, Routine> nameToRoutine) {
        if (hasNoRepeatTransition) {
            noRepeatGoTo = nameToRoutine.get(noRepeatGoToString);
        }

        if (conditionTransitions == null) {
            return;
        }
        for (ConditionTransition conditionTransition : conditionTransitions) {
            conditionTransition.init(nameToRoutine);
        }
    }
}
