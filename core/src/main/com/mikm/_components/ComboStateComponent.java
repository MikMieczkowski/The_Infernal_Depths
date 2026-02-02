package com.mikm._components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.mikm.entities.prefabLoader.attack.AttackFormattedData;
import com.mikm.entities.prefabLoader.weapon.AttackNode;

import java.util.List;

/**
 * Component for tracking combo state.
 * Manages current position in the combo tree and timing.
 */
public class ComboStateComponent implements Component {
    public static final ComponentMapper<ComboStateComponent> MAPPER = ComponentMapper.getFor(ComboStateComponent.class);

    /** Current position in the combo tree (null = at root, ready for first attack) */
    @CopyReference
    public AttackNode currentNode;

    /** Root of the grounded combo tree */
    @CopyReference
    public List<AttackNode> groundedRoot;

    /** Root of the aerial combo tree */
    @CopyReference
    public List<AttackNode> aerialRoot;

    /** Time since last attack ended */
    public float comboTimer;

    /** Whether currently using the aerial combo tree */
    public boolean inAerialCombo;

    /** Name of the current attack being executed */
    public String currentAttackName;

    /** Data for the current attack */
    @CopyReference
    public AttackFormattedData currentAttackData;

    /** Whether currently in an attack animation */
    public boolean isAttacking;

    /** Time remaining in current attack */
    public float attackTimer;

    /** The combo time window (loaded from attack data) */
    public float comboTimeWindow = 0.6f;

    /**
     * Resets combo state back to root.
     */
    public void resetCombo() {
        currentNode = null;
        comboTimer = 0f;
        inAerialCombo = false;
        currentAttackName = null;
        currentAttackData = null;
    }

    /**
     * Starts a new attack.
     *
     * @param node The attack node being executed
     * @param attackData The attack data
     */
    public void startAttack(AttackNode node, AttackFormattedData attackData) {
        currentNode = node;
        currentAttackName = node.attackName;
        currentAttackData = attackData;
        isAttacking = true;
        attackTimer = attackData.getAttackMaxTime();
        comboTimeWindow = attackData.getComboTime();
    }

    /**
     * Updates the combo timer after an attack ends.
     *
     * @param deltaTime Time since last frame
     */
    public void updateComboTimer(float deltaTime) {
        if (!isAttacking && currentNode != null) {
            comboTimer += deltaTime;
            if (comboTimer > comboTimeWindow) {
                resetCombo();
            }
        }
    }

    /**
     * Updates the attack timer during an attack.
     *
     * @param deltaTime Time since last frame
     * @return true if attack just ended
     */
    public boolean updateAttackTimer(float deltaTime) {
        if (isAttacking) {
            attackTimer -= deltaTime;
            if (attackTimer <= 0) {
                isAttacking = false;
                comboTimer = 0f;
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the combo window is still open for continuing the combo.
     *
     * @return true if within combo time window
     */
    public boolean isComboWindowOpen() {
        return !isAttacking && (currentNode == null || comboTimer < comboTimeWindow);
    }

    /**
     * Switches to aerial combo tree.
     */
    public void enterAerialCombo() {
        inAerialCombo = true;
        currentNode = null; // Reset to aerial root
    }

    /**
     * Gets the current combo tree root based on aerial state.
     *
     * @return The appropriate combo tree root
     */
    public List<AttackNode> getCurrentRoot() {
        return inAerialCombo ? aerialRoot : groundedRoot;
    }
}
