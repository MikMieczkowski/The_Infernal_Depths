package com.mikm.entities.actions;

import com.badlogic.gdx.Gdx;
import com.mikm.entities.animation.SuperAnimation;
import com.mikm.entities.Entity;
import com.mikm.entities.routineHandler.Routine;
import com.mikm.entityLoader.Blackboard;
import com.mikm.entityLoader.EntityActionLoader;

import java.util.Map;
import java.util.Objects;


//doing nothing bug may mean the enemy is running a different entities routines or actions
public abstract class Action {
    public Entity entity;
    public String name;
    public SuperAnimation animation;
    public float timeElapsedInState = 0;
    public Float MAX_TIME;
    public boolean IS_DONE;
    public Routine ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO;

    // Optional per-behaviour control: when this entity is hit, choose which routine to enter.
    // If null/empty, default to re-entering the current routine (reset cycle).
    // If set to "NONE", no routine is entered and current action continues (no interrupt).
    public String POST_HIT_ROUTINE;

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
        // First apply configVars if present, then reflectively copy scalar config fields
        // from this instance to preserve COPY_CONFIG inheritance across entity copies.
        output.configVars = this.configVars;
        if (this.configVars != null) {
            for (Map.Entry<String, Object> entry : this.configVars.entrySet()) {
                String varName = entry.getKey();
                //"speed_min" -> "SPEED_MIN"
                varName = varName.toUpperCase();
                Object varValue = entry.getValue();
                EntityActionLoader.setVarInClass(this.getClass(), output, varName, varValue, this.getClass().getSimpleName());
            }
        }
        output.POST_HIT_ROUTINE = this.POST_HIT_ROUTINE;
        // Reflectively copy scalar fields (String/boxed/primitive numeric/boolean) from this -> output
        // to retain resolved values from COPY_CONFIG chains that are not present in configVars.
        java.lang.Class<?> current = this.getClass();
        while (current != null && Action.class.isAssignableFrom(current)) {
            for (java.lang.reflect.Field f : current.getDeclaredFields()) {
                String fieldName = f.getName();
                if (fieldName.equals("entity") || fieldName.equals("name") || fieldName.equals("animation") ||
                        fieldName.equals("timeElapsedInState") || fieldName.equals("IS_DONE") ||
                        fieldName.equals("ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO") || fieldName.equals("configVars")) {
                    continue;
                }
                f.setAccessible(true);
                Class<?> type = f.getType();
                boolean isScalar = type.isPrimitive() ||
                        type == java.lang.String.class ||
                        type == java.lang.Integer.class || type == java.lang.Float.class || type == java.lang.Double.class ||
                        type == java.lang.Long.class || type == java.lang.Short.class || type == java.lang.Byte.class ||
                        type == java.lang.Boolean.class || type == java.lang.Character.class;
                if (!isScalar) {
                    continue;
                }
                try {
                    Object value = f.get(this);
                    f.set(output, value);
                } catch (IllegalAccessException ignored) {
                }
            }
            current = current.getSuperclass();
        }
        output.postConfigRead();
        return output;
    }

    public Action(Entity entity) {
        this.entity = entity;
    }

    //should only be callable by Cycle (from RoutineHandler) and this class
    public void enter() {
        if (!Objects.equals(entity.NAME, "player")) {
            System.out.println(entity.NAME + "@" + Integer.toHexString(entity.hashCode()) + " entering action: " + name);
        }
        entity.height = 0;
        IS_DONE = false;
        timeElapsedInState = 0;
        entity.animationHandler.changeAnimation(animation);
    }

    public void postConfigRead() {
        Blackboard.getInstance().bind("timeSince" + name, entity, 0f);
    }

    public void onExit() {

    }

    public void update() {
        timeElapsedInState += Gdx.graphics.getDeltaTime();
        entity.animationHandler.update();

    }
}
