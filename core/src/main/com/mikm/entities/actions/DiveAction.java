package com.mikm.entities.actions;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm.utils.DeltaTime;
import com.mikm.utils.ExtraMathUtils;
import com.mikm.Vector2Int;
import com.mikm._components.Copyable;
import com.mikm._components.Transform;
import com.mikm.input.GameInput;
import com.mikm.rendering.sound.SoundEffects;
import com.mikm.entities.prefabLoader.Blackboard;

//Dives are 8 directional
public class DiveAction extends Action {
    @Copyable public float SPEED;
    @Copyable private float FRICTION;
    @Copyable private float FRICTION_SPEED;
    @Copyable private float STARTING_SIN_COUNT;
    //can play two sound effects simultaneously on start
    @Copyable private String START_SOUND_EFFECT;
    @Copyable private String START_SOUND_EFFECT2;

    private static final ComponentMapper<DiveActionComponent> MAPPER = ComponentMapper.getFor(DiveActionComponent.class);
    class DiveActionComponent implements Component {
        Vector2 diveVel = new Vector2();
        Vector2Int diveDirection = new Vector2Int();
        float sinCounter;
    }

    public DiveAction(){}

    @Override
    public Component createActionComponent() {
        return new DiveActionComponent();
    }

    @Override
    public void enter(Entity entity) {
        super.enter(entity);
        Transform transform = Transform.MAPPER.get(entity);
        DiveActionComponent data = MAPPER.get(entity);
        
        SoundEffects.play(START_SOUND_EFFECT);
        SoundEffects.play(START_SOUND_EFFECT2);
        transform.xVel = 0;
        transform.yVel = 0;
        data.sinCounter = STARTING_SIN_COUNT;

        float globalSpeed = transform.SPEED;
        data.diveVel = new Vector2(SPEED * globalSpeed * MathUtils.sin(data.sinCounter) * GameInput.getHorizontalAxis(),
                SPEED * globalSpeed * MathUtils.sin(data.sinCounter) * GameInput.getVerticalAxis());
        data.diveDirection = new Vector2Int(transform.direction.x, transform.direction.y);
        Blackboard.getInstance().bind("diveSinCounter", entity, data.sinCounter);
        super.update(entity);
    }

    @Override
    public void update(Entity entity) {
        super.update(entity);
        Transform transform = Transform.MAPPER.get(entity);
        DiveActionComponent data = MAPPER.get(entity);
        RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(entity);

        transform.xVel = data.diveVel.x;
        transform.yVel = data.diveVel.y;
        Blackboard.getInstance().bind("diveSinCounter", entity, data.sinCounter);
        setDiveForce(entity);
        if (data.sinCounter >= MathUtils.PI) {
            routineListComponent.CURRENT_ACTION_IS_DONE = true;
        }
    }

    private void setDiveForce(Entity entity) {
        DiveActionComponent data = MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);
        if (data.sinCounter < MathUtils.PI) {
            data.sinCounter += (FRICTION - (FRICTION_SPEED * FRICTION * data.sinCounter)) * DeltaTime.deltaTimeMultiplier();
        }

        float globalSpeed = transform.SPEED;
        Vector2 normalizedDiveDirection = ExtraMathUtils.normalizeAndScale(data.diveDirection);
        data.diveVel = new Vector2(SPEED * globalSpeed * MathUtils.sin(data.sinCounter) * normalizedDiveDirection.x,
                SPEED * globalSpeed * MathUtils.sin(data.sinCounter) * normalizedDiveDirection.y);
    }
}
