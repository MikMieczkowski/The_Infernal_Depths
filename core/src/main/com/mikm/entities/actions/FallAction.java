package com.mikm.entities.actions;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.mikm._components.Copyable;
import com.mikm._components.Transform;
import com.mikm._components.CombatComponent;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.sound.SoundEffects;

//Use idleaction and handle onExit elsewhere?
public class FallAction extends Action {
    @Copyable private String START_SOUND_EFFECT;

    private static final ComponentMapper<FallActionComponent> MAPPER = ComponentMapper.getFor(FallActionComponent.class);
    class FallActionComponent implements Component {
        // No state needed
    }

    public FallAction(){}

    @Override
    public Component createActionComponent() {
        return new FallActionComponent();
    }

    @Override
    public void enter(Entity entity) {
        super.enter(entity);
        Transform transform = Transform.MAPPER.get(entity);
        CombatComponent combatComponent = CombatComponent.MAPPER.get(entity);
        
        transform.xVel = 0;
        transform.yVel = 0;
        transform.height = 0;
        combatComponent.setAttackable(false);
        SoundEffects.play(START_SOUND_EFFECT);
    }

    @Override
    public void update(Entity entity) {
        super.update(entity);
    }

    @Override
    public void onExit(Entity entity) {
        CombatComponent combatComponent = CombatComponent.MAPPER.get(entity);
        Application.getInstance().setGameScreen(Application.getInstance().caveScreen);
        Application.getInstance().caveScreen.increaseFloor();
        combatComponent.setAttackable(true);
        super.onExit(entity);
    }
}
