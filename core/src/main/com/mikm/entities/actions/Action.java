package com.mikm.entities.actions;

import com.badlogic.gdx.Gdx;
import com.mikm.entities.animation.SuperAnimation;
import com.mikm.entities.Entity;
import com.mikm.entities.routineHandler.Routine;


public abstract class Behaviour {
    public final Entity entity;
    public SuperAnimation animation;
    public float timeElapsedInState;
    public float MAX_TIME;
    public Routine ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO;

    public Behaviour(Entity entity) {
        this.entity = entity;
    }

    //should only be callable by CycleHandler
    public void enter() {
        timeElapsedInState = 0;
        entity.animationHandler.changeAnimation(animation);
    }

    public void postConfigRead() {

    }

    public void onExit() {

    }

    public void update() {
        timeElapsedInState += Gdx.graphics.getDeltaTime();
        entity.animationHandler.update();
        if (entity.DAMAGE != 0) {
            //handlePlayerCollision(entity.DAMAGE, entity.)
        }
    }

}
