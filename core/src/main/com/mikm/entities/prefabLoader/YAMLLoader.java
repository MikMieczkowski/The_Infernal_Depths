package com.mikm.entities.prefabLoader;

import com.mikm.YamlCopyResolver;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic YAML loader that supports schema defaults, instance overrides, and field transformation.
 *
 * Usage:
 * - Raw loading: YAMLLoader.load("copperSword.yaml", "weapon.yaml", WeaponRawData.class)
 * - Formatted loading: YAMLLoader.load("copperSword.yaml", "weapon.yaml", WeaponRawData.class, WeaponFormattedData.class)
 *
 * Flow:
 * 1. Load schema YAML -> COPY-resolved -> raw POJO (defaults)
 * 2. Load instance YAML -> COPY-resolved -> raw POJO (instance)
 * 3. Merge instance over schema (instance values override defaults)
 * 4. Transform merged raw POJO -> formatted POJO (if formattedClass provided)
 */
public class YAMLLoader {
    private static final Map<String, Object> cache = new HashMap<>();

    /**
     * Loads a YAML instance file with schema defaults, returning a raw POJO.
     *
     * @param instanceFilename The instance YAML file (e.g., "copperSword.yaml")
     * @param schemaFilename   The schema YAML file with defaults (e.g., "weapon.yaml")
     * @param rawClass         The raw POJO class to map to
     * @return The merged raw POJO with defaults filled in
     */
    @SuppressWarnings("unchecked")
    public static <R> R load(String instanceFilename, String schemaFilename, Class<R> rawClass) {
        String cacheKey = "raw:" + instanceFilename;
        if (cache.containsKey(cacheKey)) {
            return (R) cache.get(cacheKey);
        }

        R schema = loadRaw(schemaFilename, rawClass);
        R instance = loadRaw(instanceFilename, rawClass);
        R merged = merge(schema, instance, rawClass);

        cache.put(cacheKey, merged);
        return merged;
    }

    /**
     * Loads a YAML instance file with schema defaults, returning a formatted POJO.
     * Uses registered transformers to convert raw fields to formatted fields.
     * Fields without transformers are auto-copied if they exist in both classes.
     *
     * @param instanceFilename The instance YAML file (e.g., "copperSword.yaml")
     * @param schemaFilename   The schema YAML file with defaults (e.g., "weapon.yaml")
     * @param rawClass         The raw POJO class
     * @param formattedClass   The formatted POJO class
     * @return The formatted POJO with transformed fields
     */
    @SuppressWarnings("unchecked")
    public static <R, F> F load(String instanceFilename, String schemaFilename, Class<R> rawClass, Class<F> formattedClass) {
        String cacheKey = "formatted:" + instanceFilename;
        if (cache.containsKey(cacheKey)) {
            return (F) cache.get(cacheKey);
        }

        R mergedRaw = load(instanceFilename, schemaFilename, rawClass);
        F formatted = transform(mergedRaw, rawClass, formattedClass);

        cache.put(cacheKey, formatted);
        return formatted;
    }

    /**
     * Loads a single YAML file into a raw POJO (with COPY resolution).
     */
    private static <R> R loadRaw(String filename, Class<R> rawClass) {
        String cacheKey = "rawSingle:" + filename;
        if (cache.containsKey(cacheKey)) {
            return (R) cache.get(cacheKey);
        }

        R raw = YamlCopyResolver.loadAndResolve("yaml/" + filename, rawClass);
        cache.put(cacheKey, raw);
        return raw;
    }

    /**
     * Merges instance values over schema defaults.
     * Creates a new instance; does not mutate the inputs.
     *
     * @param schema   The schema POJO with default values
     * @param instance The instance POJO with override values
     * @param clazz    The POJO class
     * @return A new POJO with merged values
     */
    private static <R> R merge(R schema, R instance, Class<R> clazz) {
        if (instance == null) {
            return cloneShallow(schema, clazz);
        }
        if (schema == null) {
            return cloneShallow(instance, clazz);
        }

        try {
            R result = clazz.getDeclaredConstructor().newInstance();

            for (Field f : clazz.getDeclaredFields()) {
                f.setAccessible(true);

                Object instanceValue = f.get(instance);
                Object schemaValue = f.get(schema);

                // For Lists: merge each instance item with the schema's first item as defaults
                if (instanceValue instanceof List && schemaValue instanceof List) {
                    List<?> schemaList = (List<?>) schemaValue;
                    List<?> instanceList = (List<?>) instanceValue;
                    if (!schemaList.isEmpty() && !instanceList.isEmpty()) {
                        f.set(result, mergeListItems(schemaList, instanceList));
                        continue;
                    }
                }

                // For nested custom objects: merge field-by-field so partial overrides keep schema defaults
                if (instanceValue != null && schemaValue != null
                        && isCustomClass(instanceValue.getClass()) && isCustomClass(schemaValue.getClass())
                        && instanceValue.getClass().equals(schemaValue.getClass())) {
                    f.set(result, mergeObject(schemaValue, instanceValue));
                    continue;
                }

                // Instance value takes priority if non-null, otherwise use schema default
                Object valueToSet = (instanceValue != null) ? instanceValue : schemaValue;
                f.set(result, valueToSet);
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to merge YAML objects of type " + clazz.getName(), e);
        }
    }

    /**
     * Merges each item in the instance list with the schema's first list item as defaults.
     * For each instance item, any null field is filled from schema[0].
     */
    @SuppressWarnings("unchecked")
    private static List<Object> mergeListItems(List<?> schemaList, List<?> instanceList) {
        Object schemaDefault = schemaList.get(0);
        List<Object> result = new ArrayList<>();

        for (Object instanceItem : instanceList) {
            if (instanceItem == null || schemaDefault == null
                    || !instanceItem.getClass().equals(schemaDefault.getClass())) {
                result.add(instanceItem);
                continue;
            }
            result.add(mergeObject(schemaDefault, instanceItem));
        }
        return result;
    }

    /**
     * Merges two objects of the same class: instance fields override schema fields.
     */
    private static Object mergeObject(Object schema, Object instance) {
        Class<?> clazz = instance.getClass();
        try {
            Object merged = clazz.getDeclaredConstructor().newInstance();
            for (Field f : clazz.getDeclaredFields()) {
                f.setAccessible(true);
                Object instanceVal = f.get(instance);
                Object schemaVal = f.get(schema);
                f.set(merged, instanceVal != null ? instanceVal : schemaVal);
            }
            return merged;
        } catch (Exception e) {
            return instance;
        }
    }

    /**
     * Creates a shallow clone of a POJO.
     */
    private static <R> R cloneShallow(R source, Class<R> clazz) {
        if (source == null) return null;

        try {
            R clone = clazz.getDeclaredConstructor().newInstance();
            for (Field f : clazz.getDeclaredFields()) {
                f.setAccessible(true);
                f.set(clone, f.get(source));
            }
            return clone;
        } catch (Exception e) {
            throw new RuntimeException("Failed to clone YAML object of type " + clazz.getName(), e);
        }
    }

    /**
     * Transforms a raw POJO into a formatted POJO.
     * Uses registered transformers for fields that have them.
     * Auto-copies fields that exist in both classes without a transformer.
     *
     * @param raw            The raw POJO
     * @param rawClass       The raw POJO class
     * @param formattedClass The formatted POJO class
     * @return The formatted POJO
     */
    @SuppressWarnings("unchecked")
    private static <R, F> F transform(R raw, Class<R> rawClass, Class<F> formattedClass) {
        try {
            F formatted = formattedClass.getDeclaredConstructor().newInstance();

            // Build a map of raw field names to their values
            Map<String, Object> rawFieldValues = new HashMap<>();
            Map<String, Field> rawFields = new HashMap<>();
            for (Field f : rawClass.getDeclaredFields()) {
                f.setAccessible(true);
                rawFieldValues.put(f.getName(), f.get(raw));
                rawFields.put(f.getName(), f);
            }

            // Process each field in the formatted class
            for (Field formattedField : formattedClass.getDeclaredFields()) {
                formattedField.setAccessible(true);
                String fieldName = formattedField.getName();

                if (!rawFieldValues.containsKey(fieldName)) {
                    // Field doesn't exist in raw class, skip (leave as default)
                    continue;
                }

                Object rawValue = rawFieldValues.get(fieldName);
                Field rawField = rawFields.get(fieldName);

                if (TransformerRegistry.hasTransformer(fieldName)) {
                    // Use registered transformer
                    FieldTransformer<Object, Object> transformer = TransformerRegistry.get(fieldName);
                    Object transformedValue = transformer.transform(rawValue);
                    formattedField.set(formatted, transformedValue);
                } else {
                    // Auto-copy with deep copy support for nested objects
                    Object copiedValue = autoCopy(rawValue, formattedField.getType(), rawField, formattedField, rawClass, formattedClass);
                    if (copiedValue != null || rawValue == null) {
                        formattedField.set(formatted, copiedValue);
                    }
                }
            }

            return formatted;
        } catch (Exception e) {
            throw new RuntimeException("Failed to transform raw YAML to formatted type " + formattedClass.getName(), e);
        }
    }

    /**
     * Auto-copies a value to a target type, handling nested objects by field-by-field copy.
     *
     * @param rawValue       The source value
     * @param targetType     The target type
     * @param rawField       The raw field (for generic type info), may be null
     * @param formattedField The formatted field (for generic type info), may be null
     * @param rawClass       The raw POJO class (for finding inner classes)
     * @param formattedClass The formatted POJO class (for finding inner classes)
     * @return The copied value, or null if copy not possible
     */
    @SuppressWarnings("unchecked")
    private static Object autoCopy(Object rawValue, Class<?> targetType,
                                   Field rawField, Field formattedField,
                                   Class<?> rawClass, Class<?> formattedClass) {
        if (rawValue == null) {
            return null;
        }

        Class<?> rawType = rawValue.getClass();

        // Handle Map BEFORE checking assignability - map values may need conversion
        if (Map.class.isAssignableFrom(rawType) && Map.class.isAssignableFrom(targetType)) {
            return autoCopyMap((Map<?, ?>) rawValue, rawField, formattedField, rawClass, formattedClass);
        }

        // Handle List BEFORE checking assignability - list elements may need conversion
        if (List.class.isAssignableFrom(rawType) && List.class.isAssignableFrom(targetType)) {
            return autoCopyList((List<?>) rawValue, rawField, formattedField, rawClass, formattedClass);
        }

        // Direct assignment if types are compatible
        if (targetType.isAssignableFrom(rawType)) {
            return rawValue;
        }

        // Handle nested objects: copy field-by-field if both are custom classes
        if (isCustomClass(rawType) && isCustomClass(targetType)) {
            return copyFieldByField(rawValue, rawType, targetType);
        }

        // Try to find matching inner class by name
        if (isCustomClass(rawType)) {
            Class<?> matchingTargetType = findMatchingInnerClass(rawType, rawClass, formattedClass);
            if (matchingTargetType != null) {
                return copyFieldByField(rawValue, rawType, matchingTargetType);
            }
        }

        // Cannot auto-copy
        return null;
    }

    /**
     * Copies a map, converting value types if needed using generic type info.
     */
    @SuppressWarnings("unchecked")
    private static Object autoCopyMap(Map<?, ?> rawMap, Field rawField, Field formattedField,
                                      Class<?> rawClass, Class<?> formattedClass) {
        if (rawMap.isEmpty()) {
            return rawMap;
        }

        // Get the target value type from the formatted field's generic type
        Class<?> targetValueType = getMapValueType(formattedField);

        // Try to create a new map of the same type
        Map<Object, Object> newMap;
        try {
            newMap = (Map<Object, Object>) rawMap.getClass().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            newMap = new HashMap<>();
        }

        // Copy entries, converting value types if needed
        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();

            if (value == null) {
                newMap.put(key, null);
            } else if (isCustomClass(value.getClass())) {
                // Determine the target type for conversion
                Class<?> conversionTargetType = targetValueType;

                // If we couldn't get target type from generics, try matching inner class by name
                if (conversionTargetType == null) {
                    conversionTargetType = findMatchingInnerClass(value.getClass(), rawClass, formattedClass);
                }

                if (conversionTargetType != null && !conversionTargetType.equals(value.getClass())) {
                    // Convert to target value type
                    Object copiedValue = copyFieldByField(value, value.getClass(), conversionTargetType);
                    newMap.put(key, copiedValue != null ? copiedValue : value);
                } else {
                    // Same type or no conversion possible, keep as-is
                    newMap.put(key, value);
                }
            } else {
                newMap.put(key, value);
            }
        }

        return newMap;
    }

    /**
     * Copies a list, converting element types if needed using generic type info.
     */
    @SuppressWarnings("unchecked")
    private static Object autoCopyList(List<?> rawList, Field rawField, Field formattedField,
                                       Class<?> rawClass, Class<?> formattedClass) {
        if (rawList.isEmpty()) {
            return new ArrayList<>();
        }

        // Get the target element type from the formatted field's generic type
        Class<?> targetElementType = getListElementType(formattedField);

        List<Object> newList = new ArrayList<>();

        // Copy elements, converting types if needed
        for (Object element : rawList) {
            if (element == null) {
                newList.add(null);
            } else if (isCustomClass(element.getClass())) {
                // Determine the target type for conversion
                Class<?> conversionTargetType = targetElementType;

                // If we couldn't get target type from generics, try matching inner class by name
                if (conversionTargetType == null) {
                    conversionTargetType = findMatchingInnerClass(element.getClass(), rawClass, formattedClass);
                }

                if (conversionTargetType != null && !conversionTargetType.equals(element.getClass())) {
                    // Convert to target element type
                    Object copiedElement = copyFieldByField(element, element.getClass(), conversionTargetType);
                    newList.add(copiedElement != null ? copiedElement : element);
                } else {
                    // Same type or no conversion possible, keep as-is
                    newList.add(element);
                }
            } else {
                newList.add(element);
            }
        }

        return newList;
    }

    /**
     * Gets the element type of a List field from its generic type info.
     */
    private static Class<?> getListElementType(Field field) {
        if (field == null) return null;

        try {
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) genericType;
                Type[] typeArgs = paramType.getActualTypeArguments();
                if (typeArgs.length >= 1) {
                    Type elementType = typeArgs[0];
                    if (elementType instanceof Class) {
                        return (Class<?>) elementType;
                    } else if (elementType instanceof ParameterizedType) {
                        // Handle generic element types
                        Type rawType = ((ParameterizedType) elementType).getRawType();
                        if (rawType instanceof Class) {
                            return (Class<?>) rawType;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Ignore reflection errors
        }
        return null;
    }

    /**
     * Gets the value type of a Map field from its generic type info.
     */
    private static Class<?> getMapValueType(Field field) {
        if (field == null) return null;

        try {
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) genericType;
                Type[] typeArgs = paramType.getActualTypeArguments();
                if (typeArgs.length >= 2) {
                    Type valueType = typeArgs[1];
                    if (valueType instanceof Class) {
                        return (Class<?>) valueType;
                    } else if (valueType instanceof ParameterizedType) {
                        // Handle generic value types like List<String>
                        Type rawType = ((ParameterizedType) valueType).getRawType();
                        if (rawType instanceof Class) {
                            return (Class<?>) rawType;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Ignore reflection errors
        }
        return null;
    }

    /**
     * Finds a matching inner class in the formatted class based on simple name.
     * E.g., finds WeaponFormattedData$AttackConfigData for WeaponRawData$AttackConfigData
     */
    private static Class<?> findMatchingInnerClass(Class<?> rawInnerClass, Class<?> rawClass, Class<?> formattedClass) {
        if (rawClass == null || formattedClass == null) return null;

        String simpleName = rawInnerClass.getSimpleName();

        for (Class<?> innerClass : formattedClass.getDeclaredClasses()) {
            if (innerClass.getSimpleName().equals(simpleName)) {
                return innerClass;
            }
        }
        return null;
    }

    /**
     * Copies an object field-by-field from one type to another.
     */
    private static Object copyFieldByField(Object source, Class<?> sourceType, Class<?> targetType) {
        try {
            Object target = targetType.getDeclaredConstructor().newInstance();

            for (Field targetField : targetType.getDeclaredFields()) {
                targetField.setAccessible(true);
                String fieldName = targetField.getName();

                // Find matching field in source
                Field sourceField = null;
                try {
                    sourceField = sourceType.getDeclaredField(fieldName);
                    sourceField.setAccessible(true);
                } catch (NoSuchFieldException e) {
                    // Field doesn't exist in source, skip
                    continue;
                }

                Object sourceValue = sourceField.get(source);
                Object copiedValue = autoCopy(sourceValue, targetField.getType(), sourceField, targetField, sourceType, targetType);

                if (copiedValue != null || sourceValue == null) {
                    targetField.set(target, copiedValue);
                }
            }

            return target;
        } catch (Exception e) {
            // Cannot copy field-by-field
            return null;
        }
    }

    /**
     * Checks if a class is a custom/user-defined class (not a primitive, wrapper, or common JDK type).
     */
    private static boolean isCustomClass(Class<?> clazz) {
        if (clazz.isPrimitive()) return false;
        if (clazz.isArray()) return false;
        if (clazz.isEnum()) return false;

        String name = clazz.getName();
        if (name.startsWith("java.")) return false;
        if (name.startsWith("javax.")) return false;

        return true;
    }

    /**
     * Clears the cache. Useful for reloading YAML files during development.
     */
    public static void clearCache() {
        cache.clear();
    }
}
