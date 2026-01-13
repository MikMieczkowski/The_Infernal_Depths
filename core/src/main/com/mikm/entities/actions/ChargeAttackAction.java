package com.mikm.entities.actions;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.mikm._components.Transform;
import com.mikm.entities.prefabLoader.Blackboard;
import com.mikm.entities.prefabLoader.PrefabInstantiator;

import javax.xml.transform.TransformerFactory;

public class ChargeAttackAction extends AcceleratedMoveAction {
    //LIGHT_TIME_MAX = 0
    private static final float MID_TIME_MAX = .3f, HEAVY_TIME_MAX = .6f;

    private static final ComponentMapper<ChargeAttackActionComponent> MAPPER = ComponentMapper.getFor(ChargeAttackActionComponent.class);
    class ChargeAttackActionComponent implements Component {
        float chargeTime;
    }
    @Override
    public Component createActionComponent() {
        return new ChargeAttackActionComponent();
    }

    @Override
    public void enter(Entity entity) {
        super.enter(entity);
        Blackboard.getInstance().bind("idleTimer", entity, 0);
    }

    @Override
    public void update(Entity entity) {
        super.update(entity);
        ChargeAttackActionComponent data = MAPPER.get(entity);
        data.chargeTime += Gdx.graphics.getDeltaTime();
    }

    @Override
    public void onExit(Entity entity) {
        super.onExit(entity);
        ChargeAttackActionComponent data = MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);
        if (data.chargeTime > HEAVY_TIME_MAX) {
            PrefabInstantiator.addProjectile(transform.x, transform.y);
        } else if (data.chargeTime > MID_TIME_MAX) {
            PrefabInstantiator.addProjectile(transform.x, transform.y);
        } else {
            PrefabInstantiator.addProjectile(transform.x, transform.y);
        }
    }
}
