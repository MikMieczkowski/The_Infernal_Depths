package com.mikm.entities.actions;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.mikm.utils.RandomUtils;
import com.mikm._components.Copyable;
import com.mikm._components.Transform;
import com.mikm.rendering.sound.SoundEffects;

public class IdleAction extends Action {
    @Copyable private float TIME_MIN = 1;
    @Copyable private float TIME_MAX = 1;
    @Copyable private String START_SOUND_EFFECT;

    private static final ComponentMapper<IdleActionComponent> MAPPER = ComponentMapper.getFor(IdleActionComponent.class);
    class IdleActionComponent implements Component {
        // No state needed
    }

    public IdleAction(){}

    @Override
    public Component createActionComponent() {
        return new IdleActionComponent();
    }

    @Override
    public void enter(Entity entity) {
        super.enter(entity);
        Transform transform = Transform.MAPPER.get(entity);
        SoundEffects.play(START_SOUND_EFFECT);
        transform.xVel = 0;
        transform.yVel = 0;
        MAX_TIME = RandomUtils.getFloat(TIME_MIN, TIME_MAX);
    }

    //does nothing
}
