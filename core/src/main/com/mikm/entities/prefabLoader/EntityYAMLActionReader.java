package com.mikm.entities.prefabLoader;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.mikm._components.Transform;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm.entities.actions.Action;
import com.mikm.entities.animation.DirectionalAnimation;
import com.mikm.entities.animation.SingleAnimation;
import com.mikm.entities.animation.SingleFrame;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EntityYAMLActionReader {
    //BEHAVIOUR: Action + Animation + Config
    //ACTION: The action class. Does something

    //behaviourName: anything
    //actionName: must refer to an action class
    private static final String ACTION_CLASSES_PACKAGE = "com.mikm.entities.actions";
    private String fileName;

    private Map<String, Action> nameToAction = new HashMap<>();
    private Map<String, EntityYAMLData.BehaviourData> behaviourDataMap;

    private RoutineListComponent routineListComponent;
    private Transform transform;

    EntityYAMLActionReader(String fileName, Map<Class<? extends Component>, Component> components, Map<String, EntityYAMLData.BehaviourData> behaviourDataMap) {
        this.fileName = fileName;
        this.behaviourDataMap = behaviourDataMap;
        transform = (Transform) components.get(Transform.class);
        routineListComponent = (RoutineListComponent) components.get(RoutineListComponent.class);
    }

    //Creates nameToAction and fills it with loaded action instances based on behaviourDataMap.
    Map<String, Action> loadActions(EntityYAMLData.BehaviourData damagedBehaviourAnimationData, String entityPostHitRoutineData) {
        if (behaviourDataMap == null) {
            throw new RuntimeException("Couldn't load behaviour data from yaml file");
        }


        for (Map.Entry<String, EntityYAMLData.BehaviourData> behaviourDataEntry : behaviourDataMap.entrySet()) {
            //initialize and error check
            String behaviourName = behaviourDataEntry.getKey();
            EntityYAMLData.BehaviourData behaviourData = behaviourDataEntry.getValue();
            if (behaviourData == null) {
                throw new RuntimeException("Couldn't read behaviourData in " + fileName);
            }
            if (behaviourData.ACTION == null) {
                behaviourData.ACTION = behaviourName;
            }
            String actionName = behaviourData.ACTION;
            boolean noActionUnderThatNameExists = !actionExistsInFilePackage(actionName);
            if (noActionUnderThatNameExists) {
                throw new RuntimeException(fileName + ": No action class associated with " + behaviourData.ACTION + " in " + ACTION_CLASSES_PACKAGE);
            }

            loadAction(behaviourName, behaviourData, entityPostHitRoutineData);
        }

        //load damaged action
        loadActionAnimation(damagedBehaviourAnimationData, routineListComponent.damagedAction);
        routineListComponent.damagedAction.name = "damaged";

        return nameToAction;
    }

    //loads one Action into nameToAction
    //if there are duplicate behaviours it just loads and adds again
    private void loadAction(String behaviourName, EntityYAMLData.BehaviourData behaviourData, String entityPostHitRoutineData) {
        Action action = instantiateAction(behaviourData);
        //loads animation
        loadActionAnimation(behaviourData, action);
        nameToAction.put(behaviourName, action);

        //loads config
        //loadCopyConfig(behaviourName, behaviourData, action);
        loadConfig(behaviourData, action);
        action.name = behaviourData.ACTION;
        if (behaviourData.POST_HIT_ROUTINE == null) {
            behaviourData.POST_HIT_ROUTINE = entityPostHitRoutineData;
        }
        action.POST_HIT_ROUTINE = behaviourData.POST_HIT_ROUTINE;
        routineListComponent.usedActionClasses.add(behaviourData.ACTION);
    }

    private void loadActionAnimation(EntityYAMLData.BehaviourData behaviourData, Action action) {
        if (behaviourData.ANIMATION.LOOP == null) {
            behaviourData.ANIMATION.LOOP = true;
        }
        // Check for new nested ANIMATION structure first
        if (behaviourData.ANIMATION != null) {
            String animationType = behaviourData.ANIMATION.TYPE;
            // Allow shorthand by inferring TYPE based on which fields are present
            if (animationType == null) {
                if (behaviourData.ANIMATION.FROM_FIRST_FRAME_OF_EACH_DIR != null) {
                    animationType = "FROM_FIRST_FRAME_OF_EACH_DIR";
                } else if (behaviourData.ANIMATION.STARTS_WITH != null) {
                    animationType = "DIRECTIONAL_ANIMATION";
                } else if (behaviourData.ANIMATION.IMAGE_NAME != null) {
                    // If FPS present, assume animated sheet; otherwise a single frame
                    animationType = (behaviourData.ANIMATION.FPS > 0)
                            ? "SINGLE_ANIMATION"
                            : "SINGLE_FRAME";
                }
            }
            if (animationType == null) {
                throw new RuntimeException("ANIMATION TYPE not specified in " + fileName);
            }

            switch (animationType) {
                case "DIRECTIONAL_ANIMATION":
                    if (behaviourData.ANIMATION.IMAGE_NAME != null) {
                        throw new RuntimeException("Cant use IMAGE_NAME with directional animation. in file " + fileName + "Use STARTS_WITH instead");
                    }
                    action.animation = new DirectionalAnimation(
                            behaviourData.ANIMATION.STARTS_WITH,
                            transform.FULL_BOUNDS_DIMENSIONS.x,
                            transform.FULL_BOUNDS_DIMENSIONS.y,
                            behaviourData.ANIMATION.FPS,
                            behaviourData.ANIMATION.LOOP ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
                    break;
                case "SINGLE_ANIMATION":
                    if (behaviourData.ANIMATION.STARTS_WITH != null) {
                        throw new RuntimeException("Cant use STARTS_WITH with single animation. in file " + fileName + "Use IMAGE_NAME instead");
                    }
                    action.animation = new SingleAnimation(
                            behaviourData.ANIMATION.IMAGE_NAME,
                            transform.FULL_BOUNDS_DIMENSIONS.x,
                            transform.FULL_BOUNDS_DIMENSIONS.y,
                            behaviourData.ANIMATION.FPS,
                            behaviourData.ANIMATION.LOOP ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
                    break;
                case "SINGLE_FRAME":
                    action.animation = new SingleFrame(
                            behaviourData.ANIMATION.IMAGE_NAME,
                            transform.FULL_BOUNDS_DIMENSIONS.x,
                            transform.FULL_BOUNDS_DIMENSIONS.y);
                    break;
                case "FROM_FIRST_FRAME_OF_EACH_DIR":
                    if (!nameToAction.containsKey(behaviourData.ANIMATION.FROM_FIRST_FRAME_OF_EACH_DIR)) {
                        throw new RuntimeException("Behaviour " + behaviourData.ANIMATION.FROM_FIRST_FRAME_OF_EACH_DIR + " has not been defined in " + fileName + ", or is defined after");
                    }
                    DirectionalAnimation anim = (DirectionalAnimation)(nameToAction.get(behaviourData.ANIMATION.FROM_FIRST_FRAME_OF_EACH_DIR).animation);
                    if (anim == null) {
                        throw new RuntimeException();
                    }
                    action.animation = anim.createDirectionalAnimationFromFirstFrames();
                    break;
                default:
                    throw new RuntimeException("Unknown animation type: " + animationType + " in " + fileName);
            }
        }
    }

    private void loadConfig(EntityYAMLData.BehaviourData behaviourData, Action action) {
        if (nameToAction.isEmpty()) {
            throw new RuntimeException("Animations should be loaded before loading config");
        }
        Class<? extends Action> actionClass = findClassFromFilePackages(behaviourData.ACTION);

        if (behaviourData.CONFIG == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : behaviourData.CONFIG.entrySet()) {
            String varName = entry.getKey();
            //"speed_min" -> "SPEED_MIN"
            varName = varName.toUpperCase();

            Object varValue = entry.getValue();
            setVarInClass(actionClass, action, varName, varValue, fileName);
        }
    }


    private Action instantiateAction(EntityYAMLData.BehaviourData behaviourData) {
        Class<? extends Action> behaviourClass = findClassFromFilePackages(behaviourData.ACTION);
        Action action;
        try {
            //instantiate behaviourClass
            action = behaviourClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return action;
    }

    //reflectedActionClasses should only be used in these functions
    private static Map<String, Class<? extends Action>> reflectedActionClasses;
    private static boolean actionExistsInFilePackage(String name) {
        if (reflectedActionClasses == null) {
            reflectedActionClasses = new HashMap<>();
            Reflections reflections = new Reflections(ACTION_CLASSES_PACKAGE);
            Set<Class<? extends Action>> readClasses =
                    reflections.getSubTypesOf(Action.class);

            for (Class<? extends Action> c : readClasses) {
                String n = c.getSimpleName()
                        .replace("Action", "");
                reflectedActionClasses.put(n, c);
            }
        }
        return reflectedActionClasses.containsKey(name);
    }

    private static Class<? extends Action> findClassFromFilePackages(String actionName) {
        return reflectedActionClasses.get(actionName);
    }


    public static void setVarInClass(Class<? extends Action> clazz, Action instance, String varName, Object varValue, String fileName) {
        try {
            //getField allows for fields in superclasses
            Field f = findField(clazz, varName);
            f.setAccessible(true);
            setField(instance, f, varValue, fileName);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("No field in " + clazz.getSimpleName() + " for config var: " + varName + " in file " + fileName, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to set config var: " + varName + " in file " + fileName, e);
        }
    }

    private static void setField(Action instance, Field var, Object varValue, String fileName) throws IllegalAccessException {
        if (varValue == null) {
            return;
        }

        var.setAccessible(true);
        Class<?> type = var.getType();

        // --- Boolean ---
        if (type == boolean.class || type == Boolean.class) {
            if (!(varValue instanceof Boolean)) {
                throw new IllegalArgumentException(String.format(
                        "Field '%s' expects a boolean, but got %s (%s).",
                        var.getName(), varValue.getClass().getSimpleName(), varValue)+ " in " + fileName);
            }
            var.setBoolean(instance, (Boolean) varValue);
            return;
        }

        // --- Numeric types ---
        if (Number.class.isAssignableFrom(varValue.getClass())) {
            Number n = (Number) varValue;

            // Integer
            if (type == int.class || type == Integer.class) {
                double d = n.doubleValue();
                if (d % 1 != 0) {
                    throw new IllegalArgumentException(String.format(
                            "Field '%s' expects an integer, but got a fractional number: %s.",
                            var.getName(), n) + " in " + fileName);
                }
                if (d < Integer.MIN_VALUE || d > Integer.MAX_VALUE) {
                    throw new IllegalArgumentException(String.format(
                            "Field '%s' integer value %s is out of range [%d, %d].",
                            var.getName(), n, Integer.MIN_VALUE, Integer.MAX_VALUE)+ " in " + fileName);
                }
                var.set(instance, n.intValue());
                return;
            }

            // Float
            if (type == float.class || type == Float.class) {
                double d = n.doubleValue();
                if (!Double.isFinite(d)) {
                    throw new IllegalArgumentException(String.format(
                            "Field '%s' expects a finite float value, but got %s.",
                            var.getName(), n)+ " in " + fileName);
                }
                if (Math.abs(d) > Float.MAX_VALUE) {
                    throw new IllegalArgumentException(String.format(
                            "Field '%s' float value %s exceeds float range (±%e).",
                            var.getName(), n, Float.MAX_VALUE)+ " in " + fileName);
                }
                var.set(instance, n.floatValue());
                return;
            }

            // Double
            if (type == double.class || type == Double.class) {
                double d = n.doubleValue();
                if (!Double.isFinite(d)) {
                    throw new IllegalArgumentException(String.format(
                            "Field '%s' expects a finite double value, but got %s.",
                            var.getName(), n)+ " in " + fileName);
                }
                var.set(instance, d);
                return;
            }

            // Unknown numeric target
            throw new IllegalArgumentException(String.format(
                    "Field '%s' has unsupported numeric type %s.", var.getName(), type.getSimpleName())+ " in " + fileName);
        }

        // --- Fallback for all other field types ---
        if (!type.isInstance(varValue)) {
            throw new IllegalArgumentException(String.format(
                    "Field '%s' expects %s but got %s (%s).",
                    var.getName(), type.getSimpleName(), varValue.getClass().getSimpleName(), varValue)+ " in " + fileName);
        }

        var.set(instance, varValue);
    }

    private static Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(name); // can find private/protected fields
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }
}
