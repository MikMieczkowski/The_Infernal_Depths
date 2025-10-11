package com.mikm.entityLoader;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.mikm.RandomUtils;
import com.mikm.entities.Entity;
import com.mikm.entities.actions.Action;
import com.mikm.input.GameInput;
import com.mikm.rendering.screens.Application;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;


public class Blackboard {
    private final Map<Entity, Map<String,Object>> perEntityValues = new HashMap<>();
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
    private final Map<String, java.util.function.Function<Entity, ?>> accessors = new HashMap<>();

    // Bind simple variable
    public <T> void bind(String key, java.util.function.Function<Entity, T> accessor) {
        accessors.put(key, accessor);
    }

    public Object getVar(Entity e, String key) {
        // check accessor first
        Function<Entity, ?> f = accessors.get(key);
        if (f != null) return f.apply(e);

        // then check per-entity values
        Map<String,Object> map = perEntityValues.get(e);
        if (map != null && map.containsKey(key)) return map.get(key);

        throw new IllegalArgumentException("No var bound for key " + key + " for entity " + e.NAME);
    }

    public boolean containsKey(Entity e, String key) {
        // check accessor first
        Function<Entity, ?> f = accessors.get(key);
        if (f != null) return true;

        // then check per-entity values
        Map<String,Object> map = perEntityValues.get(e);
        return map != null && map.containsKey(key);
    }

    // Bind function: (Entity, Object[] args) -> Object
    public void bindFunction(String name, FunctionDef fn) {
        functions.put(name, fn);
    }

    public Object callFunction(Entity e, String name, List<String> args) {
        FunctionDef f = functions.get(name);
        if (f == null) throw new IllegalArgumentException("No function bound for " + name);
        return f.invoke(e, args);
    }

    // bind per-entity static value
    public void bind(String key, Entity e, Object value) {
        perEntityValues
                .computeIfAbsent(e, k -> new HashMap<>())
                .put(key, value);
    }

    // Functional interface for bound functions
    @FunctionalInterface
    public interface FunctionDef {
        Object invoke(Entity e, List<String> args);
    }
}
