package com.mikm._systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.mikm._components.Event;
import com.mikm._components.Transform;
import com.mikm._components.TriggerComponent;
import com.mikm.input.GameInput;
import com.mikm.rendering.screens.Application;

public class PlayerTriggerSystem extends IteratingSystem {
    public PlayerTriggerSystem() {
        super(Family.all(TriggerComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        if (!Application.getInstance().systemShouldTick()) {
            return;
        }
        TriggerComponent triggerComponent = TriggerComponent.MAPPER.get(entity);

        for (Event event : triggerComponent.eventToAction.keySet()) {
            if (event.getCondition(entity)) {
                triggerComponent.eventToAction.get(event).run(entity);
            }
        }

        triggerComponent.playerInsideLastFrame = triggerIntersectsPlayer(entity);
    }

    private boolean triggerIntersectsPlayer(Entity trigger) {
        TriggerComponent triggerComponent = TriggerComponent.MAPPER.get(trigger);
        Transform transform = Transform.MAPPER.get(trigger);

        //if triggeredByPProj or triggeredByMiningPProj then if proj is miningPProj and check all collisions for mining pproj's vs rock and then pproj's vs destr,
        //then return true. They will only have onEnter, which will be TriggerAction.break.


        //+8 so that the hitbox coords can be of the nearest tile
        //DebugRenderer.getInstance().drawHitboxes( new Circle(transform.x+8, transform.y+8, triggerComponent.diameter/2f));
        return Intersector.overlaps(Application.getInstance().getPlayerHitbox(), new Circle(transform.x+8, transform.y+8, triggerComponent.diameter/2));
    }
}
