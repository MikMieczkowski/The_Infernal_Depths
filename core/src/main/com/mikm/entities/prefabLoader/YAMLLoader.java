package com.mikm.entities.prefabLoader;

import com.mikm.YamlCopyResolver;

import java.lang.reflect.Field;
import java.util.Map;

public class YAMLLoader {
    private static Map<String, Object> fileNameToFormattedYAMLObject;

    //No transformer, raw yaml object loading
    public static Object load(String instantiationFilename, String defaultsFilename, Class<?> rawYAML) {
        if (fileNameToFormattedYAMLObject.containsKey(instantiationFilename)) {
            return fileNameToFormattedYAMLObject.get(instantiationFilename);
        }
        Object defaults;
        if (fileNameToFormattedYAMLObject.containsKey(defaultsFilename)) {
            defaults = fileNameToFormattedYAMLObject.get(defaultsFilename);
        } else {
            defaults = YamlCopyResolver.loadAndResolve("yaml/" + defaultsFilename, rawYAML);
            fileNameToFormattedYAMLObject.put(defaultsFilename, defaults);
        }
        Object instantiation = YamlCopyResolver.loadAndResolve("yaml/" + instantiationFilename, rawYAML);
        return merge(defaults, instantiation);
    }

    //Formatted, usable object loading
    public static Object load(String instantiationFilename, String defaultsFilename, Class<?> rawYAML, Class<?> formattedYAML) {
        if (fileNameToFormattedYAMLObject.containsKey(instantiationFilename)) {
            return fileNameToFormattedYAMLObject.get(instantiationFilename);
        }
        Object defaults;
        if (fileNameToFormattedYAMLObject.containsKey(defaultsFilename)) {
            defaults = fileNameToFormattedYAMLObject.get(defaultsFilename);
        } else {
            defaults = loadAndTransformYAMLObject(defaultsFilename, rawYAML, formattedYAML);
            fileNameToFormattedYAMLObject.put(defaultsFilename, defaults);
        }

        Object instantiation = loadAndTransformYAMLObject(instantiationFilename, rawYAML, formattedYAML);
        return merge(defaults, instantiation);
    }


    //Create an instance of formattedYAML from the yaml file fileName, adding what is loaded to startingObject.
    private static <F> F loadAndTransformYAMLObject(String fileName, Class<?> rawYAML, Class<F> formattedYAML) {
        //load text file
        fileName = "yaml/" + fileName;
        Object rawData = YamlCopyResolver.loadAndResolve(fileName, rawYAML);
        //TODO run the transformers to go from rawYaml object to formattedYaml on each
        F output = null;
        //save to storage
        fileNameToFormattedYAMLObject.put(fileName, output);
        return output;
    }

    private static <T> T merge(T defaults, T instance) {
        if (instance == null) return defaults;

        Class<?> cls = defaults.getClass();

        try {
            for (Field f : cls.getDeclaredFields()) {
                f.setAccessible(true);

                Object v = f.get(instance);
                if (v != null) {
                    f.set(defaults, v);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return defaults;
    }
}
