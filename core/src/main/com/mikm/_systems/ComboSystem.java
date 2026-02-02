package com.mikm._systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.mikm._components.AerialStateComponent;
import com.mikm._components.ComboStateComponent;
import com.mikm._components.Transform;
import com.mikm.entities.prefabLoader.attack.AttackFormattedData;
import com.mikm.entities.prefabLoader.weapon.AttackDuration;
import com.mikm.entities.prefabLoader.weapon.AttackNode;
import com.mikm.entities.prefabLoader.YAMLLoader;
import com.mikm.entities.prefabLoader.attack.AttackTransformers;
import com.mikm.rendering.screens.Application;

import java.util.List;

/**
 * System that handles combo tree traversal and attack execution.
 * Evaluates distance conditions and selects the appropriate attack.
 */
public class ComboSystem extends EntitySystem {

    private static final String ATTACK_SCHEMA = "combat/attacks/attack.yaml";

    @Override
    public void update(float deltaTime) {
        // Combo state updates are handled per-entity by AttackInputSystem
        // This system primarily provides the executeAttack method
    }

    /**
     * Executes an attack based on the current combo state and input.
     *
     * @param entity The attacking entity
     * @param duration The attack duration (LIGHT/MEDIUM/HEAVY)
     * @param distanceToEnemy The distance to the locked enemy
     */
    public void executeAttack(Entity entity, AttackDuration duration, float distanceToEnemy) {
        ComboStateComponent combo = ComboStateComponent.MAPPER.get(entity);
        if (combo == null) {
            return;
        }

        // Check if combo window is open
        if (!combo.isComboWindowOpen()) {
            return;
        }

        // Find the next attack node
        AttackNode nextNode = findNextNode(combo, duration, distanceToEnemy);

        if (nextNode == null) {
            combo.resetCombo();
            return;
        }

        // Load the attack data
        AttackFormattedData attackData = loadAttackData(nextNode.attackName);

        if (attackData == null) {
            attackData = loadDefaultAttackData();
        }

        // Start the attack
        combo.startAttack(nextNode, attackData);

        // Trigger the attack action
        triggerAttackAction(entity, combo, attackData);

        // Check for launcher attacks
        checkLauncher(entity, attackData);
    }

    /**
     * Finds the next attack node based on current state and input.
     */
    private AttackNode findNextNode(ComboStateComponent combo, AttackDuration duration, float distance) {
        List<AttackNode> root = combo.getCurrentRoot();

        if (root == null || root.isEmpty()) {
            return null;
        }

        // If at root (currentNode == null), search root level
        if (combo.currentNode == null) {
            for (AttackNode node : root) {
                if (node.duration == duration) {
                    // Evaluate condition
                    if (node.evaluateCondition(distance)) {
                        return node;
                    }
                }
            }
            return null;
        }

        // Otherwise, traverse from current node
        return combo.currentNode.getNextNode(duration, distance);
    }

    /**
     * Loads attack data from YAML.
     */
    private AttackFormattedData loadAttackData(String attackName) {
        try {
            // Register transformers if not already done
            AttackTransformers.register();

            String attackFile = "combat/attacks/" + attackName + ".yaml";
            return YAMLLoader.load(attackFile, ATTACK_SCHEMA,
                    AttackFormattedData.class);
        } catch (Exception e) {
            // Attack file doesn't exist, use schema defaults
            return null;
        }
    }

    /**
     * Loads the default attack data from schema.
     */
    private AttackFormattedData loadDefaultAttackData() {
        try {
            AttackTransformers.register();
            return YAMLLoader.load(ATTACK_SCHEMA, ATTACK_SCHEMA,
                    AttackFormattedData.class);
        } catch (Exception e) {
            // Create minimal default
            AttackFormattedData defaultData = new AttackFormattedData();
            defaultData.ATTACK_MAX_TIME = 0.5f;
            defaultData.COMBO_TIME = 0.6f;
            defaultData.DAMAGE_MULTIPLIER = 1.0f;
            return defaultData;
        }
    }

    /**
     * Triggers the attack action on the entity.
     */
    private void triggerAttackAction(Entity entity, ComboStateComponent combo, AttackFormattedData attackData) {
        Transform transform = Transform.MAPPER.get(entity);

        if (transform != null) {
            // Set attack direction toward locked enemy
            float angle = LockOnSystem.getAngleToLockedEnemy(entity);
            transform.direction = com.mikm.utils.ExtraMathUtils.angleToVector2Int(angle);
        }

        // The actual state transition to attacking is handled by the existing routine system
        // through transitions based on attack input. This method just configures the attack data.
        // The routine system will detect the attack input and transition to the attacking state.
    }

    /**
     * Checks if the attack is a launcher and handles aerial state transition.
     */
    private void checkLauncher(Entity entity, AttackFormattedData attackData) {
        if (attackData.isLauncher()) {
            // This will be used when the attack hits an enemy
            // The hit detection will check currentAttackData.isLauncher()
            // and call enterAerialCombo() on the player's combo state

            // For now, just mark the attack as a launcher for hit detection
        }
    }

    /**
     * Called when a launcher attack hits an enemy.
     * Triggers the aerial combo transition.
     *
     * @param attacker The attacking entity (player)
     * @param victim The entity that was hit
     */
    public void onLauncherHit(Entity attacker, Entity victim) {
        // Set victim's aerial state
        AerialStateComponent victimAerial = AerialStateComponent.MAPPER.get(victim);
        if (victimAerial != null) {
            victimAerial.launch(true);
        }

        // Switch attacker to aerial combo tree
        ComboStateComponent attackerCombo = ComboStateComponent.MAPPER.get(attacker);
        if (attackerCombo != null) {
            attackerCombo.enterAerialCombo();
        }
    }

    /**
     * Resets a combo to the root state.
     */
    public void resetCombo(Entity entity) {
        ComboStateComponent combo = ComboStateComponent.MAPPER.get(entity);
        if (combo != null) {
            combo.resetCombo();
        }
    }
}
