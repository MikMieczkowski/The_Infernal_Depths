package com.mikm.entities.routineHandler;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.mikm.entities.DamageInformation;
import com.mikm.entities.animation.SuperAnimation;
import com.mikm.entities.Entity;
import com.mikm.rendering.screens.Application;

import java.util.ArrayList;

public class RoutineHandler {
    public boolean CHECK_TRANSITIONS_EVERY_FRAME = false;
    Entity entity;
    private ArrayList<Routine> routines;

    //TODO remove public
    public Routine currentRoutine;

    public RoutineHandler(Entity entity) {
        this.entity = entity;
    }

    //Expects routines.get(0) to be the routine named start
    public void init(ArrayList<Routine> routines) {
        this.routines = routines;
        init();
    }

    //copy constructor ONLY FOR DEEP COPYING ENTITY
    public void init(RoutineHandler routineHandler) {
        this.routines = new ArrayList<>();
        CHECK_TRANSITIONS_EVERY_FRAME = routineHandler.CHECK_TRANSITIONS_EVERY_FRAME;
        for (Routine routine : routineHandler.routines) {
            this.routines.add(new Routine(entity, routine));
        }
        // Re-link transitions to the newly created routines for this entity
        java.util.Map<String, Routine> nameToRoutine = new java.util.HashMap<>();
        for (Routine r : this.routines) {
            nameToRoutine.put(r.name, r);
        }
        for (Routine r : this.routines) {
            if (r.transitions != null) {
                r.transitions.init(nameToRoutine);
            }
        }
        init();
    }

    private void init() {
        if (routines == null || routines.isEmpty() || routines.get(0) == null) {
            throw new RuntimeException("Routines is null");
        }
        enterRoutine(routines.get(0));
    }


    public void update() {
        checkIfDamagedPlayer();
        //Calls checkForTransition
        currentRoutine.cycle.update();
    }

    //if DAMAGE != 0 && name != "player" then do this
    boolean checkIfDamagedPlayer() {
        if (entity.DAMAGE == 0 || entity.NAME.equals("player")) {
            return false;
        }
        boolean hitboxesOverlap = Intersector.overlaps(entity.getHitbox(), Application.player.getHitbox());
        if (hitboxesOverlap) {
            float angleToPlayer = MathUtils.atan2(Application.player.getHitbox().y - entity.y, Application.player.getHitbox().x - entity.x);
            Routine routine = entity.routineHandler.currentRoutine.cycle.currentAction.ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO;
            if (routine != null) {
                enterRoutine(routine);
            }
            Application.player.damagedAction.enter(new DamageInformation(angleToPlayer, entity.DAMAGE, entity.KNOCKBACK));
            return true;
        }
        return false;
    }

    //Gets called after any behaviour is complete
    public boolean checkForTransition() {
        //check Damaged first
        if (checkIfDamagedPlayer()) {
            //checkIfDamagedPlayer calls enter
            return true;
        }

        //check NoRepeatTransition
        if (currentRoutine.transitions.hasNoRepeatTransition) {
            if (currentRoutine.cycle.i >= currentRoutine.cycle.cycleSteps.size()) {
                enterRoutine(currentRoutine.transitions.noRepeatGoTo);
                return true;
            }
        }
        if (currentRoutine.transitions.conditionTransitions == null) {
            return false;
        }
        //check conditionTransitions
        for (ConditionTransition conditionTransition : currentRoutine.transitions.conditionTransitions) {
            if (conditionTransition.getCondition(entity)) {
                enterRoutine(conditionTransition.getGoTo());
                return true;
            }
        }
        return false;
    }

    public void enterRoutine(Routine routine) {
        if (routine == null) return;
        currentRoutine = routine;
        currentRoutine.cycle.i = 0;
        currentRoutine.cycle.currentAction = currentRoutine.cycle.cycleSteps.get(0).getAction();
        currentRoutine.cycle.currentAction.enter();
    }

    public boolean inAction(String name) {
        return currentRoutine.cycle.currentAction.name.equalsIgnoreCase(name);
    }

    public SuperAnimation getCurrentAnimation() {
        if (entity.damagedAction.active) {
            return entity.damagedAction.animation;
        }
        return currentRoutine.cycle.currentAction.animation;
    }

    public Routine getRoutine(String name) {
        for (Routine routine : routines) {
            if (routine.name.equals(name)) {
                return routine;
            }
        }
        throw new RuntimeException("No routine " + name + " in entity. For copy constructor, must copy Routines in correct order.");
    }

    //null - do not enter a routine
    //otherwise returns currentRoutine if POST_HIT_ROUTINE == null or a named routine
    public Routine getPostHitRoutine() {
        String POST_HIT_ROUTINE = currentRoutine.cycle.currentAction.POST_HIT_ROUTINE;
        if (POST_HIT_ROUTINE == null) {
            return currentRoutine;
        } if (POST_HIT_ROUTINE.equals("NONE")) {
            return null;
        } else {
            return getRoutine(POST_HIT_ROUTINE);
        }
    }

    public Routine getOnHittingPlayerInterruptAndGoTo() {
        return currentRoutine.cycle.currentAction.ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO;
    }
}
