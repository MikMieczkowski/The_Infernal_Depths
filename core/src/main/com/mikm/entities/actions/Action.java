package com.mikm.entities.actions;

import com.badlogic.gdx.Gdx;
import com.mikm.entities.animation.SuperAnimation;
import com.mikm.entities.Entity;
import com.mikm.entities.routineHandler.Routine;
import com.mikm.entityLoader.Blackboard;
import com.mikm.entityLoader.EntityData;
import com.mikm.entityLoader.EntityLoader;
import org.w3c.dom.ls.LSOutput;

import java.util.Map;
import java.util.Objects;


public abstract class Action {
    public Entity entity;
    public String name;
    public SuperAnimation animation;
    public float timeElapsedInState = 0;
    public float MAX_TIME = 0;
    public boolean IS_DONE;
    public Routine ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO;

    public Map<String, Object> configVars;

    //copy constructor
    public Action copy(Entity entity) {
        Action output;
        try {
            //this.getClass is a subclass of action
            output = this.getClass().getConstructor(Entity.class).newInstance(entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        output.entity = entity;
        output.name = this.name;
        output.animation = this.animation.copy();
        output.MAX_TIME = this.MAX_TIME;
        //Output's interrupt routine = copy of corresponding routine in this object's interrupt routine
        //Can do it this way because the mentioned Routine should already be copied by now - must copy in order

        if (this.ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO != null) {
            String thisInterruptRoutineName = this.ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO.name;
            Routine entityInterruptRoutine = entity.routineHandler.getRoutine(thisInterruptRoutineName);
            output.ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO = entityInterruptRoutine;
        }

        //Set output's configvars to these configvars. Don't need to do output.configvars = entity.configVars because output should not be used to copy other entities, but did it anyway
        output.configVars = this.configVars;
        if (this.configVars == null) {
            return output;
        }
        for (Map.Entry<String, Object> entry : this.configVars.entrySet()) {
            String varName = entry.getKey();
            //"speed_min" -> "SPEED_MIN"
            varName = varName.toUpperCase();

            Object varValue = entry.getValue();
            EntityLoader.setVarInClass(this.getClass(), output, varName, varValue);
        }
        // Apply defaults that depend on the bound entity (e.g., set SPEED from entity if unset)
        output.postConfigRead();
        return output;
    }

    public Action(Entity entity) {
        this.entity = entity;
    }

    //should only be callable by Cycle (from RoutineHandler) and this class
    public void enter() {
        if (!Objects.equals(entity.NAME, "player")) {
            //System.out.println(entity.NAME + "@" + Integer.toHexString(entity.hashCode()) + " entering action: " + name);
        }
        entity.height = 0;
        IS_DONE = false;
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
    }
}
