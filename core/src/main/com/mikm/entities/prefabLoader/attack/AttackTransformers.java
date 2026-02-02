package com.mikm.entities.prefabLoader.attack;

/**
 * Registers all transformers needed for attack YAML loading.
 * Call {@link #register()} before loading any attack YAML files.
 */
public class AttackTransformers {
    private static boolean registered = false;

    /**
     * Registers all attack-related transformers.
     * Safe to call multiple times; only registers once.
     * Currently no custom transformers needed - attack data uses direct mapping.
     */
    public static void register() {
        if (registered) {
            return;
        }

        // No custom transformers needed for attacks - the data maps directly
        // If future transformations are needed, register them here:
        // TransformerRegistry.register("FIELD_NAME", new CustomTransformer());

        registered = true;
    }
}
