package com.mikm.entityLoader;

import com.mikm.entities.Entity;
import com.mikm.entities.actions.Action;
import com.mikm.entities.routineHandler.*;

import java.util.*;
import java.util.function.Predicate;

public class EntityRoutineLoader {
    private String fileName;
    private Entity entity;
    private Map<String, EntityData.RoutineData> routineDataMap;
    private Map<String, Routine> nameToRoutine = new HashMap<>();

    EntityRoutineLoader(String fileName, Entity entity, Map<String, EntityData.RoutineData> routineDataMap) {
        this.fileName = fileName;
        this.entity = entity;
        this.routineDataMap = routineDataMap;
    }
    Map<String, Routine> loadRoutines(Map<String, String> behaviourNameToInterruptRoutine, Map<String, Action> nameToAction) {
        if (routineDataMap == null) {
            throw new RuntimeException("Couldn't load cycle data from yaml file " + fileName);
        }

        ArrayList<Routine> routines = new ArrayList<>();
        List<Transitions> transitionsToInit = new ArrayList<>();

        for (Map.Entry<String, EntityData.RoutineData> routineDataEntry : routineDataMap.entrySet()) {
            String name = routineDataEntry.getKey();
            EntityData.RoutineData routineData = routineDataEntry.getValue();


            Routine routine = new Routine();
            nameToRoutine.put(name, routine);

            Cycle cycle = createCycle(routineData, nameToAction);
            Transitions transitions = createTransitions(name, routineData);
            transitionsToInit.add(transitions);
            routine.init(cycle, transitions);
            routine.name = name;
            routines.add(routine);
        }
        entity.routineHandler.init(routines);

        //init ConditionTransitions
        for (Transitions transition : transitionsToInit) {
            transition.init(nameToRoutine);
        }

        loadInterruptRoutines(behaviourNameToInterruptRoutine, nameToAction);

        //Ensure POST_HIT_ROUTINE in each action is set correctly
        for (Action action: nameToAction.values()) {
            String s = action.POST_HIT_ROUTINE;
            if (s == null || s.equals("NONE")) {
                continue;
            }
            if (!nameToRoutine.containsKey(s)) {
                throw new RuntimeException("No routine " + s + " in " + action.name + " " + fileName);
            }
        }
        return nameToRoutine;
    }

    @SuppressWarnings("unchecked")
    private Cycle createCycle(EntityData.RoutineData routineData, Map<String, Action> nameToAction) {
        ArrayList<CycleStep> cycleSteps = new ArrayList<>();
        if (routineData == null) {
            throw new RuntimeException("Incorrect ROUTINES definition in" + fileName);
        }
        if (routineData.CYCLE == null) {
            throw new RuntimeException("Must add a list of BEHAVIOUR, RANDOM, WEIGHTED_RANDOM, or perhaps other under CYCLES in" + fileName);
        }
        for (Map<String, Object> cycleStepData: routineData.CYCLE) {
            Iterator<Map.Entry<String, Object>> it = cycleStepData.entrySet().iterator();
            Map.Entry<String, Object> entry1 = it.next();
            int repeatTimes = 1;
            if (it.hasNext()) {
                //handle optional REPEAT attr under BEHAVIOUR:
                Map.Entry<String, Object> entry2 = it.next();
                String attrName = entry2.getKey();
                if (!attrName.equals("REPEAT")) {
                    throw new RuntimeException("Unknown second attribute under ROUTINES:routineName:CYCLE:-: in " + fileName);
                }
                Object repeatObj = entry2.getValue();
                repeatTimes = ((Number) repeatObj).intValue();
                if (it.hasNext()) {
                    throw new RuntimeException("Only up to two children allowed in ROUTINES:routineName:CYCLE:-: in " + fileName);
                }
            }
            Object listMapOrString = entry1.getValue();

            CycleStep cycleStep = createCycleStep(listMapOrString, nameToAction);

            for (int i = 0; i < repeatTimes; i++) {
                cycleSteps.add(cycleStep);
            }
        }
        return new Cycle(entity, cycleSteps);
    }

    private CycleStep createCycleStep(Object listMapOrString, Map<String, Action> nameToAction) {
        if (listMapOrString instanceof Map) {
            Map<String, Object> weightedRandomData = (Map<String, Object>) listMapOrString;
            Map<Action, Float> weightedRandom = new HashMap<>();

            float totalProbability = 0;
            boolean foundElse = false;
            Action elseAction = null;
            for (Map.Entry<String, Object> weightedRandomEntry : weightedRandomData.entrySet()) {
                String behaviourName = weightedRandomEntry.getKey();
                if (!nameToAction.containsKey(behaviourName)) {
                    throw new RuntimeException("Referred to a nonexistent behaviour in " + fileName);
                }
                Action action = nameToAction.get(behaviourName);
                Object obj = weightedRandomEntry.getValue();
                if (obj instanceof String) {
                    String s = (String)obj;
                    if (!s.equals("else")) {
                        throw new RuntimeException("Found string in ROUTINES:routineName:CYCLE:-:WEIGHTED_RANDOM in " + fileName + ". Only string allowed here is 'else'");
                    }
                    foundElse = true;
                    elseAction = action;
                } else {
                    Double d = (Double)obj;
                    float probability = d.floatValue();
                    totalProbability += probability;
                    weightedRandom.put(action, probability);
                }
            }
            if (foundElse) {
                weightedRandom.put(elseAction, 1-totalProbability);
            }
            return new WeightedRandomCycleStep(weightedRandom);
        } else if (listMapOrString instanceof List) {
            List<String> randomData = (List<String>) listMapOrString;
            List<Action> random = new ArrayList<>();
            for (String behaviourName : randomData) {
                if (!nameToAction.containsKey(behaviourName)) {
                    throw new RuntimeException("Referred to a nonexistent behaviour in " + fileName);
                }
                random.add(nameToAction.get(behaviourName));
            }
            return new RandomCycleStep(random);
        } else if (listMapOrString instanceof String) {
            String behaviourName = (String) listMapOrString;
            if (!nameToAction.containsKey(behaviourName)) {
                throw new RuntimeException("Referred to nonexistent behaviour " + behaviourName + " in " + fileName);
            }
            return new BehaviourCycleStep(nameToAction.get(behaviourName));
        } else {
            throw new RuntimeException("Unknown type: " + listMapOrString.getClass());
        }
    }

    private Transitions createTransitions(String routineName, EntityData.RoutineData routineData) {
        ArrayList<ConditionTransition> conditionTransitions = new ArrayList<>();
        //only one NO_REPEAT
        String noRepeatGoToString = null;
        if (routineData.TRANSITIONS == null) {
            throw new RuntimeException("Incorrect TRANSITIONS definition in " + fileName);
        }
        //Multiple transitions per behaviour
        for (EntityData.RoutineData.TransitionData transitionData : routineData.TRANSITIONS) {
            if (transitionData.NO_REPEAT != null) {
                if (noRepeatGoToString != null) {
                    throw new RuntimeException("Multiple NoRepeat conditions specified in " + fileName);
                }
                noRepeatGoToString = transitionData.NO_REPEAT;
            } else if (transitionData.GO_TO != null) {

                //build conditionTransition
                Predicate<Entity> condition = loadCompoundCondition(transitionData);

                Object listMapOrString = transitionData.GO_TO;
                ConditionTransition conditionTransition = createConditionTransition(condition, listMapOrString, routineName);
                conditionTransitions.add(conditionTransition);
            } else {
                throw new RuntimeException("Must define a NO_REPEAT or a ON_CONDITION GO_TO in condition transition in " + routineName + " in " + fileName);
            }

        }

        return finalizeTransitions(noRepeatGoToString, conditionTransitions);
    }

    private Transitions finalizeTransitions(String noRepeatGoToString, ArrayList<ConditionTransition> conditionTransitions) {
        if (noRepeatGoToString != null && !conditionTransitions.isEmpty()) {
            return new Transitions(conditionTransitions, noRepeatGoToString);
        } else if (noRepeatGoToString != null) {
            return new Transitions(noRepeatGoToString);
        } else if (!conditionTransitions.isEmpty()) {
            return new Transitions(conditionTransitions);
        } else {
            throw new RuntimeException("Can't find any transition in " + fileName);
        }
    }

    @SuppressWarnings("unchecked")
    private ConditionTransition createConditionTransition(Predicate<Entity> condition, Object listMapOrString, String routineName) {
        if (listMapOrString instanceof String) {
            return new ConditionTransition(condition, (String) listMapOrString);
        } else if (listMapOrString instanceof java.util.Map) {
            // weighted random of routines
            java.util.Map<String, Object> weightedRandomDef = (java.util.Map<String, Object>) listMapOrString;
            // Build the transition with weighted map placeholder then bind routines
            ConditionTransition conditionTransition = new ConditionTransition(condition, weightedRandomDef);
            // Bind now using nameToRoutine
            java.util.LinkedHashMap<Routine, Float> weightedMap = new java.util.LinkedHashMap<>();
            float total = 0f;
            boolean foundElse = false;
            Routine elseRoutine = null;
            for (java.util.Map.Entry<String, Object> e : weightedRandomDef.entrySet()) {
                String routineTargetName = e.getKey();
                if (!nameToRoutine.containsKey(routineTargetName)) {
                    throw new RuntimeException("Referred to a nonexistent routine in TRANSITIONS: " + routineTargetName + " in " + fileName);
                }
                Routine r = nameToRoutine.get(routineTargetName);
                Object v = e.getValue();
                if (v instanceof String) {
                    String s = (String)v;
                    if (!s.equals("else")) {
                        throw new RuntimeException("Found string in TRANSITIONS:GO_TO:WEIGHTED_RANDOM in " + fileName + ". Only string allowed here is 'else'");
                    }
                    foundElse = true;
                    elseRoutine = r;
                } else {
                    Double d = (Double)v;
                    float p = d.floatValue();
                    total += p;
                    weightedMap.put(r, p);
                }
            }
            if (foundElse) {
                weightedMap.put(elseRoutine, 1 - total);
            }
            conditionTransition.bindWeightedRandom(weightedMap);
            return conditionTransition;
        } else if (listMapOrString instanceof java.util.List) {
            // uniform random of routines
            java.util.List<String> listDef = (java.util.List<String>) listMapOrString;
            ConditionTransition conditionTransition = new ConditionTransition(condition, listDef);
            java.util.List<Routine> targets = new java.util.ArrayList<>();
            for (String routineTargetName : listDef) {
                if (!nameToRoutine.containsKey(routineTargetName)) {
                    throw new RuntimeException("Referred to a nonexistent routine in TRANSITIONS: " + routineTargetName + " in " + fileName);
                }
                targets.add(nameToRoutine.get(routineTargetName));
            }
            conditionTransition.bindRandomList(targets);
            return conditionTransition;
        } else {
            throw new RuntimeException("Unsupported GO_TO type in TRANSITIONS for routine '" + routineName + "' in " + fileName);
        }
    }


    //routines must be loaded before ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO is loaded
    private void loadInterruptRoutines(Map<String, String> behaviourNameToInterruptRoutine, Map<String, Action> nameToAction) {
        for (Map.Entry<String, String> entry : behaviourNameToInterruptRoutine.entrySet()) {
            String behaviourName = entry.getKey();
            String routineName = entry.getValue();
            if (!nameToAction.containsKey(behaviourName)) {
                throw new RuntimeException("This error should not occur " + routineName + " " + behaviourName + " in " + fileName);
            }
            if (!nameToRoutine.containsKey(routineName)) {
                throw new RuntimeException("No such routine " + routineName + " for ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO in " + behaviourName + " in " + fileName);
            }

            Action action = nameToAction.get(behaviourName);
            action.ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO = nameToRoutine.get(routineName);
        }
    }

    private Predicate<Entity> loadCompoundCondition(EntityData.RoutineData.TransitionData transitionData) {
        return ConditionCompiler.compile(transitionData.ON_CONDITION);
    }

}
