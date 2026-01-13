package com.mikm.entities.prefabLoader;

import com.mikm._components.Transform;

import java.util.*;
import java.util.function.Function;


public class Blackboard {
    private final Map<com.badlogic.ashley.core.Entity, Map<String,Object>> perEntityValues = new HashMap<>();
    private static Blackboard instance;
    private Blackboard() {}

    public static Blackboard getInstance() {
        if (instance == null) {
            instance = new Blackboard();
            new BlackboardBindings();
        }
        return instance;
    }

    private final Map<String, FunctionDef> functions = new HashMap<>();
    private final Map<String, java.util.function.Function<com.badlogic.ashley.core.Entity, ?>> accessors = new HashMap<>();

    public <T> void bind(String key, java.util.function.Function<com.badlogic.ashley.core.Entity, T> accessor) {
        accessors.put(key, accessor);
    }

    public void bind(String key, com.badlogic.ashley.core.Entity e, Object value) {
        perEntityValues
                .computeIfAbsent(e, k -> new HashMap<>())
                .put(key, value);
    }


    public Object getVar(com.badlogic.ashley.core.Entity e, String key) {
        // check accessor first
        Function<com.badlogic.ashley.core.Entity, ?> f = accessors.get(key);
        if (f != null) return f.apply(e);

        // then check per-entity values
        Map<String,Object> map = perEntityValues.get(e);
        if (map != null && map.containsKey(key)) return map.get(key);

        throw new IllegalArgumentException("No var bound for key " + key + " for entity " + Transform.MAPPER.get(e).ENTITY_NAME);
    }


    public boolean containsKey(com.badlogic.ashley.core.Entity e, String key) {
        // check accessor first
        Function<com.badlogic.ashley.core.Entity, ?> f = accessors.get(key);
        if (f != null) return true;

        // then check per-entity values
        Map<String,Object> map = perEntityValues.get(e);
        return map != null && map.containsKey(key);
    }

    // Bind function: (Entity, Object[] args) -> Object
    public void bindFunction(String name, FunctionDef fn) {
        functions.put(name, fn);
    }

    public Object callFunction(com.badlogic.ashley.core.Entity e, String name, List<String> args) {
        FunctionDef f = functions.get(name);
        if (f == null) throw new IllegalArgumentException("No function bound for " + name);
        return f.invoke(e, args);
    }

    @FunctionalInterface
    public interface FunctionDef {
        Object invoke(com.badlogic.ashley.core.Entity e, List<String> args);
    }
}
