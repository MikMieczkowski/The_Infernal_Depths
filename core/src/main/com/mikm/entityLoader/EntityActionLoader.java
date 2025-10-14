package com.mikm.entityLoader;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.mikm.entities.Entity;
import com.mikm.entities.actions.Action;
import com.mikm.entities.animation.DirectionalAnimation;
import com.mikm.entities.animation.SingleAnimation;
import com.mikm.entities.animation.SingleFrame;
import com.mikm.entities.animation.SuperAnimation;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EntityBehaviourLoader {
    //BEHAVIOUR: Action + Animation + Config
    //ACTION: The action class. Does something

    //behaviourName: anything
    //actionName: must refer to an action class
    private static final String ACTION_CLASSES_PACKAGE = "com.mikm.entities.actions";
    private String fileName;
    private Entity entity;

    private Map<String, Action> nameToAction = new HashMap<>();
    private Map<String, EntityData.BehaviourData> behaviourDataMap;

    EntityBehaviourLoader(String fileName, Entity entity, Map<String, EntityData.BehaviourData> behaviourDataMap) {
        this.fileName = fileName;
        this.entity = entity;
        this.behaviourDataMap = behaviourDataMap;
    }

    //Creates nameToAction and fills it with loaded action instances based on behaviourDataMap.
    Map<String, Action> loadActions(EntityData.BehaviourData damagedBehaviourAnimationData) {
        if (behaviourDataMap == null) {
            throw new RuntimeException("Couldn't load behaviour data from yaml file");
        }


        for (Map.Entry<String, EntityData.BehaviourData> behaviourDataEntry : behaviourDataMap.entrySet()) {
            //initialize and error check
            String behaviourName = behaviourDataEntry.getKey();
            EntityData.BehaviourData behaviourData = behaviourDataEntry.getValue();
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

            loadAction(behaviourName, behaviourData);
        }

        //load damaged action
        loadActionAnimation(damagedBehaviourAnimationData, entity.damagedAction);
        entity.damagedAction.name = "damaged";

        return nameToAction;
    }

    //loads one Action into nameToAction
    //if there are duplicate behaviours it just loads and adds again
    private void loadAction(String behaviourName, EntityData.BehaviourData behaviourData) {
        Action action = instantiateAction(behaviourData);
        loadActionAnimation(behaviourData, action);
        nameToAction.put(behaviourName, action);


        Map<String, String> behaviourNameToInterruptRoutine = new HashMap<>();
        if (behaviourData.ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO != null) {
            behaviourNameToInterruptRoutine.put(behaviourName, behaviourData.ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO);
        }
        loadCopyConfig(behaviourName, behaviourData, action);
        loadConfig(behaviourData, action);
        action.postConfigRead();
        action.name = behaviourData.ACTION;
        action.configVars = behaviourData.CONFIG;
        entity.usedActionClasses.add(behaviourData.ACTION);
    }

    private void loadCopyConfig(String behaviourName, EntityData.BehaviourData behaviourData, Action action) {
        if (behaviourData.COPY_CONFIG != null) {
            if (!behaviourDataMap.containsKey(behaviourData.COPY_CONFIG)) {
                throw new RuntimeException("No behaviour named " + behaviourData.COPY_CONFIG + " to execute COPY_CONFIG in " + behaviourName + " in " + fileName);
            }
            // Recursively apply ancestor COPY_CONFIG chains before applying the direct parent
            loadCopyConfigRecursive(behaviourData.COPY_CONFIG, new java.util.HashSet<String>(), action);
        }
    }

    // Applies COPY_CONFIG recursively so that a behaviour inherits the entire chain of configs.
    private void loadCopyConfigRecursive(String behaviourNameToCopy, java.util.Set<String> visited, Action action) {
        if (visited.contains(behaviourNameToCopy)) {
            throw new RuntimeException("Detected COPY_CONFIG cycle involving behaviour '" + behaviourNameToCopy + "' in " + fileName);
        }
        visited.add(behaviourNameToCopy);

        EntityData.BehaviourData toCopy = behaviourDataMap.get(behaviourNameToCopy);
        if (toCopy == null) {
            throw new RuntimeException("No behaviour named " + behaviourNameToCopy + " to execute COPY_CONFIG in " + fileName);
        }
        if (toCopy.COPY_CONFIG != null) {
            if (!behaviourDataMap.containsKey(toCopy.COPY_CONFIG)) {
                throw new RuntimeException("No behaviour named " + toCopy.COPY_CONFIG + " to execute COPY_CONFIG in " + behaviourNameToCopy + " in " + fileName);
            }
            loadCopyConfigRecursive(toCopy.COPY_CONFIG, visited, action);
        }
        // Apply this behaviour's own CONFIG after its ancestors
        loadConfig(toCopy, action);
    }

    private void loadActionAnimation(EntityData.BehaviourData behaviourData, Action action) {
        int count = numberOfAnimationFields(behaviourData);
        if (count == 0) {
            throw new RuntimeException("No animation source set for behaviour in file " + fileName);
        } else if (count > 1) {
            throw new RuntimeException("Multiple animation sources set for behaviour in " + fileName + ": only one allowed");
        }

        if (behaviourData.DIRECTIONAL_ANIMATION != null) {
            if (behaviourData.DIRECTIONAL_ANIMATION.IMAGE_NAME != null) {
                throw new RuntimeException("Cant use IMAGE_NAME with single animation. in file " + fileName + "Use STARTS_WITH instead");
            }
            action.animation = new DirectionalAnimation(
                    behaviourData.DIRECTIONAL_ANIMATION.STARTS_WITH,
                    entity.FULL_BOUNDS_DIMENSIONS.x,
                    entity.FULL_BOUNDS_DIMENSIONS.y,
                    behaviourData.DIRECTIONAL_ANIMATION.FPS,
                    behaviourData.DIRECTIONAL_ANIMATION.LOOP ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
        } else if (behaviourData.COPY_ANIMATION != null) {
            //search through yamlBehaviourNames for name and addAnimation(itsAnimation) through entity.animationHandler.animations.get(name)
            if (!nameToAction.containsKey(behaviourData.COPY_ANIMATION)) {
                throw new RuntimeException("Behaviour " + behaviourData.COPY_ANIMATION + " has not been defined in " + fileName + ", or is defined after");
            }
            SuperAnimation anim = nameToAction.get(behaviourData.COPY_ANIMATION).animation;
            if (anim == null) {
                throw new RuntimeException();
            }
            action.animation = anim;
        } else if (behaviourData.FROM_FIRST_FRAME_OF_EACH_DIR != null) {
            if (!nameToAction.containsKey(behaviourData.FROM_FIRST_FRAME_OF_EACH_DIR)) {
                throw new RuntimeException("Behaviour " + behaviourData.FROM_FIRST_FRAME_OF_EACH_DIR + " has not been defined in " + fileName + ", or is defined after");
            }
            DirectionalAnimation anim = (DirectionalAnimation)(nameToAction.get(behaviourData.FROM_FIRST_FRAME_OF_EACH_DIR).animation);
            if (anim == null) {
                throw new RuntimeException();
            }
            action.animation = anim.createDirectionalAnimationFromFirstFrames();
        } else if (behaviourData.SINGLE_ANIMATION != null) {
            if (behaviourData.SINGLE_ANIMATION.STARTS_WITH != null) {
                throw new RuntimeException("Cant use STARTS_WITH with single animation. in file " + fileName + "Use IMAGE_NAME instead");
            }
            action.animation = new SingleAnimation(
                    behaviourData.SINGLE_ANIMATION.IMAGE_NAME,
                    entity.FULL_BOUNDS_DIMENSIONS.x,
                    entity.FULL_BOUNDS_DIMENSIONS.y,
                    behaviourData.SINGLE_ANIMATION.FPS,
                    behaviourData.SINGLE_ANIMATION.LOOP ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
        } else if (behaviourData.SINGLE_FRAME != null) {
            action.animation = new SingleFrame(
                    behaviourData.SINGLE_FRAME.IMAGE_NAME,
                    entity.FULL_BOUNDS_DIMENSIONS.x,
                    entity.FULL_BOUNDS_DIMENSIONS.y);
        }
    }

    private void loadConfig(EntityData.BehaviourData behaviourData, Action action) {
        if (nameToAction.isEmpty()) {
            throw new RuntimeException("Animations should be loaded before loading config");
        }
        Class<? extends Action> behaviourClass = findClassFromFilePackages(behaviourData.ACTION);

        if (behaviourData.CONFIG == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : behaviourData.CONFIG.entrySet()) {
            String varName = entry.getKey();
            //"speed_min" -> "SPEED_MIN"
            varName = varName.toUpperCase();

            Object varValue = entry.getValue();
            setVarInClass(behaviourClass, action, varName, varValue, fileName);
        }
    }

    private int numberOfAnimationFields(EntityData.BehaviourData behaviourData) {
        int count = 0;
        if (behaviourData.DIRECTIONAL_ANIMATION != null) count++;
        if (behaviourData.COPY_ANIMATION != null) count++;
        if (behaviourData.FROM_FIRST_FRAME_OF_EACH_DIR != null) count++;
        if (behaviourData.SINGLE_ANIMATION != null) count++;
        if (behaviourData.SINGLE_FRAME != null) count++;
        return count;
    }

    private Action instantiateAction(EntityData.BehaviourData behaviourData) {
        Class<? extends Action> behaviourClass = findClassFromFilePackages(behaviourData.ACTION);
        Action action;
        try {
            //instantiate behaviourClass
            action = behaviourClass.getConstructor(Entity.class).newInstance(entity);
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
