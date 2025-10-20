package com.mikm.entities.routineHandler;

import com.badlogic.gdx.math.MathUtils;
import com.mikm.RandomUtils;
import com.mikm.entities.Entity;

import java.util.*;
import java.util.function.Predicate;

public class ConditionTransition {
	public Predicate<Entity> pred;
	private Routine goTo;
	private Cycle current;
	private String goToString;

	// RANDOM target support
	private boolean isRandom;
	private List<Routine> randomRoutines; // uniform
	private LinkedHashMap<Routine, Float> weightedRandomRoutines; // preserves insertion order
	private float weightedRandomTotal;

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

	// copy constructor for safe deep copy when cloning routines
	public ConditionTransition(ConditionTransition other) {
		this.pred = other.pred;
		this.goToString = other.goToString;
		this.isRandom = other.isRandom;
		if (other.randomRoutines != null) {
			// Copy only routine names into lightweight stubs; rebind to actual routines in init(nameToRoutine)
			this.randomRoutines = new ArrayList<>();
			for (Routine r : other.randomRoutines) {
				Routine stub = new Routine();
				stub.name = r.name;
				this.randomRoutines.add(stub);
			}
		}
		if (other.weightedRandomRoutines != null) {
			// Preserve insertion order and probabilities, but avoid holding references to the old entity's routines
			this.weightedRandomRoutines = new LinkedHashMap<>();
			for (Map.Entry<Routine, Float> e : other.weightedRandomRoutines.entrySet()) {
				Routine stub = new Routine();
				stub.name = e.getKey().name;
				this.weightedRandomRoutines.put(stub, e.getValue());
			}
			this.weightedRandomTotal = other.weightedRandomTotal;
		}
	}

	public String getGoToString() {
		return goToString;
	}

	//First pass: collect routine names, then once all are loaded call init to update the string references into Routine references
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
			java.util.List<Routine> rebound = new java.util.ArrayList<>(randomRoutines.size());
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
			java.util.LinkedHashMap<Routine, Float> rebound = new java.util.LinkedHashMap<>();
			for (java.util.Map.Entry<Routine, Float> e : weightedRandomRoutines.entrySet()) {
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

	public Routine getGoTo() {
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

	public boolean getCondition(Entity entity) {
		return pred.test(entity);
	}

	// Used by loader after constructing this transition to bind names to routines
	public void bindRandomList(java.util.List<Routine> routines) {
		this.randomRoutines = routines;
	}
	public void bindWeightedRandom(java.util.LinkedHashMap<Routine, Float> map) {
		this.weightedRandomRoutines = map;
	}
}
