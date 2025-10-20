package com.mikm.entities.actions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.mikm.DeltaTime;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.Entity;
import com.mikm.entityLoader.Blackboard;
import com.mikm.input.GameInput;
import com.mikm.rendering.sound.SoundEffects;

import java.util.function.Supplier;

public class AcceleratedMoveAction extends Action {

    private float ACCELERATION_FRAMES;
    private float DECELERATION_FRAMES;
    private String STEP_SOUND_EFFECT;
    private String MOVEMENT_DIRECTION_TYPE;
    private Supplier<Vector2> movementDirection;

    private Vector2 targetVelocity = new Vector2();
    private Vector2 startingVelocity = new Vector2();
    private final float STEP_MAX = .66f;
    float acc;
    float dec;
    private float stepTimer = 0;

    private float idleTimer = 0;

    public AcceleratedMoveAction(Entity entity) {
        super(entity);
    }

    @Override
    public void postConfigRead() {
        super.postConfigRead();
        if (MOVEMENT_DIRECTION_TYPE.equals("InputAxis")) {
            movementDirection = () -> new Vector2(GameInput.getHorizontalAxis(), GameInput.getVerticalAxis());
        } else if (MOVEMENT_DIRECTION_TYPE.equals("Wander")) {
            float shouldBeInConfig_min = .2f;
            float shouldBeInConfig_max = 1f;
            movementDirection = () -> new Vector2(
                    ExtraMathUtils.getRandomWanderVel(shouldBeInConfig_min, shouldBeInConfig_max), ExtraMathUtils.getRandomWanderVel(shouldBeInConfig_min, shouldBeInConfig_max));
        } else {
            throw new RuntimeException("Undefined AcceleratedMove MOVEMENT_DIRECTION_TYPE " + MOVEMENT_DIRECTION_TYPE);
        }
    }

    @Override
    public void enter() {
        super.enter();
        idleTimer = 0;
        Blackboard.getInstance().bind("idleTimer", entity, 0);
    }

    @Override
    public void update() {
        super.update();
        acc = entity.SPEED * DeltaTime.deltaTime() / ACCELERATION_FRAMES;
        dec = entity.SPEED * DeltaTime.deltaTime() / DECELERATION_FRAMES;
        stepTimer -= Gdx.graphics.getDeltaTime();
        if (stepTimer < 0 && (entity.xVel != 0 || entity.yVel != 0)) {
            stepTimer += STEP_MAX;
            SoundEffects.play(STEP_SOUND_EFFECT);
        };
        if (movementDirection.get().x != 0) {
            xAccelerate();
        } else {
            xDecelerate();
        }

        if (movementDirection.get().y != 0) {
            yAccelerate();
        } else {
            yDecelerate();
        }

        if (entity.xVel == 0 && entity.yVel == 0) {
            idleTimer += Gdx.graphics.getDeltaTime();
            Blackboard.getInstance().bind("idleTimer", entity, idleTimer);
        } else {
            idleTimer = 0;
            Blackboard.getInstance().bind("idleTimer", entity, idleTimer);
        }
    }

    private void xAccelerate() {
        entity.xVel += acc * movementDirection.get().x;
        clampVelocityX();
        startingVelocity.x = entity.xVel;
    }

    private void yAccelerate() {
        entity.yVel += acc * movementDirection.get().y;
        clampVelocityY();
        startingVelocity.y = entity.yVel;
    }

    private void xDecelerate() {
        entity.xVel -= dec * ExtraMathUtils.sign(startingVelocity.x);
        if (!ExtraMathUtils.haveSameSign(entity.xVel, startingVelocity.x)) {
            entity.xVel = 0;
        }
    }

    private void yDecelerate() {
        entity.yVel -= dec * ExtraMathUtils.sign(startingVelocity.y);
        if (!ExtraMathUtils.haveSameSign(entity.yVel, startingVelocity.y)) {
            entity.yVel = 0;
        }
    }

    private void clampVelocityX() {
        if (movementDirection.get().x != 0) {
            targetVelocity.x = entity.SPEED * movementDirection.get().x;
        }
        float topXSpeed = Math.abs(targetVelocity.x);

        if (entity.xVel < -topXSpeed) {
            entity.xVel = -topXSpeed;
        }
        if (entity.xVel > topXSpeed) {
            entity.xVel = topXSpeed;
        }
    }

    private void clampVelocityY() {
        if (movementDirection.get().y != 0) {
            targetVelocity.y = entity.SPEED * movementDirection.get().y;
        }
        float topYSpeed = Math.abs(targetVelocity.y);

        if (entity.yVel < -topYSpeed) {
            entity.yVel = -topYSpeed;
        }
        if (entity.yVel > topYSpeed) {
            entity.yVel = topYSpeed;
        }
    }
}
