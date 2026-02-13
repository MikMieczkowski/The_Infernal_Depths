package com.mikm.entities.prefabLoader.weapon;

import com.mikm.entities.prefabLoader.FieldTransformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Transforms a raw COMBO_TREE map from YAML into a list of AttackNodes.
 *
 * Valid node formats:
 * <pre>
 * # Leaf node (no follow-ups):
 * LIGHT:
 *   ATTACK: swingRegular
 *
 * # Unconditional branching (always continues):
 * LIGHT:
 *   ATTACK: swingRegular
 *   THEN_NEXT:
 *     LIGHT:
 *       ATTACK: swing2
 *
 * # Shorthand for leaf follow-ups (equivalent to above):
 * LIGHT:
 *   ATTACK: swingRegular
 *   THEN_NEXT:
 *     LIGHT: swing2
 *
 * # Conditional branching (distance-based):
 * LIGHT:
 *   ATTACK: swingRegular
 *   IF_DISTANCE_IS: GREATER    # GREATER or LESS
 *   THAN: 30                   # distance threshold
 *   THEN_NEXT:
 *     LIGHT:
 *       ATTACK: closeSwing
 *   ELSE_NEXT:                 # optional
 *     LIGHT:
 *       ATTACK: farSwing
 * </pre>
 */
public class ComboTreeTransformer implements FieldTransformer<Map<String, Object>, List<AttackNode>> {

    @Override
    public List<AttackNode> transform(Map<String, Object> rawValue) {
        if (rawValue == null) {
            return new ArrayList<>();
        }
        return parseRootLevel(rawValue);
    }

    @SuppressWarnings("unchecked")
    private List<AttackNode> parseRootLevel(Map<String, Object> map) {
        List<AttackNode> nodes = new ArrayList<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String durationKey = entry.getKey();
            Object value = entry.getValue();

            if (!(value instanceof Map)) {
                continue;
            }

            Map<String, Object> nodeData = (Map<String, Object>) value;
            AttackNode node = parseNode(durationKey, nodeData);
            if (node != null) {
                nodes.add(node);
            }
        }

        return nodes;
    }

    @SuppressWarnings("unchecked")
    private AttackNode parseNode(String durationKey, Map<String, Object> nodeData) {
        // Parse duration
        AttackDuration duration;
        try {
            duration = AttackDuration.fromString(durationKey);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid attack duration '" + durationKey +
                    "'. Valid durations: LIGHT, HEAVY");
        }

        // Parse attack name (required)
        String attackName = (String) nodeData.get("ATTACK");
        if (attackName == null) {
            throw new IllegalArgumentException("Missing ATTACK field for duration " + durationKey);
        }

        // Check for known keys
        boolean hasCondition = nodeData.containsKey("IF_DISTANCE_IS");
        boolean hasThan = nodeData.containsKey("THAN");
        boolean hasThenNext = nodeData.containsKey("THEN_NEXT");
        boolean hasElseNext = nodeData.containsKey("ELSE_NEXT");

        // Check for old IF key usage
        if (nodeData.containsKey("IF")) {
            throw new IllegalArgumentException("'" + durationKey + " -> " + attackName +
                    "': 'IF' has been renamed to 'IF_DISTANCE_IS'. Please update your YAML.");
        }

        // Validate: IF_DISTANCE_IS requires THAN
        if (hasCondition && !hasThan) {
            throw new IllegalArgumentException("'" + durationKey + " -> " + attackName +
                    "': IF_DISTANCE_IS requires THAN (distance threshold).");
        }

        // Validate: THAN requires IF_DISTANCE_IS
        if (hasThan && !hasCondition) {
            throw new IllegalArgumentException("'" + durationKey + " -> " + attackName +
                    "': THAN requires IF_DISTANCE_IS (GREATER or LESS).");
        }

        // Validate: IF_DISTANCE_IS requires THEN_NEXT
        if (hasCondition && !hasThenNext) {
            throw new IllegalArgumentException("'" + durationKey + " -> " + attackName +
                    "': IF_DISTANCE_IS requires THEN_NEXT (branches to follow).");
        }

        // Validate: ELSE_NEXT requires IF_DISTANCE_IS
        if (hasElseNext && !hasCondition) {
            throw new IllegalArgumentException("'" + durationKey + " -> " + attackName +
                    "': ELSE_NEXT requires IF_DISTANCE_IS (no condition to branch on).");
        }

        // Parse condition (null if not present)
        AttackNode.DistanceCondition condition = null;
        float distanceThreshold = 0f;

        if (hasCondition) {
            String ifStr = (String) nodeData.get("IF_DISTANCE_IS");
            try {
                condition = AttackNode.DistanceCondition.valueOf(ifStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("'" + durationKey + " -> " + attackName +
                        "': Invalid IF_DISTANCE_IS value '" + ifStr + "'. Valid values: GREATER, LESS");
            }

            Object thanValue = nodeData.get("THAN");
            if (thanValue instanceof Number) {
                distanceThreshold = ((Number) thanValue).floatValue();
            } else {
                throw new IllegalArgumentException("'" + durationKey + " -> " + attackName +
                        "': THAN must be a number (distance threshold).");
            }
        }

        // Create the node
        AttackNode node = new AttackNode(duration, attackName, condition, distanceThreshold);

        // Parse THEN_NEXT branches
        if (hasThenNext) {
            Object thenNextObj = nodeData.get("THEN_NEXT");
            if (thenNextObj instanceof Map) {
                parseBranches(node, (Map<String, Object>) thenNextObj, true);
            }
        }

        // Parse ELSE_NEXT branches
        if (hasElseNext) {
            Object elseNextObj = nodeData.get("ELSE_NEXT");
            if (elseNextObj instanceof Map) {
                parseBranches(node, (Map<String, Object>) elseNextObj, false);
            }
        }

        return node;
    }

    /**
     * Parses branch entries (THEN_NEXT or ELSE_NEXT).
     * Supports both full node maps and shorthand strings:
     *   LIGHT: {ATTACK: swing2, ...}   (full node)
     *   LIGHT: swing2                  (shorthand for leaf with ATTACK: swing2)
     */
    @SuppressWarnings("unchecked")
    private void parseBranches(AttackNode parent, Map<String, Object> branchMap, boolean isThenNext) {
        for (Map.Entry<String, Object> childEntry : branchMap.entrySet()) {
            AttackNode childNode;
            if (childEntry.getValue() instanceof Map) {
                childNode = parseNode(childEntry.getKey(), (Map<String, Object>) childEntry.getValue());
            } else if (childEntry.getValue() instanceof String) {
                // Shorthand: "LIGHT: swingXL" → leaf node with just ATTACK
                AttackDuration childDuration;
                try {
                    childDuration = AttackDuration.fromString(childEntry.getKey());
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid attack duration '" + childEntry.getKey() +
                            "'. Valid durations: LIGHT, HEAVY");
                }
                childNode = new AttackNode(childDuration, (String) childEntry.getValue());
            } else {
                continue;
            }
            if (childNode != null) {
                if (isThenNext) {
                    parent.addThenNext(childNode);
                } else {
                    parent.addElseNext(childNode);
                }
            }
        }
    }
}
