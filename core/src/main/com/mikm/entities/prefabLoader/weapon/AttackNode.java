package com.mikm.entities.prefabLoader.weapon;

import java.util.EnumMap;
import java.util.Map;

/**
 * A node in the combo tree representing a single attack.
 * Each node has a duration (light/medium/heavy), an attack name,
 * an optional distance condition, and branching maps for follow-up attacks.
 *
 * Valid node shapes:
 *   Leaf:         ATTACK only, no children
 *   Unconditional: ATTACK + THEN_NEXT (always follows thenNext)
 *   Conditional:   ATTACK + IF_DISTANCE_IS + THAN + THEN_NEXT, optionally ELSE_NEXT
 */
public class AttackNode {
    public final AttackDuration duration;
    public final String attackName;

    // Distance condition for branching (null = no condition, always use thenNext)
    public final DistanceCondition condition;
    public final float distanceThreshold;

    // Branches based on condition result
    public final Map<AttackDuration, AttackNode> thenNext;
    public final Map<AttackDuration, AttackNode> elseNext;

    /**
     * Distance condition types for combo branching.
     */
    public enum DistanceCondition {
        GREATER,  // Use thenNext if distance > threshold, else elseNext
        LESS      // Use thenNext if distance < threshold, else elseNext
    }

    public AttackNode(AttackDuration duration, String attackName) {
        this(duration, attackName, null, 0f);
    }

    public AttackNode(AttackDuration duration, String attackName, DistanceCondition condition, float distanceThreshold) {
        this.duration = duration;
        this.attackName = attackName;
        this.condition = condition;
        this.distanceThreshold = distanceThreshold;
        this.thenNext = new EnumMap<>(AttackDuration.class);
        this.elseNext = new EnumMap<>(AttackDuration.class);
    }

    public void addThenNext(AttackNode child) {
        thenNext.put(child.duration, child);
    }

    public void addElseNext(AttackNode child) {
        elseNext.put(child.duration, child);
    }

    /**
     * Gets the next attack node based on attack duration and distance to enemy.
     * If no condition is set, always uses thenNext.
     */
    public AttackNode getNextNode(AttackDuration attackDuration, float distanceToEnemy) {
        if (condition == null) {
            return thenNext.get(attackDuration);
        }

        boolean conditionPasses = evaluateCondition(distanceToEnemy);
        if (conditionPasses) {
            return thenNext.get(attackDuration);
        } else {
            return elseNext.get(attackDuration);
        }
    }

    /**
     * Evaluates the distance condition.
     * Only meaningful when condition is non-null.
     */
    public boolean evaluateCondition(float distance) {
        if (condition == null) {
            return true;
        }
        switch (condition) {
            case GREATER:
                return distance > distanceThreshold;
            case LESS:
                return distance < distanceThreshold;
            default:
                return true;
        }
    }

    public boolean isLeaf() {
        return thenNext.isEmpty() && elseNext.isEmpty();
    }

    public String getKeyName() {
        return duration.name().toLowerCase() + "_" + attackName;
    }

    @Override
    public String toString() {
        return "AttackNode{" + getKeyName() +
                (condition != null ? ", condition=" + condition + ", threshold=" + distanceThreshold : "") +
                ", thenNext=" + thenNext.size() +
                ", elseNext=" + elseNext.size() + "}";
    }
}
