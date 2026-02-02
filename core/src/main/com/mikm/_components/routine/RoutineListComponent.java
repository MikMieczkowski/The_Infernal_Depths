package com.mikm._components.routine;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm._components.CombatComponent;
import com.mikm._components.Copyable;
import com.mikm._components.SpriteComponent;
import com.mikm._components.Transform;
import com.mikm.entities.DamageInformation;
import com.mikm.entities.actions.Action;
import com.mikm.entities.actions.DamagedAction;
import com.mikm.entities.animation.Directions;
import com.mikm.entities.animation.SingleAnimation;
import com.mikm.entities.animation.SingleFrame;
import com.mikm.entities.animation.SuperAnimation;
import com.mikm.entities.prefabLoader.Blackboard;

import java.util.*;

public class RoutineListComponent implements Component {
    public static final ComponentMapper<RoutineListComponent> MAPPER = ComponentMapper.getFor(RoutineListComponent.class);

    @Copyable private ArrayList<Routine> routines;
    @Copyable public boolean CHECK_TRANSITIONS_EVERY_FRAME;
    @Copyable public DamagedAction damagedAction = new DamagedAction();
    @Copyable public Set<String> usedActionClasses = new HashSet<>();

    public float timeElapsedInCurrentAction = 0;
    public boolean CURRENT_ACTION_IS_DONE;
    Routine currentRoutine;
    //enable/disable this component's behaviour
    public boolean active = true;

    public RoutineListComponent() {

    }


    public RoutineListComponent(ArrayList<Routine> routines) {
        initRoutines(routines);
    }

    //For an entity which only does one action
    public void initRoutines(Action action, Entity entity, SuperAnimation animation) {
        NoRepeatTransition noRepeatTransition = new NoRepeatTransition("this");
        ArrayList<CycleStep> steps = new ArrayList<>();
        steps.add(new BehaviourCycleStep(action));
        ArrayList<Transition> transitions = new ArrayList<>();
        transitions.add(noRepeatTransition);
        Routine routine = new Routine(steps, transitions);
        routines = new ArrayList<>();
        routines.add(routine);
        Map<String, Routine> fakeNameToString = new HashMap<>();
        fakeNameToString.put("this", routine);
        noRepeatTransition.init(fakeNameToString);

        entity.add(action.createActionComponent());
        action.postConfigRead(entity);

        action.animation = animation;
    }

    public void initRoutines(ArrayList<Routine> routines) {
        this.routines = routines;
    }

    public void runtimeInit(Entity entity) {
        if (routines == null || routines.isEmpty() || routines.get(0) == null) {
            throw new RuntimeException("Routines is null");
        }
        enterRoutine(routines.get(0), entity);

        // Set initial sprite texture so entities render correctly even when paused (e.g., main menu)
        SpriteComponent spriteComponent = SpriteComponent.MAPPER.get(entity);
        if (spriteComponent != null && currentRoutine != null && currentRoutine.currentAction != null
                && currentRoutine.currentAction.animation != null) {
            Transform transform = Transform.MAPPER.get(entity);
            currentRoutine.currentAction.animation.update(transform != null ? transform.direction : Directions.DOWN.vector2Int);
            spriteComponent.textureRegion = currentRoutine.currentAction.animation.getKeyFrame(0);
        }
    }

    public void enterCurrentOnHittingPlayerRoutine(Entity entity) {
        Routine routine = currentRoutine.currentAction.ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO;
        if (routine != null) {
            enterRoutine(routine, entity);
        }
    }

    public void enterRoutine(Routine routine, Entity entity) {
        if (routine == null) return;
        if (currentRoutine != null) {
            currentRoutine.currentAction.onExit(entity);
        }
        currentRoutine = routine;
        currentRoutine.i = 0;
        currentRoutine.currentAction = currentRoutine.cycleSteps.get(0).getAction();
        currentRoutine.currentAction.enter(entity);
    }

    public void takeDamage(DamageInformation damageInformation, Entity entity) {
        CombatComponent combatComponent = CombatComponent.MAPPER.get(entity);
        combatComponent.takeDamage(damageInformation, entity);
        if (entersDamagedActionOnHit(entity)) {
            damagedAction.enter(entity, damageInformation);
        }
        combatComponent.startInvincibilityFrames();
    }

    private boolean entersDamagedActionOnHit(Entity entity) {
        CombatComponent combatComponent = CombatComponent.MAPPER.get(entity);
        if (combatComponent != null && combatComponent.dead) {
            return true;
        }
        String POST_HIT_ROUTINE = currentRoutine.currentAction.POST_HIT_ROUTINE;
        // NONE explicitly opts out of knockback; null or any routine name enters damaged action
        return POST_HIT_ROUTINE == null || !POST_HIT_ROUTINE.equals("NONE");
    }

    public void enterPostHitRoutine(Entity entity) {
        Routine routine;

        String POST_HIT_ROUTINE = currentRoutine.currentAction.POST_HIT_ROUTINE;
        if (POST_HIT_ROUTINE == null) {
            routine = currentRoutine;
        } else if (POST_HIT_ROUTINE.equals("NONE")) {
            throw new RuntimeException("Shouldn't enter post hit routine if POST_HIT_ROUTINE == null on " + Transform.MAPPER.get(entity).ENTITY_NAME);
        } else {
            routine = getRoutine(POST_HIT_ROUTINE);
        }
        enterRoutine(routine, entity);
    }

    public Routine getRoutine(String name) {
        for (Routine routine : routines) {
            if (routine.name.equals(name)) {
                return routine;
            }
        }
        throw new RuntimeException("No routine " + name + " in entity. For copy constructor, must copy Routines in correct order.");
    }


    public void update(Entity entity) {
        currentRoutine.currentAction.update(entity);
        if ((currentRoutine.currentAction.MAX_TIME != null && timeElapsedInCurrentAction > currentRoutine.currentAction.MAX_TIME) || CURRENT_ACTION_IS_DONE) {
            endAction(entity);
            currentRoutine.i++;
            if (checkForTransition(entity)) {
                return;
            }
            if (currentRoutine.i >= currentRoutine.cycleSteps.size()) {
                currentRoutine.i = 0;
            }

            currentRoutine.currentAction = currentRoutine.cycleSteps.get(currentRoutine.i).getAction();
            currentRoutine.currentAction.enter(entity);
        }
        if (CHECK_TRANSITIONS_EVERY_FRAME) {
            if (checkForTransition(entity)) {
                return;
            }
        }
    }

    private void endAction(Entity entity) {
        Action currentAction = currentRoutine.currentAction;
        for (String s: usedActionClasses) {
            if (!s.equals(currentAction.name)) {
                //update time value of all actions except the one that just ended

                float f = (float) Blackboard.getInstance().getVar(entity, "timeSince" + s);
                Blackboard.getInstance().bind("timeSince" + s, entity, f+ timeElapsedInCurrentAction);
            }
        }
        currentAction.onExit(entity);
    }

    //Gets called after any behaviour is complete
    public boolean checkForTransition(Entity entity) {
        for (Transition transition : currentRoutine.transitions) {
            if (transition.shouldEnter(entity, currentRoutine)) {
                enterRoutine(transition.getGoToRoutine(), entity);
                return true;
            }
        }
        return false;
    }

    public boolean inAction(String name) {
        return currentRoutine.currentAction.name.equalsIgnoreCase(name);
    }

    public SuperAnimation getCurrentActionsAnimation() {
        return currentRoutine.currentAction.animation;
    }
}
