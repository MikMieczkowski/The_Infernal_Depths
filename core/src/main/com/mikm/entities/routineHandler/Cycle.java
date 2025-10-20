package com.mikm.entities.routineHandler;

import com.badlogic.gdx.Gdx;
import com.mikm.entities.actions.Action;
import com.mikm.entities.Entity;
import com.mikm.entityLoader.Blackboard;
import com.mikm.entityLoader.EntityLoader;

import java.util.ArrayList;
import java.util.HashMap;

public class Cycle {
    private RoutineHandler routineHandler;
    ArrayList<CycleStep> cycleSteps;
    private Entity entity;

    //TODO remove public
    public Action currentAction;
    int i = 0;

    //copy constructor
    public Cycle(Entity entity, Cycle cycle) {
        this.entity = entity;
        this.routineHandler = entity.routineHandler;
        this.cycleSteps = new ArrayList<>();
        for (CycleStep step : cycle.cycleSteps) {
            this.cycleSteps.add(step.copy(entity));
        }
        currentAction = this.cycleSteps.get(0).getAction();
        if (currentAction == null) {
            throw new RuntimeException();
        }
    }

    public Cycle(Entity entity, ArrayList<CycleStep> cycleSteps) {
        this.entity = entity;
        this.routineHandler = entity.routineHandler;
        this.cycleSteps = cycleSteps;
        currentAction = cycleSteps.get(0).getAction();
        if (currentAction == null) {
            throw new RuntimeException();
        }
    }

    public void update() {
        currentAction.update();
        if ((currentAction.MAX_TIME != null && currentAction.timeElapsedInState > currentAction.MAX_TIME) || currentAction.IS_DONE) {
            endAction();
            i++;
            if (routineHandler.checkForTransition()) {
                return;
            }
            if (i >= cycleSteps.size()) {
                i = 0;
            }
            currentAction = cycleSteps.get(i).getAction();
            currentAction.enter();
        }
        if (routineHandler.CHECK_TRANSITIONS_EVERY_FRAME) {
            if (routineHandler.checkForTransition()) {
                return;
            }
        }
    }

    private void endAction() {
        for (String s: entity.usedActionClasses) {
            if (!s.equals(currentAction.name)) {
                //update time value of all actions except the one that just ended
                float f = (float)Blackboard.getInstance().getVar(entity, "timeSince" + s);
                Blackboard.getInstance().bind("timeSince" + s, entity, f+ currentAction.timeElapsedInState);
            }
        }
        currentAction.onExit();
    }
}