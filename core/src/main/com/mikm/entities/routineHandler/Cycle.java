package com.mikm.entities.enemies;

import com.badlogic.gdx.Gdx;
import com.mikm.entities.enemies.cyclestep.CycleStep;

import java.util.ArrayList;

public class Cycle {
    private RoutineHandler routineHandler;
    ArrayList<CycleStep> cycleSteps;

    private Behaviour currentBehaviour;
    int i = 0;

    Cycle(Entity entity, ArrayList<CycleStep> cycleSteps) {
        this.routineHandler = entity.routineHandler;
        this.cycleSteps = cycleSteps;
        currentBehaviour = cycleSteps.get(0).getBehaviour();
        currentBehaviour.enter();
    }

    public void update() {
        currentBehaviour.update();
        currentBehaviour.timeElapsedInState += Gdx.graphics.getDeltaTime();
        if (currentBehaviour.timeElapsedInState > currentBehaviour.MAX_TIME) {
            currentBehaviour.onExit();
            //update time since behaviours
            for (Behaviour behaviour : routineHandler.timeSinceBehaviour.keySet()) {
                if (behaviour != currentBehaviour) {
                    //update time value
                    routineHandler.timeSinceBehaviour.put(behaviour, routineHandler.timeSinceBehaviour.get(behaviour) + currentBehaviour.MAX_TIME);
                }
            }
            i++;
            if (routineHandler.checkForTransition()) {
                return;
            }
            if (i >= cycleSteps.size()) {
                i = 0;
            }
            currentBehaviour = cycleSteps.get(i).getBehaviour();
            currentBehaviour.enter();
        }
    }
}
