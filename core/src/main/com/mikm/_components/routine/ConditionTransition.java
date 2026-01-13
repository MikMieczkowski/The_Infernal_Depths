package com.mikm._components.routine;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.mikm.utils.RandomUtils;
import com.mikm._components.CopyReference;
import com.mikm._components.Copyable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ConditionTransition implements Transition{
    @Copyable private Routine goTo;
    @Copyable private boolean isRandom;
    @Copyable private List<Routine> randomRoutines; // uniform
    @Copyable private LinkedHashMap<Routine, Float> weightedRandomRoutines; // preserves insertion order
    @Copyable private float weightedRandomTotal;

    @CopyReference private Predicate<Entity> pred;
    @Copyable private String goToString;

    public ConditionTransition() {

    }

    public ConditionTransition(Predicate<Entity> pred, String goToString) {
        this.pred = pred;
        this.goToString = goToString;
    }

    // New constructor for RANDOM targets parsed by loader
    public ConditionTransition(Predicate<Entity> pred, List<String> randomGoToList) {
        this.pred = pred;
        this.isRandom = true;
        this.goToString = null;
        this.randomRoutines = new ArrayList<>();
        this.weightedRandomRoutines = null;
    }

    public ConditionTransition(Predicate<Entity> pred, Map<String, Object> weightedRandomMap) {
        this.pred = pred;
        this.isRandom = true;
        this.goToString = null;
        this.randomRoutines = null;
        this.weightedRandomRoutines = new LinkedHashMap<>();
    }

    @Override
    public Routine getGoToRoutine() {
        if (!isRandom) {
            if (goTo == null) {
                throw new RuntimeException("Must first call init before getGoTo()");
            }
            return goTo;
        }
        // RANDOM selection at runtime
        if (randomRoutines != null) {
            int idx = RandomUtils.getInt(randomRoutines.size()-1);
            System.out.println("uniform " + randomRoutines.get(idx).name);
            return randomRoutines.get(idx);
        }
        if (weightedRandomRoutines != null) {
            int r = RandomUtils.getInt(1, 100);
            for (Map.Entry<Routine, Float> entry : weightedRandomRoutines.entrySet()) {
                int probability = MathUtils.round(entry.getValue() * 100);
                if (r <= probability) {
                    System.out.println("weighted " + entry.getKey().name);
                    return entry.getKey();
                }
                r -= probability;
            }
            throw new RuntimeException("Error selecting weighted random routine");
        }
        throw new RuntimeException("Random transition not initialized");
    }

    public void init(Map<String, Routine> nameToRoutine) {
        if (!isRandom) {
            if (!nameToRoutine.containsKey(goToString)) {
                throw new RuntimeException("CAN NOT GO TO ROUTINE " + goToString + " in condition transition");
            }
            goTo = nameToRoutine.get(goToString);
            return;
        }
        // RANDOM: rebind targets to this entity's routines using routine names
        if (randomRoutines != null) {
            List<Routine> rebound = new ArrayList<>(randomRoutines.size());
            for (Routine r : randomRoutines) {
                Routine mapped = nameToRoutine.get(r.name);
                if (mapped == null) {
                    throw new RuntimeException("CAN NOT REBIND RANDOM ROUTINE " + r.name + " in condition transition");
                }
                rebound.add(mapped);
            }
            randomRoutines = rebound;
            return;
        }
        if (weightedRandomRoutines != null) {
            LinkedHashMap<Routine, Float> rebound = new LinkedHashMap<>();
            for (Map.Entry<Routine, Float> e : weightedRandomRoutines.entrySet()) {
                Routine mapped = nameToRoutine.get(e.getKey().name);
                if (mapped == null) {
                    throw new RuntimeException("CAN NOT REBIND WEIGHTED RANDOM ROUTINE " + e.getKey().name + " in condition transition");
                }
                rebound.put(mapped, e.getValue());
            }
            weightedRandomRoutines = rebound;
            return;
        }
    }


    @Override
    public boolean shouldEnter(Entity entity, Routine currentRoutine) {
        return pred.test(entity);
    }

    // Used by loader after constructing this transition to bind names to routines
    public void bindRandomList(List<Routine> routines) {
        this.randomRoutines = routines;
    }
    public void bindWeightedRandom(LinkedHashMap<Routine, Float> map) {
        this.weightedRandomRoutines = map;
    }
}
