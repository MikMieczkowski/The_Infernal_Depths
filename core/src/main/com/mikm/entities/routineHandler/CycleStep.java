package com.mikm.entities.routineHandler;

import com.mikm.entities.Entity;
import com.mikm.entities.actions.Action;

public interface CycleStep {
    CycleStep copy(Entity entity);
    Action getAction();
}
