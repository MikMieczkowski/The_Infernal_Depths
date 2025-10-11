package com.mikm.entityLoader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Rectangle;
import com.mikm.Vector2Int;
import com.mikm.entities.Entity;
import com.mikm.entities.animation.DirectionalAnimation;
import com.mikm.entities.animation.SingleAnimation;
import com.mikm.entities.animation.SingleFrame;
import com.mikm.entities.actions.Action;
import com.mikm.entities.animation.SuperAnimation;
import com.mikm.entities.player.Player;
import com.mikm.entities.routineHandler.*;
import com.mikm.rendering.cave.SpawnProbability;
import com.mikm.rendering.screens.Application;
import org.reflections.Reflections;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;

//Add checks whenever there is only supposed to be one entry but there is multiple
public class EntityLoader {
    private static final String ACTION_CLASSES_PACKAGE = "com.mikm.entities.actions";

    //"wander" -> WanderBehaviour.class
    private static Map<String, Class<? extends Action>> actionClasses;
    //"slime" -> Created Slime instance
    private static Map<String, Entity> entities = new HashMap<>();

    private static Map<String, Action> nameToBehaviour = new HashMap<>();
    private static Map<String, Routine> nameToRoutine = new HashMap<>();

    private static Set<String> yamlBehaviourNames = new HashSet<>();
    private static Set<String> yamlCycleNames = new HashSet<>();

    //Better way to do this?
    //used to implement interrupt routines since routines must be loaded first
    private static Map<String, String> behaviourNameToInterruptRoutine = new HashMap<>();
    private static List<Transitions> transitionsToInit = new ArrayList<>();

    static {
        BlackboardBindings.setNameToBehaviour(nameToBehaviour);
    }

    //ANY CHANGES MADE HERE MUST ALSO BE MADE TO COPY ENTITY

    private static class TransitionToImplement {
        boolean isRepeatTransition;
        String behaviourName;
        String routineName;

        public TransitionToImplement(boolean isRepeatTransition, String behaviourName, String routineName) {
            this.isRepeatTransition = isRepeatTransition;
            this.behaviourName = behaviourName;
            this.routineName = routineName;
        }
    }

    private static String fileName;

    static {
        actionClasses = getActionClasses();
    }

    public static Map<String, Class<? extends Action>> getActionClasses() {
        if (actionClasses == null) {
            actionClasses = new HashMap<>();
            Reflections reflections = new Reflections(ACTION_CLASSES_PACKAGE);
            Set<Class<? extends Action>> readClasses =
                    reflections.getSubTypesOf(Action.class);

            for (Class<? extends Action> c : readClasses) {
                String name = c.getSimpleName()
                        .replace("Action", "");
                actionClasses.put(name, c);
            }
        }
        return actionClasses;
    }

    public static Entity create(String entityName) {
        //if already read this yaml file, return a copy of the entity, otherwise read
        if (entities.containsKey(entityName)) {
            if (entityName.equals("player")) {
                throw new RuntimeException("Already created player");
            }
            return copyEntity(entities.get(entityName));
        }

        nameToBehaviour.clear();
        nameToRoutine.clear();
        yamlBehaviourNames.clear();
        yamlCycleNames.clear();
        behaviourNameToInterruptRoutine.clear();
        transitionsToInit.clear();

        FileHandle file = Gdx.files.internal("yaml/" + entityName + ".yaml");
        fileName = entityName + ".yaml";
        yamlAction = null;
        InputStream input = file.read();
        Yaml yaml = new Yaml();
        EntityData data = yaml.loadAs(input, EntityData.class);
        Entity entity = createEntityFromEntityData(data);
        if (entityName.equals("bat")) {
            entity.collider.isBat = true;
        }
        entities.put(entityName, entity);
        return entity;
    }

    private static Entity createEntityFromEntityData(EntityData data) {
        Entity entity;
        if (data.CONFIG.NAME.equals("player")) {
            entity = new Player(0, 0);
        } else {
            entity = new Entity(0, 0);
        }

        //Must load in this order: FULL_BOUNDS_DIMENSIONS (in CONFIG), BEHAVIOURS, ROUTINES, then CONFIG/SPAWN_CONFIG

        entity.NAME = data.CONFIG.NAME;
        loadConfigBounds(entity, data.CONFIG);

        loadBehaviourDataMap(entity, data.BEHAVIOURS);
        loadRoutineData(entity, data.ROUTINES);
        loadInterruptRoutines();
        //init ConditionTransitions
        for (Transitions transition : transitionsToInit) {
            transition.init(nameToRoutine);
        }
        loadConfig(entity, data.CONFIG, data);
        //load SPAWN_CONFIG
        if (data.SPAWN_CONFIG != null) {
            data.SPAWN_CONFIG.LEVEL_4_SPAWN_PERCENT = data.SPAWN_CONFIG.LEVEL_3_SPAWN_PERCENT;
            entity.spawnProbability = new SpawnProbability(data.SPAWN_CONFIG.LEVEL_1_SPAWN_PERCENT, data.SPAWN_CONFIG.LEVEL_2_SPAWN_PERCENT, data.SPAWN_CONFIG.LEVEL_3_SPAWN_PERCENT, data.SPAWN_CONFIG.LEVEL_4_SPAWN_PERCENT);
        }
        return entity;
    }

    private static void loadConfigBounds(Entity entity, EntityData.Config config) {

        if (config.BOUNDS == null) {
            config.BOUNDS = new EntityData.Config.BoundsData();
        }
        int w = varOrDef(config.BOUNDS.IMAGE_WIDTH, Application.TILE_WIDTH);
        int h = varOrDef(config.BOUNDS.IMAGE_HEIGHT, Application.TILE_HEIGHT);
        entity.FULL_BOUNDS_DIMENSIONS = new Vector2Int(w, h);

        entity.ORIGIN_X = varOrDef(config.ORIGIN_X, entity.FULL_BOUNDS_DIMENSIONS.x/2);

        //BOUNDS
        //DEFAULTS

        boolean shadow = config.BOUNDS.HAS_SHADOW == null || config.BOUNDS.HAS_SHADOW;

        int sw = varOrDef(config.BOUNDS.SHADOW_WIDTH, Application.TILE_WIDTH);
        int sh = varOrDef(config.BOUNDS.SHADOW_HEIGHT, Application.TILE_HEIGHT);

        int hr = (config.BOUNDS.HITBOX_RADIUS == 0) ? 8 : config.BOUNDS.HITBOX_RADIUS;

        entity.HAS_SHADOW = shadow;
        entity.SHADOW_BOUNDS_OFFSETS = new Rectangle(
                config.BOUNDS.SHADOW_OFFSET_X,
                config.BOUNDS.SHADOW_OFFSET_Y,
                sw,
                sh
        );
        entity.HITBOX_OFFSETS = new Vector2Int(config.BOUNDS.HITBOX_OFFSET_X, config.BOUNDS.HITBOX_OFFSET_Y);
        entity.HITBOX_RADIUS = hr;

    }

    private static void loadInterruptRoutines() {
        for (Map.Entry<String, String> entry : behaviourNameToInterruptRoutine.entrySet()) {
            String behaviourName = entry.getKey();
            String routineName = entry.getValue();
            if (!nameToBehaviour.containsKey(behaviourName)) {
                throw new RuntimeException("This error should not occur " + routineName + " " + behaviourName + " in " + fileName);
            }
            if (!nameToRoutine.containsKey(routineName)) {
                throw new RuntimeException("No such routine " + routineName + " for ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO in " + behaviourName + " in " + fileName);
            }

            Action behaviour = nameToBehaviour.get(behaviourName);
            behaviour.ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO = nameToRoutine.get(routineName);
        }
    }

    private static Action yamlAction;

    private static void loadConfig(Entity entity, EntityData.Config config, EntityData data) {
        entity.NAME = config.NAME;
        entity.MAX_HP = varOrDef(config.MAX_HP, 3);
        entity.hp = entity.MAX_HP;
        entity.DAMAGE = varOrDef(config.DAMAGE, 1);
        entity.KNOCKBACK = varOrDef(config.KNOCKBACK, 1);
        entity.SPEED = varOrDef(config.SPEED, 1);
        entity.isAttackable = !config.INVINCIBLE;

        //Create entity damaged action (not rlly createConfig)
        //load hurt anim
        EntityData.BehaviourData b = config.HURT_ANIMATION;
        if (config.HURT_ANIMATION == null) {
            b = new EntityData.BehaviourData();
            if (data.BEHAVIOURS.containsKey("Idle")) {
                b.COPY_ANIMATION = "Idle";
            } else {
                //grab random animation
                b.COPY_ANIMATION = data.BEHAVIOURS.entrySet().iterator().next().getKey();
            }
        }
        loadActionAnimation(entity, entity.damagedAction, b);
        entity.damagedAction.name = "damaged";


        entity.HURT_SOUND_EFFECT = config.HURT_SOUND_EFFECT;
        if (config.POST_HIT_ROUTINE == null || config.POST_HIT_ROUTINE.isEmpty()) {
            config.POST_HIT_ROUTINE = "start";
        }
        entity.POST_HIT_ROUTINE = nameToRoutine.get(config.POST_HIT_ROUTINE);

        entity.routineHandler.CHECK_TRANSITIONS_EVERY_FRAME = config.CHECK_TRANSITIONS_EVERY_FRAME;

    }

    private static int varOrDef(int var, int def) {
        if (var == 0) {
            return def;
        }
        return var;
    }

    private static float varOrDef(float var, float def) {
        if (var == 0) {
            return def;
        }
        return var;
    }

    private static void loadBehaviourDataMap(Entity entity, Map<String, EntityData.BehaviourData> behaviourDataMap) {
        if (behaviourDataMap == null) {
            throw new RuntimeException("Couldn't load behaviour data from yaml file");
        }

        for (Map.Entry<String, EntityData.BehaviourData> behaviourDataEntry : behaviourDataMap.entrySet()) {
            String name = behaviourDataEntry.getKey();
            EntityData.BehaviourData behaviourData = behaviourDataEntry.getValue();
            if (behaviourData == null) {
                throw new RuntimeException("Couldn't read behaviourData in " + fileName);
            }
            if (behaviourData.ACTION == null) {
                behaviourData.ACTION = name;
            }

            boolean noBehaviourUnderThatNameExists = !actionClasses.containsKey(behaviourData.ACTION);
            if (noBehaviourUnderThatNameExists) {
                throw new RuntimeException(fileName + ": No behaviour class associated with " + behaviourData.ACTION + " in " + ACTION_CLASSES_PACKAGE);
            }
            yamlAction = null;
            loadBehaviourData(entity, name, behaviourData, behaviourDataMap);
            yamlAction.name = behaviourData.ACTION;
            yamlAction.configVars = behaviourData.CONFIG;
            entity.usedActionClasses.add(behaviourData.ACTION);
        }
        for (String s: entity.usedActionClasses) {
            Blackboard.getInstance().bind("timeSince" + s, entity, 0f);
        }
    }

    //builds yamlAction and adds it to nameToBehaviour
    //if there are duplicate behaviours it just loads and adds again
    private static void loadBehaviourData(Entity entity, String name, EntityData.BehaviourData behaviourData, Map<String, EntityData.BehaviourData> behaviourDataMap) {
        loadBehaviourAnimation(entity, name, behaviourData);
        if (behaviourData.ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO != null) {
            behaviourNameToInterruptRoutine.put(name, behaviourData.ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO);
        }
        //load copy config
        if (behaviourData.COPY_CONFIG != null) {
            Action toCopy = nameToBehaviour.get(behaviourData.COPY_CONFIG);
            if (!behaviourDataMap.containsKey(behaviourData.COPY_CONFIG)) {
                throw new RuntimeException("No behaviour named " + behaviourData.COPY_CONFIG + " to execute COPY_CONFIG in " + name + " in " + fileName);
            }
            loadBehaviourConfig(entity, behaviourData.COPY_CONFIG, behaviourDataMap.get(behaviourData.COPY_CONFIG));
        }
        loadBehaviourConfig(entity, name, behaviourData);
        yamlAction.postConfigRead();
    }

    private static void loadBehaviourAnimation(Entity entity, String name, EntityData.BehaviourData behaviourData) {
        if (yamlAction != null) {
            throw new RuntimeException("yamlBehaviour should be null");
        }

        yamlBehaviourNames.add(name);
        yamlAction = createEmptyBehaviourFromName(entity, name, behaviourData);

        //fill yamlAction with behaviourData
        loadActionAnimation(entity, yamlAction, behaviourData);

        nameToBehaviour.put(name, yamlAction);
    }
    private static int numberOfAnimationFields(EntityData.BehaviourData behaviourData) {
        int count = 0;
        if (behaviourData.DIRECTIONAL_ANIMATION != null) count++;
        if (behaviourData.COPY_ANIMATION != null) count++;
        if (behaviourData.FROM_FIRST_FRAME_OF_EACH_DIR != null) count++;
        if (behaviourData.SINGLE_ANIMATION != null) count++;
        if (behaviourData.SINGLE_FRAME != null) count++;
        return count;
    }

    private static void loadActionAnimation(Entity entity, Action action, EntityData.BehaviourData behaviourData) {
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
            if (!nameToBehaviour.containsKey(behaviourData.COPY_ANIMATION)) {
                throw new RuntimeException("Behaviour " + behaviourData.COPY_ANIMATION + " has not been defined in " + fileName + ", or is defined after");
            }
            SuperAnimation anim = nameToBehaviour.get(behaviourData.COPY_ANIMATION).animation;
            if (anim == null) {
                throw new RuntimeException();
            }
            action.animation = anim;
        } else if (behaviourData.FROM_FIRST_FRAME_OF_EACH_DIR != null) {
            if (!yamlBehaviourNames.contains(behaviourData.FROM_FIRST_FRAME_OF_EACH_DIR)) {
                throw new RuntimeException("Behaviour " + behaviourData.FROM_FIRST_FRAME_OF_EACH_DIR + " has not been defined in " + fileName + ", or is defined after");
            }
            DirectionalAnimation anim = (DirectionalAnimation)(nameToBehaviour.get(behaviourData.FROM_FIRST_FRAME_OF_EACH_DIR).animation);
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

    private static void loadBehaviourConfig(Entity entity, String name, EntityData.BehaviourData behaviourData) {
        if (nameToBehaviour.isEmpty()) {
            throw new RuntimeException("Animations should be loaded before loading config");
        }
        Class<? extends Action> behaviourClass = actionClasses.get(behaviourData.ACTION);

        if (behaviourData.CONFIG == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : behaviourData.CONFIG.entrySet()) {
            String varName = entry.getKey();
            //"speed_min" -> "SPEED_MIN"
            varName = varName.toUpperCase();

            Object varValue = entry.getValue();
            setVarInClass(behaviourClass, yamlAction, varName, varValue);
        }
    }

    public static void setVarInClass(Class<? extends Action> clazz, Action instance, String varName, Object varValue) {
        try {
            //getField allows for fields in superclasses
            Field f = findField(clazz, varName);
            f.setAccessible(true);
            setField(instance, f, varValue);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("No field in " + clazz.getSimpleName() + " for config var: " + varName + " in file " + fileName, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to set config var: " + varName + " in file " + fileName, e);
        }
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

    private static void setField(Action instance, Field var, Object varValue) throws IllegalAccessException {
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
                var.setInt(instance, n.intValue());
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
                var.setFloat(instance, n.floatValue());
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
                var.setDouble(instance, d);
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


    private static Action createEmptyBehaviourFromName(Entity entity, String name, EntityData.BehaviourData behaviourData) {
        Class<? extends Action> behaviourClass = actionClasses.get(behaviourData.ACTION);
        Action action;
        try {
            //instantiate behaviourClass
            action = behaviourClass.getConstructor(Entity.class).newInstance(entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return action;
    }

    private static void loadRoutineData(Entity entity, Map<String, EntityData.RoutineData> routineDataMap) {
        if (routineDataMap == null) {
            throw new RuntimeException("Couldn't load cycle data from yaml file " + fileName);
        }

        ArrayList<Routine> routines = new ArrayList<>();
        for (Map.Entry<String, EntityData.RoutineData> routineDataEntry : routineDataMap.entrySet()) {
            String name = routineDataEntry.getKey();
            EntityData.RoutineData routineData = routineDataEntry.getValue();


            Routine routine = new Routine();
            nameToRoutine.put(name, routine);

            yamlCycleNames.add(name);
            Cycle cycle = loadCycleData(entity, routineData);
            Transitions transitions = loadCycleTransitionsData(entity, name, routineData);
            routine.init(cycle, transitions);
            routine.name = name;
            routines.add(routine);
        }
        entity.routineHandler.init(routines);
    }

    @SuppressWarnings("unchecked")
    private static Cycle loadCycleData(Entity entity, EntityData.RoutineData routineData) {
        ArrayList<CycleStep> cycleSteps = new ArrayList<>();
        if (routineData == null) {
            throw new RuntimeException("Incorrect ROUTINES definition in" + fileName);
        }
        if (routineData.CYCLE == null) {
            throw new RuntimeException("Must add a list of BEHAVIOUR, RANDOM, WEIGHTED_RANDOM, or perhaps other under CYCLES in" + fileName);
        }
        for (Map<String, Object> cycleStepData: routineData.CYCLE) {
            Iterator<Map.Entry<String, Object>> it = cycleStepData.entrySet().iterator();
            Map.Entry<String, Object> entry1 = it.next();
            int repeatTimes = 1;
            if (it.hasNext()) {
                //handle optional REPEAT attr under BEHAVIOUR:
                Map.Entry<String, Object> entry2 = it.next();
                String attrName = entry2.getKey();
                if (!attrName.equals("REPEAT")) {
                    throw new RuntimeException("Unknown second attribute under ROUTINES:routineName:CYCLE:-: in " + fileName);
                }
                Object repeatObj = entry2.getValue();
                repeatTimes = ((Number) repeatObj).intValue();
                if (it.hasNext()) {
                    throw new RuntimeException("Only up to two children allowed in ROUTINES:routineName:CYCLE:-: in " + fileName);
                }
            }
            Object listMapOrString = entry1.getValue();

            CycleStep cycleStep;
            if (listMapOrString instanceof Map) {
                Map<String, Object> weightedRandomData = (Map<String, Object>) listMapOrString;
                Map<Action, Float> weightedRandom = new HashMap<>();

                float totalProbability = 0;
                boolean foundElse = false;
                Action elseAction = null;
                for (Map.Entry<String, Object> weightedRandomEntry : weightedRandomData.entrySet()) {
                    String behaviourName = weightedRandomEntry.getKey();
                    if (!nameToBehaviour.containsKey(behaviourName)) {
                        throw new RuntimeException("Referred to a nonexistent behaviour in " + fileName);
                    }
                    Action action = nameToBehaviour.get(behaviourName);
                    Object obj = weightedRandomEntry.getValue();
                    if (obj instanceof String) {
                        String s = (String)obj;
                        if (!s.equals("else")) {
                            throw new RuntimeException("Found string in ROUTINES:routineName:CYCLE:-:WEIGHTED_RANDOM in " + fileName + ". Only string allowed here is 'else'");
                        }
                        foundElse = true;
                        elseAction = action;
                    } else {
                        Double d = (Double)obj;
                        float probability = d.floatValue();
                        totalProbability += probability;
                        weightedRandom.put(action, probability);
                    }
                }
                if (foundElse) {
                    weightedRandom.put(elseAction, 1-totalProbability);
                }
                cycleStep = new WeightedRandomCycleStep(weightedRandom);
            } else if (listMapOrString instanceof List) {
                List<String> randomData = (List<String>) listMapOrString;
                List<Action> random = new ArrayList<>();
                for (String behaviourName : randomData) {
                    if (!nameToBehaviour.containsKey(behaviourName)) {
                        throw new RuntimeException("Referred to a nonexistent behaviour in " + fileName);
                    }
                    random.add(nameToBehaviour.get(behaviourName));
                }
                cycleStep = new RandomCycleStep(random);
            } else if (listMapOrString instanceof String) {
                String behaviourName = (String) listMapOrString;
                if (!nameToBehaviour.containsKey(behaviourName)) {
                    throw new RuntimeException("Referred to a nonexistent behaviour in " + fileName);
                }
                cycleStep = new BehaviourCycleStep(nameToBehaviour.get(behaviourName));
            } else {
                throw new RuntimeException("Unknown type: " + listMapOrString.getClass());
            }
            for (int i = 0; i < repeatTimes; i++) {
                cycleSteps.add(cycleStep);
            }
        }
        return new Cycle(entity, cycleSteps);
    }

    private static Transitions loadCycleTransitionsData(Entity entity, String routineName, EntityData.RoutineData routineData) {
        ArrayList<ConditionTransition> conditionTransitions = new ArrayList<>();
        //only one NO_REPEAT
        String noRepeatGoToString = null;
        if (routineData.TRANSITIONS == null) {
            throw new RuntimeException("Incorrect TRANSITIONS definition in " + fileName);
        }
        //Multiple transitions per behaviour
        for (EntityData.RoutineData.TransitionData transitionData : routineData.TRANSITIONS) {
            if (transitionData.NO_REPEAT != null) {
                if (noRepeatGoToString != null) {
                    throw new RuntimeException("Multiple NoRepeat conditions specified in " + fileName);
                }
                noRepeatGoToString = transitionData.NO_REPEAT;
            } else if (transitionData.GO_TO != null) {

                //build conditionTransition
                Predicate<Entity> condition = loadCompoundCondition(transitionData);

                ConditionTransition conditionTransition = new ConditionTransition(condition, transitionData.GO_TO);
                conditionTransitions.add(conditionTransition);
            } else {
                throw new RuntimeException("Must define a NO_REPEAT or a ON_CONDITION GO_TO in condition transition in " + routineName + " in " + fileName);
            }

        }
        Transitions transitions;
        if (noRepeatGoToString != null && !conditionTransitions.isEmpty()) {
            transitions = new Transitions(conditionTransitions, noRepeatGoToString);
        } else if (noRepeatGoToString != null) {
            transitions = new Transitions(noRepeatGoToString);
        } else if (!conditionTransitions.isEmpty()) {
            transitions = new Transitions(conditionTransitions);
        } else {
            throw new RuntimeException("Can't find any transition in " + fileName);
        }

        transitionsToInit.add(transitions);
        return transitions;
    }

    private static Predicate<Entity> loadCompoundCondition(EntityData.RoutineData.TransitionData transitionData) {
        return ConditionCompiler.compile(transitionData.ON_CONDITION);
    }


    public static List<String> getEntitiesInYamlFolder() {
        List<String> names = new ArrayList<>();

        FileHandle dir = Gdx.files.internal("yaml"); // relative to assets/
        for (FileHandle file : dir.list()) {
            if (!file.extension().equals("yaml") && !file.extension().equals("yml")) continue;

            boolean inConfig = false;
            for (String line : file.readString().split("\\r?\\n")) {
                line = line.trim();

                if (line.equals("CONFIG:")) {
                    inConfig = true;
                    continue;
                }

                if (inConfig && line.startsWith("NAME:")) {
                    // extract value after "NAME:"
                    String value = line.substring("NAME:".length()).trim();
                    names.add(value);
                    inConfig = false; // stop after first NAME in CONFIG
                }
            }
        }

        return names;
    }

    private static Entity copyEntity(Entity entity) {
        Entity output = new Entity(entity.x, entity.y);
        output.damagesPlayer = entity.damagesPlayer;
        output.NAME = entity.NAME;
        output.DAMAGE = entity.DAMAGE;
        output.KNOCKBACK = entity.KNOCKBACK;
        output.MAX_HP = entity.MAX_HP;
        output.hp = output.MAX_HP;
        output.SPEED = entity.SPEED;
        output.ORIGIN_X = entity.ORIGIN_X;
        output.ORIGIN_Y = entity.ORIGIN_Y;
        output.isAttackable = entity.isAttackable;
        output.POST_HIT_ROUTINE = entity.POST_HIT_ROUTINE;
        output.HURT_SOUND_EFFECT = entity.HURT_SOUND_EFFECT;
        output.spawnProbability = entity.spawnProbability;
        output.HAS_SHADOW = entity.HAS_SHADOW;
        output.SHADOW_BOUNDS_OFFSETS = entity.SHADOW_BOUNDS_OFFSETS;
        output.FULL_BOUNDS_DIMENSIONS = entity.FULL_BOUNDS_DIMENSIONS;
        output.HITBOX_OFFSETS = entity.HITBOX_OFFSETS;
        output.HITBOX_RADIUS = entity.HITBOX_RADIUS;
        //deep copy routines/cycles/actions
        output.routineHandler.init(entity.routineHandler);
        output.damagedAction.name = "damaged";
        output.damagedAction.animation = entity.damagedAction.animation.copy();
        // Copy used action classes and (re)bind per-entity timeSince<Action> vars for this new entity
        output.usedActionClasses.addAll(entity.usedActionClasses);
        for (String actionName : output.usedActionClasses) {
            Blackboard.getInstance().bind("timeSince" + actionName, output, 0f);
        }
        if (output.NAME.equals("bat")) {
            output.collider.isBat = true;
        }
        output.isCopied = true;
        return output;
    }
}
