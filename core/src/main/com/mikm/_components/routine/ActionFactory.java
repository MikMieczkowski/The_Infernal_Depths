package com.mikm._components.routine;

import com.mikm.entities.actions.Action;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ActionFactory {
    public static Action create(ActionDescriptor actionDescriptor) {
        Action output = instantiateAction(actionDescriptor.actionName);
        return output;

    }

    private static Action instantiateAction(String actionName) {
        Class<? extends Action> behaviourClass = findClassFromFilePackages(actionName);
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
    private static final String ACTION_CLASSES_PACKAGE = "com.mikm.entities.actions";
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
}
