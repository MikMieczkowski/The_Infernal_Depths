package com.mikm.entities.actions;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.mikm._components.Copyable;
import com.mikm._components.EffectsComponent;
import com.mikm._components.Transform;

public class SquishAction extends Action {
    @Copyable private float AMOUNT;

    private static final ComponentMapper<SquishActionComponent> MAPPER = ComponentMapper.getFor(SquishActionComponent.class);
    class SquishActionComponent implements Component {
        // No state needed
    }

    public SquishAction(){}

    @Override
    public Component createActionComponent() {
        return new SquishActionComponent();
    }

    @Override
    public void enter(Entity entity) {
        super.enter(entity);
        Transform transform = Transform.MAPPER.get(entity);
        transform.xVel = 0;
        transform.yVel = 0;
        EffectsComponent.MAPPER.get(entity).startSquish(0, AMOUNT, MAX_TIME, true);
    }

    @Override
    public void update(Entity entity) {
        super.update(entity);
        Transform transform = Transform.MAPPER.get(entity);
        transform.xVel = 0;
        transform.yVel = 0;
    }
}
