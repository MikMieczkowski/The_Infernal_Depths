package com.mikm.entities.actions;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.mikm.utils.RandomUtils;
import com.mikm._components.CombatComponent;
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
        // Save velocity before super.enter() clears residual knockback flag
        Transform transform = Transform.MAPPER.get(entity);
        CombatComponent combat = CombatComponent.MAPPER.get(entity);
        float savedXVel = transform.xVel;
        float savedYVel = transform.yVel;
        float savedHeight = transform.height;
        boolean wasResidual = combat != null && combat.inResidualKnockback;

        super.enter(entity);
        SoundEffects.play(START_SOUND_EFFECT);
        MAX_TIME = RandomUtils.getFloat(TIME_MIN, TIME_MAX);

        if (wasResidual) {
            // Restore velocity and height for residual knockback deceleration
            transform.xVel = savedXVel;
            transform.yVel = savedYVel;
            transform.height = savedHeight;
            combat.inResidualKnockback = true; // re-set since super.enter() cleared it
        } else {
            transform.xVel = 0;
            transform.yVel = 0;
        }
    }

    @Override
    public void update(Entity entity) {
        super.update(entity);
        CombatComponent combat = CombatComponent.MAPPER.get(entity);
        if (combat != null && combat.inResidualKnockback) {
            Transform transform = Transform.MAPPER.get(entity);
            float friction = 0.85f;
            transform.xVel *= friction;
            transform.yVel *= friction;
            if (transform.height > 0) {
                transform.height = Math.max(0, transform.height - 0.5f);
            }
            if (Math.abs(transform.xVel) < 0.1f && Math.abs(transform.yVel) < 0.1f && transform.height <= 0) {
                transform.xVel = 0;
                transform.yVel = 0;
                transform.height = 0;
                combat.inResidualKnockback = false;
            }
        }
    }
}
