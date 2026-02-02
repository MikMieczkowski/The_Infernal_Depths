package com.mikm.entities.prefabLoader.weapon;

import java.util.EnumMap;
import java.util.Map;

/**
 * A node in the combo tree representing a single attack.
 * Each node has a duration (light/medium/heavy), an attack name,
 * a distance condition, and branching maps for follow-up attacks.
 */
public class AttackNode {
    public final AttackDuration duration;
    public final String attackName;

    // Distance condition for this node
    public final DistanceCondition condition;
    public final float distanceThreshold;

    // Branches based on condition result
    public final Map<AttackDuration, AttackNode> thenNext;
    public final Map<AttackDuration, AttackNode> elseNext;

    /**
     * Distance condition types for combo branching.
     */
    public enum DistanceCondition {
        ANY,      // Always use thenNext
        GREATER,  // Use thenNext if distance > threshold, else elseNext
        LESS      // Use thenNext if distance < threshold, else elseNext
    }

    public AttackNode(AttackDuration duration, String attackName) {
        this(duration, attackName, DistanceCondition.ANY, 0f);
    }

    public AttackNode(AttackDuration duration, String attackName, DistanceCondition condition, float distanceThreshold) {
        this.duration = duration;
        this.attackName = attackName;
        this.condition = condition;
        this.distanceThreshold = distanceThreshold;
        this.thenNext = new EnumMap<>(AttackDuration.class);
        this.elseNext = new EnumMap<>(AttackDuration.class);
    }

    /**
     * Adds a child to the thenNext branch (for when condition passes).
     *
     * @param child The child node to add
     */
    public void addThenNext(AttackNode child) {
        thenNext.put(child.duration, child);
    }

    /**
     * Adds a child to the elseNext branch (for when condition fails).
     *
     * @param child The child node to add
     */
    public void addElseNext(AttackNode child) {
        elseNext.put(child.duration, child);
    }

    /**
     * Gets the next attack node based on attack duration and distance to enemy.
     *
     * @param attackDuration The duration of the input attack
     * @param distanceToEnemy The distance to the locked enemy
     * @return The next attack node, or null if no matching branch exists
     */
    public AttackNode getNextNode(AttackDuration attackDuration, float distanceToEnemy) {
        boolean conditionPasses = evaluateCondition(distanceToEnemy);

        if (conditionPasses) {
            return thenNext.get(attackDuration);
        } else {
            return elseNext.get(attackDuration);
        }
    }

    /**
     * Evaluates the distance condition.
     *
     * @param distance The distance to the enemy
     * @return true if condition passes (use thenNext), false otherwise (use elseNext)
     */
    public boolean evaluateCondition(float distance) {
        switch (condition) {
            case ANY:
                return true;
            case GREATER:
                return distance > distanceThreshold;
            case LESS:
                return distance < distanceThreshold;
            default:
                return true;
        }
    }

    /**
     * Checks if this is a leaf node (no follow-up attacks in either branch).
     *
     * @return true if this node has no children in any branch
     */
    public boolean isLeaf() {
        return thenNext.isEmpty() && elseNext.isEmpty();
    }

    /**
     * Gets the full key name as it appears in YAML (e.g., "light_swing1").
     *
     * @return The combined duration and attack name
     */
    public String getKeyName() {
        return duration.name().toLowerCase() + "_" + attackName;
    }

    @Override
    public String toString() {
        return "AttackNode{" + getKeyName() +
                ", condition=" + condition +
                (condition != DistanceCondition.ANY ? ", threshold=" + distanceThreshold : "") +
                ", thenNext=" + thenNext.size() +
                ", elseNext=" + elseNext.size() + "}";
    }
}
