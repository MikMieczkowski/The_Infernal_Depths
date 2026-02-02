package com.mikm.entities.prefabLoader;

/**
 * Interface for transforming a raw YAML field value into a formatted value.
 *
 * @param <R> The raw type from the YAML POJO (e.g., Map<String, Object>)
 * @param <F> The formatted type for the output POJO (e.g., Tree<AttackNode>)
 */
@FunctionalInterface
public interface FieldTransformer<R, F> {
    /**
     * Transforms a raw value into a formatted value.
     *
     * @param rawValue The raw value from the YAML POJO
     * @return The transformed formatted value
     */
    F transform(R rawValue);
}
