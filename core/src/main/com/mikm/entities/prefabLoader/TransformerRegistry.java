package com.mikm.entities.prefabLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * Global registry for field transformers.
 * Transformers are registered by field name and are used to convert raw YAML values
 * into formatted values during YAML loading.
 */
public class TransformerRegistry {
    private static final Map<String, FieldTransformer<?, ?>> transformers = new HashMap<>();

    /**
     * Registers a transformer for a specific field name.
     *
     * @param fieldName   The YAML field name (e.g., "COMBO_TREE")
     * @param transformer The transformer to use for this field
     */
    public static void register(String fieldName, FieldTransformer<?, ?> transformer) {
        transformers.put(fieldName, transformer);
    }

    /**
     * Gets the transformer registered for a field name.
     *
     * @param fieldName The YAML field name
     * @return The transformer, or null if none registered
     */
    @SuppressWarnings("unchecked")
    public static <R, F> FieldTransformer<R, F> get(String fieldName) {
        return (FieldTransformer<R, F>) transformers.get(fieldName);
    }

    /**
     * Checks if a transformer is registered for a field name.
     *
     * @param fieldName The YAML field name
     * @return true if a transformer is registered
     */
    public static boolean hasTransformer(String fieldName) {
        return transformers.containsKey(fieldName);
    }

    /**
     * Removes a transformer registration.
     *
     * @param fieldName The YAML field name
     */
    public static void unregister(String fieldName) {
        transformers.remove(fieldName);
    }

    /**
     * Clears all registered transformers.
     */
    public static void clear() {
        transformers.clear();
    }
}
