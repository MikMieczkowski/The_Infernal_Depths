package com.mikm.entities.actions;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.mikm.utils.ExtraMathUtils;
import com.mikm._components.Copyable;
import com.mikm._components.Transform;

public class WanderAction extends Action {
    @Copyable private float SPEED_MIN;
    @Copyable private float SPEED_MAX;

    private static final ComponentMapper<WanderActionComponent> MAPPER = ComponentMapper.getFor(WanderActionComponent.class);
    class WanderActionComponent implements Component {
        // No state needed
    }

    public WanderAction(){}

    @Override
    public Component createActionComponent() {
        return new WanderActionComponent();
    }

    @Override
    public void enter(Entity entity) {
        super.enter(entity);
        Transform transform = Transform.MAPPER.get(entity);
        transform.xVel = ExtraMathUtils.getRandomWanderVel(SPEED_MIN, SPEED_MAX);
        transform.yVel = ExtraMathUtils.getRandomWanderVel(SPEED_MIN, SPEED_MAX);
    }
}
