package com.mikm.entities.prefabLoader.weapon;

import com.mikm.entities.prefabLoader.FieldTransformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Transforms a raw COMBO_TREE map from YAML into a list of AttackNodes.
 *
 * New YAML structure:
 * <pre>
 * COMBO_TREE:
 *   LIGHT:
 *     ATTACK: swing1
 *     IF: ANY           # or GREATER, LESS
 *     THAN: 30          # distance threshold (only for GREATER/LESS)
 *     THEN_NEXT:        # branches when condition passes
 *       LIGHT:
 *         ATTACK: swing2
 *         IF: ANY
 *     ELSE_NEXT:        # branches when condition fails (optional)
 *       HEAVY:
 *         ATTACK: slam
 *         IF: ANY
 * </pre>
 *
 * Becomes a List<AttackNode> where each node has duration, attackName, condition,
 * and thenNext/elseNext branch maps.
 */
public class ComboTreeTransformer implements FieldTransformer<Map<String, Object>, List<AttackNode>> {

    @Override
    public List<AttackNode> transform(Map<String, Object> rawValue) {
        if (rawValue == null) {
            return new ArrayList<>();
        }
        return parseRootLevel(rawValue);
    }

    /**
     * Parses the root level of the combo tree.
     * At root level, keys are attack durations (LIGHT, MEDIUM, HEAVY).
     */
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

    /**
     * Parses a single attack node from its map representation.
     *
     * @param durationKey The duration key (LIGHT, MEDIUM, HEAVY)
     * @param nodeData The node's data map containing ATTACK, IF, THAN, THEN_NEXT, ELSE_NEXT
     * @return The parsed AttackNode
     */
    @SuppressWarnings("unchecked")
    private AttackNode parseNode(String durationKey, Map<String, Object> nodeData) {
        // Parse duration
        AttackDuration duration;
        try {
            duration = AttackDuration.fromString(durationKey);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid attack duration '" + durationKey +
                    "'. Valid durations: LIGHT, MEDIUM, HEAVY");
        }

        // Parse attack name
        String attackName = (String) nodeData.get("ATTACK");
        if (attackName == null) {
            throw new IllegalArgumentException("Missing ATTACK field for duration " + durationKey);
        }

        // Parse condition
        String ifStr = (String) nodeData.get("IF");
        AttackNode.DistanceCondition condition = AttackNode.DistanceCondition.ANY;
        if (ifStr != null) {
            try {
                condition = AttackNode.DistanceCondition.valueOf(ifStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid IF condition '" + ifStr +
                        "'. Valid conditions: ANY, GREATER, LESS");
            }
        }

        // Parse distance threshold
        float distanceThreshold = 0f;
        Object thanValue = nodeData.get("THAN");
        if (thanValue != null) {
            if (thanValue instanceof Number) {
                distanceThreshold = ((Number) thanValue).floatValue();
            }
        }

        // Create the node
        AttackNode node = new AttackNode(duration, attackName, condition, distanceThreshold);

        // Parse THEN_NEXT branches
        Object thenNextObj = nodeData.get("THEN_NEXT");
        if (thenNextObj instanceof Map) {
            Map<String, Object> thenNextMap = (Map<String, Object>) thenNextObj;
            for (Map.Entry<String, Object> childEntry : thenNextMap.entrySet()) {
                if (childEntry.getValue() instanceof Map) {
                    AttackNode childNode = parseNode(childEntry.getKey(), (Map<String, Object>) childEntry.getValue());
                    if (childNode != null) {
                        node.addThenNext(childNode);
                    }
                }
            }
        }

        // Parse ELSE_NEXT branches
        Object elseNextObj = nodeData.get("ELSE_NEXT");
        if (elseNextObj instanceof Map) {
            Map<String, Object> elseNextMap = (Map<String, Object>) elseNextObj;
            for (Map.Entry<String, Object> childEntry : elseNextMap.entrySet()) {
                if (childEntry.getValue() instanceof Map) {
                    AttackNode childNode = parseNode(childEntry.getKey(), (Map<String, Object>) childEntry.getValue());
                    if (childNode != null) {
                        node.addElseNext(childNode);
                    }
                }
            }
        }

        return node;
    }
}
