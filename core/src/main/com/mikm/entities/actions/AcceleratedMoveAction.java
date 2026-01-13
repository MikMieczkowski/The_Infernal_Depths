package com.mikm.entities.actions;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.mikm.utils.DeltaTime;
import com.mikm.utils.ExtraMathUtils;
import com.mikm._components.Copyable;
import com.mikm._components.Transform;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm.entities.prefabLoader.Blackboard;
import com.mikm.input.GameInput;
import com.mikm.rendering.sound.SoundEffects;

import java.util.function.Supplier;


public class AcceleratedMoveAction extends Action {
    @Copyable private float ACCELERATION_FRAMES;
    @Copyable private float DECELERATION_FRAMES;
    @Copyable private String STEP_SOUND_EFFECT;
    @Copyable private String MOVEMENT_DIRECTION_TYPE;
    private static final float STEP_MAX = .66f;

    @Override
    public Component createActionComponent() {
        return new AcceleratedMoveComponent();
    }

    public AcceleratedMoveAction() {

    }


    private static final ComponentMapper<AcceleratedMoveComponent> MAPPER = ComponentMapper.getFor(AcceleratedMoveComponent.class);
    class AcceleratedMoveComponent implements Component {
        Supplier<Vector2> movementDirection;

        Vector2 targetVelocity = new Vector2();
        Vector2 startingVelocity = new Vector2();
        float acc;
        float dec;
        float stepTimer = 0;

        float idleTimer = 0;

        @Deprecated
        int framesMouseHeld;
    }

    @Override
    public void postConfigRead(com.badlogic.ashley.core.Entity entity) {
        super.postConfigRead(entity);
        AcceleratedMoveComponent data = MAPPER.get(entity);
        if (MOVEMENT_DIRECTION_TYPE.equals("InputAxis")) {
            data.movementDirection = () -> new Vector2(GameInput.getHorizontalAxis(), GameInput.getVerticalAxis());
        } else if (MOVEMENT_DIRECTION_TYPE.equals("Wander")) {
            float shouldBeInConfig_min = .2f;
            float shouldBeInConfig_max = 1f;
            data.movementDirection = () -> new Vector2(
                    ExtraMathUtils.getRandomWanderVel(shouldBeInConfig_min, shouldBeInConfig_max), ExtraMathUtils.getRandomWanderVel(shouldBeInConfig_min, shouldBeInConfig_max));
        } else {
            throw new RuntimeException("Undefined AcceleratedMove MOVEMENT_DIRECTION_TYPE " + MOVEMENT_DIRECTION_TYPE);
        }
    }

    @Override
    public void enter(com.badlogic.ashley.core.Entity entity) {
        super.enter(entity);
        AcceleratedMoveComponent data = MAPPER.get(entity);
        data.idleTimer = 0;
        Blackboard.getInstance().bind("idleTimer", entity, 0);
    }

    @Override
    public void update(com.badlogic.ashley.core.Entity entity) {
        AcceleratedMoveComponent data = MAPPER.get(entity);
        super.update(entity);

        Transform transform = Transform.MAPPER.get(entity);

        if (GameInput.isAttackButtonPressed()) {
            data.framesMouseHeld++;
        } else {
            data.framesMouseHeld = 0;
        }
        //System.out.println(data.framesMouseHeld);

        data.acc = transform.SPEED * DeltaTime.deltaTime() / ACCELERATION_FRAMES;
        data.dec = transform.SPEED * DeltaTime.deltaTime() / DECELERATION_FRAMES;
        data.stepTimer += Gdx.graphics.getDeltaTime();
        if (data.stepTimer > STEP_MAX && (transform.xVel != 0 || transform.yVel != 0)) {
            data.stepTimer -= STEP_MAX;
            SoundEffects.play(STEP_SOUND_EFFECT);
        };
        if (data.movementDirection.get().x != 0) {
            xAccelerate(entity);
        } else {
            xDecelerate(entity);
        }

        if (data.movementDirection.get().y != 0) {
            yAccelerate(entity);
        } else {
            yDecelerate(entity);
        }

        if (transform.xVel == 0 && transform.yVel == 0) {
            data.idleTimer += Gdx.graphics.getDeltaTime();
            Blackboard.getInstance().bind("idleTimer", entity, data.idleTimer);
        } else {
            data.idleTimer = 0;
            Blackboard.getInstance().bind("idleTimer", entity, data.idleTimer);
        }
    }


    private void xAccelerate(com.badlogic.ashley.core.Entity entity) {
        AcceleratedMoveComponent data = MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);
        RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(entity);

        transform.xVel += data.acc * data.movementDirection.get().x;
        clampVelocityX(entity);
        data.startingVelocity.x = transform.xVel;
    }

    private void yAccelerate(com.badlogic.ashley.core.Entity entity) {
        AcceleratedMoveComponent data = MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);
        RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(entity);

        transform.yVel += data.acc * data.movementDirection.get().y;
        clampVelocityY(entity);
        data.startingVelocity.y = transform.yVel;
    }

    private void xDecelerate(com.badlogic.ashley.core.Entity entity) {
        AcceleratedMoveComponent data = MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);
        RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(entity);

        transform.xVel -= data.dec * ExtraMathUtils.sign(data.startingVelocity.x);
        if (!ExtraMathUtils.haveSameSign(transform.xVel, data.startingVelocity.x)) {
            transform.xVel = 0;
        }
    }

    private void yDecelerate(com.badlogic.ashley.core.Entity entity) {
        AcceleratedMoveComponent data = MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);
        RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(entity);

        transform.yVel -= data.dec * ExtraMathUtils.sign(data.startingVelocity.y);
        if (!ExtraMathUtils.haveSameSign(transform.yVel, data.startingVelocity.y)) {
            transform.yVel = 0;
        }
    }

    private void clampVelocityX(com.badlogic.ashley.core.Entity entity) {
        AcceleratedMoveComponent data = MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);

        if (data.movementDirection.get().x != 0) {
            data.targetVelocity.x = transform.SPEED * data.movementDirection.get().x;
        }
        float topXSpeed = Math.abs(data.targetVelocity.x);

        if (transform.xVel < -topXSpeed) {
            transform.xVel = -topXSpeed;
        }
        if (transform.xVel > topXSpeed) {
            transform.xVel = topXSpeed;
        }
    }

    private void clampVelocityY(com.badlogic.ashley.core.Entity entity) {
        AcceleratedMoveComponent data = MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);

        if (data.movementDirection.get().y != 0) {
            data.targetVelocity.y = transform.SPEED * data.movementDirection.get().y;
        }
        float topYSpeed = Math.abs(data.targetVelocity.y);

        if (transform.yVel < -topYSpeed) {
            transform.yVel = -topYSpeed;
        }
        if (transform.yVel > topYSpeed) {
            transform.yVel = topYSpeed;
        }
    }
}
