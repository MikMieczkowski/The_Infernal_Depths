package com.mikm.entities.prefabLoader.weapon;

import com.mikm.entities.prefabLoader.TransformerRegistry;

/**
 * Registers all transformers needed for weapon YAML loading.
 * Call {@link #register()} before loading any weapon YAML files.
 */
public class WeaponTransformers {
    private static boolean registered = false;

    /**
     * Registers all weapon-related transformers.
     * Safe to call multiple times; only registers once.
     */
    public static void register() {
        if (registered) {
            return;
        }

        ComboTreeTransformer comboTreeTransformer = new ComboTreeTransformer();

        // Both COMBO_TREE and AERIAL_COMBO_TREE use the same transformation
        TransformerRegistry.register("COMBO_TREE", comboTreeTransformer);
        TransformerRegistry.register("AERIAL_COMBO_TREE", comboTreeTransformer);

        registered = true;
    }
}
