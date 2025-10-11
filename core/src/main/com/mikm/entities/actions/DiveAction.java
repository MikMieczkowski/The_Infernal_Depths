package com.mikm.entities.actions;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mikm.DeltaTime;
import com.mikm.ExtraMathUtils;
import com.mikm.Vector2Int;
import com.mikm.entities.Entity;
import com.mikm.entities.animation.AnimationName;
import com.mikm.input.GameInput;
import com.mikm.rendering.sound.SoundEffects;
import com.mikm.entityLoader.Blackboard;

//Dives are 8 directional
public class DiveAction extends Action {
    private Vector2 diveVel = new Vector2();
    private Vector2Int diveDirection = new Vector2Int();
    private float sinCounter;

    public float SPEED;
    private float FRICTION;
    private float FRICTION_SPEED;
    private float STARTING_SIN_COUNT;
    private String START_SOUND_EFFECT;
    private String START_SOUND_EFFECT2;

    public DiveAction(Entity entity) {
        super(entity);
    }

    @Override
    public void enter() {
        super.enter();
        SoundEffects.play(START_SOUND_EFFECT);
        SoundEffects.play(START_SOUND_EFFECT2);
        entity.xVel = 0;
        entity.yVel = 0;
        sinCounter = STARTING_SIN_COUNT;

        diveVel = new Vector2(SPEED * MathUtils.sin(sinCounter) * GameInput.getHorizontalAxis(),
                SPEED * MathUtils.sin(sinCounter) * GameInput.getVerticalAxis());
        diveDirection = new Vector2Int(entity.direction.x, entity.direction.y);
        Blackboard.getInstance().bind("diveSinCounter", entity, sinCounter);
        super.update();
    }

    @Override
    public void update() {
        entity.xVel = diveVel.x;
        entity.yVel = diveVel.y;
        Blackboard.getInstance().bind("diveSinCounter", entity, sinCounter);
        setDiveForce();
        if (sinCounter >= MathUtils.PI) {
            IS_DONE = true;
        }
    }

    private void setDiveForce() {
        if (sinCounter < MathUtils.PI) {
            sinCounter += (FRICTION - (FRICTION_SPEED * FRICTION * sinCounter)) * DeltaTime.deltaTime();
        }

        Vector2 normalizedDiveDirection = ExtraMathUtils.normalizeAndScale(diveDirection);
        diveVel = new Vector2(SPEED * MathUtils.sin(sinCounter) * normalizedDiveDirection.x,
                SPEED * MathUtils.sin(sinCounter) * normalizedDiveDirection.y);
    }
}
