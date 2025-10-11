package com.mikm.entities.enemies.cyclestep;

import com.mikm.entities.enemies.Cycle;
import com.mikm.entities.enemies.Entity;
import com.mikm.entities.enemies.Routine;

import java.util.function.Predicate;

public class ConditionTransition {
    public Predicate<Entity> pred;
    public Routine goTo;
    private Cycle current;

    public ConditionTransition(Predicate<Entity> pred, Routine goTo) {
        this.pred = pred;
        this.goTo = goTo;
    }

    public boolean condition(Entity entity) {
        return pred.test(entity);
    }
}
