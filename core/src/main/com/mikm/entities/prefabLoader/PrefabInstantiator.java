package com.mikm.entities.prefabLoader;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mikm.Vector2Int;
import com.mikm._components.*;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm.entities.actions.ChargeAction;
import com.mikm.entities.actions.OrbitPlayerAction;
import com.mikm.entities.animation.SingleFrame;
import com.mikm.entities.inanimateEntities.particles.ParticleTypes;
import com.mikm.input.GameInput;
import com.mikm.rendering.cave.RockType;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.GameScreen;
import com.mikm.serialization.Serializer;
import com.mikm.utils.RandomUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class PrefabInstantiator {
    private static Map<String, PrefabInstantiator.EntityInitFunction> entityNameToActionsInit = new HashMap<>();
    private static Map<String, Prefab> nameToPrefab = new HashMap<>();

    private PrefabInstantiator() {
    }

    static {
        new PrefabBinding(entityNameToActionsInit);
    }

    public static void addPrefab(String name, Iterable<Component> components) {
        if (nameToPrefab.containsKey(name)) {
            return;
        }
        nameToPrefab.put(name, new Prefab(components));
    }

    //Creates entity from prefab with all @Copyable or @CopyReference fields copied over
    public static Entity addEntity(String prefabName, GameScreen screen) {
        return addEntity(prefabName, screen, 0, 0);
    }

    public static Entity addEntity(String prefabName, GameScreen screen, Vector2 pos) {
        return addEntity(prefabName, screen, pos.x, pos.y);
    }

    public static Entity addEntity(String prefabName, GameScreen screen, Vector2Int pos) {
        return addEntity(prefabName, screen, pos.x, pos.y);
    }

    public static Entity addEntity(String prefabName, GameScreen screen, float x, float y) {
        Entity e = instantiatePrefab(prefabName, screen, x, y);
        screen.engine.addEntity(e);
        return e;
    }

    public static Entity instantiatePrefab(String prefabName, GameScreen screen, float x, float y) {
        if (!nameToPrefab.containsKey(prefabName)) {
            throw new RuntimeException("Prefab " + prefabName + " does not exist");
        }
        EntityInitFunction action = null;
        if (entityNameToActionsInit.containsKey(prefabName)) {
            action = entityNameToActionsInit.get(prefabName);
        }

        Entity e = screen.engine.createEntity();
        boolean hasSpawnComp = false;
        for (Component c : nameToPrefab.get(prefabName).components) {
            if (c instanceof SpawnComponent) {
                hasSpawnComp = true;
            }
            Component cloned;
            try {
                cloned = cloneRecursive(c);
            } catch (Exception ex) {

                ex.printStackTrace();
                throw new RuntimeException("Could not clone component " + c.getClass().getSimpleName() + " for entity " + prefabName + ". " + ex);
            }
            e.add(cloned);
        }
        if (action != null) {
            action.execute(e);
        }


        Transform t = Transform.MAPPER.get(e);
        t.x = x;
        t.y = y;
        return e;
    }


    //Creates entity from prefab with all fields copied over
    public static Entity addEntityExact(String prefabName, GameScreen screen) {
        Entity e = screen.engine.createEntity();
        for (Component c : nameToPrefab.get(prefabName).components) {
            e.add(Serializer.getInstance().copy(c));
            //cloneComp.runtimeInit()?
        }
        return e;
    }

    public static boolean prefabExists(String name) {
        return nameToPrefab.containsKey(name);
    }


    //------------Prefab Loading Helpers------------

    public static Entity addTestObject(GameScreen screen, int x, int y, Color color) {
        Entity e = addEntity("testObject", screen, x, y);
        SpriteComponent.MAPPER.get(e).color = color;
        return e;
    }

    public static void addAfterImage(Entity entity) {
        Transform transform = Transform.MAPPER.get(entity);
        SpriteComponent spriteComponent = SpriteComponent.MAPPER.get(entity);

        Entity afterImageEffect = Application.getInstance().currentScreen.engine.createEntity();
        Transform afterImageTransform = new Transform(transform.x, transform.y + transform.height);
        afterImageTransform.xScale = transform.xScale;
        afterImageTransform.yScale = transform.yScale;

        SpriteComponent afterImageSpriteComponent = new SpriteComponent(spriteComponent.textureRegion);
        afterImageEffect.add(spriteComponent);
        afterImageEffect.add(afterImageTransform);
        Application.getInstance().currentScreen.engine.addEntity(afterImageEffect);
    }

    public static void addIndicator(GameScreen screen) {
        final int INDICATOR_OFFSET_X = 8;
        final int INDICATOR_OFFSET_Y = 20;

        Entity indicator = screen.engine.createEntity();
        indicator.add(new Transform());
        indicator.add(new SpriteComponent(GameInput.getTalkButtonImage()));
        indicator.add(new FollowComponent(screen.playerTransform, INDICATOR_OFFSET_X, INDICATOR_OFFSET_Y));
        Application.getInstance().currentScreen.engine.addEntity(indicator);
    }

    public static void addDoor(GameScreen screen, float x, float y, int goToScreen) {
        Entity e = addEntity("door", screen, x, y);
        TriggerComponent.MAPPER.get(e).goToScreenTriggerActionScreen = goToScreen;
    }

    public static void addGrave(GameScreen screen) {
        //TODO particles
//        new ParticleEffect(ParticleTypes.getRockParameters(RockType.NORMAL), x, y);
//        new ParticleEffect(ParticleTypes.getRockParameters(RockType.NORMAL), x, y);
//        new ParticleEffect(ParticleTypes.getRockParameters(RockType.NORMAL), x, y);
        Entity e = addEntity("grave", screen, Application.getInstance().getPlayerX(), Application.getInstance().getPlayerY());
        GraveComponent graveComponent = GraveComponent.MAPPER.get(e);
        for (int i = 0; i < RockType.SIZE; i++) {
            graveComponent.ores[i] = RockType.get(i).tempOreAmount;
            System.out.println(graveComponent.ores[i]);
            RockType.get(i).increaseOreAmount(-RockType.get(i).tempOreAmount);
            RockType.get(i).tempOreAmount = 0;
        }
    }

    public static void addRock(GameScreen screen, float x, float y, RockType rockType) {
        Entity e = addEntity("destructible", screen, x, y);
        RockComponent.MAPPER.get(e).rockType = rockType;
        SpriteComponent.MAPPER.get(e).textureRegion = rockType.getRockImage();
    }

    public static void addParticles(float x, float y, ParticleTypes particleTypes) {
        addParticles(Application.getInstance().currentScreen, x, y, 0, particleTypes);
    }

    public static void addParticles(GameScreen screen, float x, float y, ParticleTypes particleTypes) {
        addParticles(screen, x, y, 0, particleTypes);
    }

    public static void addParticles(GameScreen screen, float x, float y, float angleOffset, ParticleTypes parameters) {
        int amount = RandomUtils.getInt(parameters.amountMin, parameters.amountMax);

        for (int i = 0; i < amount; i++) {
            //create a particle entity with random offset, startSize, start and end color, angle, and speed

            //offset position
            final float offsetAngle = RandomUtils.getFloat(0, MathUtils.PI2);
            final float xOffset = MathUtils.cos(offsetAngle) * RandomUtils.getFloat(0, parameters.positionOffsetRadius);
            final float yOffset = MathUtils.sin(offsetAngle) * RandomUtils.getFloat(0, parameters.positionOffsetRadius);

            Entity particle = instantiatePrefab("particle", screen, x + xOffset, y + yOffset);
            EffectsComponent effectsComponent = EffectsComponent.MAPPER.get(particle);
            effectsComponent.startSizeChange(parameters.maxLifeTime, parameters.sizeMin, parameters.sizeMax);
            if (parameters.usesColor) {
                effectsComponent.startColorChange(parameters.maxLifeTime, RandomUtils.getColor(parameters.startColorMin, parameters.startColorMax),
                        RandomUtils.getColor(parameters.endColorMin, parameters.endColorMax));
            }
            if (parameters.hasGravity) {
                effectsComponent.startBouncing(parameters.maxLifeTime, .1f, parameters.peakHeight);
            }

            //action stuff - speed and angle
            RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(particle);
            Transform.MAPPER.get(particle).yVel = 1;
            ChargeAction action = ChargeAction.simpleMoveTowardsAngle(.5f);
            routineListComponent.initRoutines(action, particle, new SingleFrame(parameters.image));

            screen.engine.addEntity(particle);
        }
    }

    //probably a ProjectileParameters class
    public static void addProjectile(float x, float y) {
        addProjectile(Application.getInstance().currentScreen, x, y);
    }
    public static void addProjectile(GameScreen screen, float x, float y) {
        Entity projectile = instantiatePrefab("projectile", screen, x, y);

        Transform transform = Transform.MAPPER.get(projectile);
        //temp, replace with projectileparameters
        transform.yVel = -1;

        RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(projectile);
        //temp speed
        ChargeAction action = ChargeAction.simpleMoveTowardsAngle(.5f);
        routineListComponent.initRoutines(action, projectile, new SingleFrame("sand"));

        ProjectileComponent p = new ProjectileComponent();
        p.isPlayer = true;
        projectile.add(p);
        screen.engine.addEntity(projectile);
    }

    public static void addPlayerWeapon(GameScreen screen) {
        Entity weapon = instantiatePrefab("playerWeapon", screen, 0, 0);

        RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(weapon);

        OrbitPlayerAction action = new OrbitPlayerAction();
        routineListComponent.initRoutines(action, weapon, new SingleFrame("sand"));

        screen.engine.addEntity(weapon);
    }

    //EntityInitFunction - code which must be done before adding the entity to the ECS but after creating the entity
    @FunctionalInterface
    public static interface EntityInitFunction {
        void execute(Entity entity);
    }


    @SuppressWarnings("unchecked")
    private static <T> T cloneRecursive(T object) {
        return cloneRecursive(object, new IdentityHashMap<>());
    }

    @SuppressWarnings("unchecked")
    private static <T> T cloneRecursive(T object, IdentityHashMap<Object, Object> clonedObjects) {
        boolean LOG = false; // turn on for debug
        if (object == null) {
            if (LOG) System.out.println("cloneRecursive: object is null → returning null");
            return null;
        }

        if (clonedObjects.containsKey(object)) {
            if (LOG) System.out.println("cloneRecursive: found existing clone for " + object.getClass().getName());
            return (T) clonedObjects.get(object);
        }

        Class<?> clazz = object.getClass();
        if (LOG) System.out.println("Cloning instance of " + clazz.getName());

        // Primitive / immutable
        if (clazz.isPrimitive() ||
                object instanceof String ||
                object instanceof Number ||
                object instanceof Boolean ||
                object instanceof Character) {
            if (LOG) System.out.println("→ Primitive/immutable type: returning same instance");
            return object;
        }

        // Class-level @CopyReference
        if (clazz.getDeclaredAnnotation(CopyReference.class) != null) {
            if (LOG) System.out.println("→ @CopyReference class: returning reference");
            return object;
        }

        try {
            // Arrays
            if (clazz.isArray()) {
                int length = Array.getLength(object);
                if (LOG) System.out.println("→ Cloning array length " + length);

                Object clonedArray = Array.newInstance(clazz.getComponentType(), length);
                clonedObjects.put(object, clonedArray);

                for (int i = 0; i < length; i++) {
                    Object elem = Array.get(object, i);
                    if (LOG) System.out.println("   Array element[" + i + "]: " + (elem != null ? elem.getClass().getSimpleName() : "null"));
                    Array.set(clonedArray, i, cloneRecursive(elem, clonedObjects));
                }

                return (T) clonedArray;
            }

            // List
            if (object instanceof List<?>) {
                List<?> list = (List<?>) object;
                if (LOG) System.out.println("→ Cloning List size " + list.size());

                List<Object> clonedList = object instanceof ArrayList ? new ArrayList<>() : new ArrayList<>();
                clonedObjects.put(object, clonedList);

                for (Object elem : list)
                    clonedList.add(cloneRecursive(elem, clonedObjects));

                return (T) clonedList;
            }

            // Set
            if (object instanceof Set<?>) {
                Set<?> set = (Set<?>) object;
                if (LOG) System.out.println("→ Cloning Set size " + set.size());

                Set<Object> clonedSet = new HashSet<>();
                clonedObjects.put(object, clonedSet);

                for (Object elem : set)
                    clonedSet.add(cloneRecursive(elem, clonedObjects));

                return (T) clonedSet;
            }

            // Map
            if (object instanceof Map<?, ?>) {
                Map<?, ?> map = (Map<?, ?>) object;
                if (LOG) System.out.println("→ Cloning Map size " + map.size());

                Map<Object, Object> clonedMap =
                        object instanceof LinkedHashMap ? new LinkedHashMap<>() : new HashMap<>();

                clonedObjects.put(object, clonedMap);

                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (LOG) System.out.println("   Cloning key/value: " + entry.getKey() + " → " + entry.getValue());
                    clonedMap.put(
                            cloneRecursive(entry.getKey(), clonedObjects),
                            cloneRecursive(entry.getValue(), clonedObjects)
                    );
                }

                return (T) clonedMap;
            }

            // Construct new instance
            Constructor<?> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            T copy = (T) ctor.newInstance();
            clonedObjects.put(object, copy);

            if (LOG) System.out.println("→ Instantiated new " + clazz.getSimpleName());

            // Traverse class hierarchy
            for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {

                if (LOG) System.out.println("   Inspecting class: " + c.getName());

                Field[] fields = c.getDeclaredFields();

                boolean hasAnnotations = false;
                for (Field f : fields) {
                    if (f.getAnnotation(Copyable.class) != null ||
                            f.getAnnotation(CopyReference.class) != null
                            || clazz.getAnnotation(RuntimeDataComponent.class) != null) {
                        hasAnnotations = true;
                        break;
                    }
                }

                for (Field f : fields) {
                    f.setAccessible(true);

                    int mods = f.getModifiers();
                    if (Modifier.isStatic(mods) || Modifier.isFinal(mods))
                        continue;

                    Object fieldValue = f.get(object);

                    if (LOG) {
                        System.out.println("      Field: " + f.getName() +
                                " (" + f.getType().getSimpleName() + ")" +
                                " → " + (fieldValue != null ? fieldValue.getClass().getSimpleName() : "null"));
                    }

                    // @CopyReference
                    if (f.getAnnotation(CopyReference.class) != null) {
                        if (LOG) System.out.println("         @CopyReference → shallow copy");
                        f.set(copy, fieldValue);
                        continue;
                    }

                    // @Copyable
                    if (f.getAnnotation(Copyable.class) != null) {
                        if (LOG) System.out.println("         @Copyable → deep copying");
                        f.set(copy, cloneRecursive(fieldValue, clonedObjects));
                        continue;
                    }

                    // No annotations in class → deep copy everything
                    if (!hasAnnotations) {
                        if (LOG) System.out.println("         No annotations → deep copy");
                        f.set(copy, cloneRecursive(fieldValue, clonedObjects));
                        continue;
                    }

                    // Annotations exist but this field has none → skip
                    if (LOG) System.out.println("         Skipped (field unannotated)");
                }
            }

            if (LOG) System.out.println("✔ Finished cloning " + clazz.getName());
            return copy;

        } catch (Exception e) {
            if (LOG) System.out.println("✖ Error cloning " + clazz.getName() + ": " + e);
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException(e);
        }
    }


    //Instantiation data for a gameObject (ECS entity)
    private static class Prefab {
        private Prefab(Iterable<Component> components) {
            this.components = components;
        }

        private Iterable<Component> components;
    }
}


