package com.mikm._systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.mikm._components.*;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm.entities.DamageInformation;
import com.mikm.entities.actions.DamagedAction;
import com.mikm.rendering.screens.Application;

public class PlayerTriggerSystem extends IteratingSystem {
    public PlayerTriggerSystem() {
        super(Family.one(TriggerComponent.class, CombatComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        if (!Application.getInstance().systemShouldTick()) {
            return;
        }

        // Handle trigger components
        TriggerComponent triggerComponent = TriggerComponent.MAPPER.get(entity);
        if (triggerComponent != null) {
            for (Event event : triggerComponent.eventToAction.keySet()) {
                if (event.getCondition(entity)) {
                    triggerComponent.eventToAction.get(event).run(entity);
                }
            }
            triggerComponent.playerInsideLastFrame = triggerIntersectsPlayer(entity);
        }

        // Handle combat components
        CombatComponent combatComponent = CombatComponent.MAPPER.get(entity);
        if (combatComponent != null) {
            WorldColliderComponent collider = WorldColliderComponent.MAPPER.get(entity);
            RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(entity);
            if (collider != null && routineListComponent != null) {
                checkIfDamagedPlayer(entity, combatComponent, collider, routineListComponent);
            }
            handleInvincibility(combatComponent);
        }
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

    //enemy vs player
    //(eproj not made yet)
    private boolean checkIfDamagedPlayer(Entity entity, CombatComponent combatComponent, WorldColliderComponent collider, RoutineListComponent routineListComponent) {
        Transform transform = Transform.MAPPER.get(entity);

        Circle playerHitbox = Application.getInstance().getPlayerHitbox();
        if (combatComponent.DAMAGE == 0 || transform.ENTITY_NAME.equals("player")) {
            return false;
        }

        // Don't deal contact damage while in hitstun (DamagedAction active)
        DamagedAction.DamagedActionComponent damagedData = DamagedAction.MAPPER.get(entity);
        if (damagedData != null && damagedData.active) {
            return false;
        }
        boolean hitboxesOverlap = Intersector.overlaps(collider.getHitbox(transform), playerHitbox);
        if (hitboxesOverlap) {
            float angleToPlayer = MathUtils.atan2(playerHitbox.y - transform.y, playerHitbox.x - transform.x);
            routineListComponent.enterCurrentOnHittingPlayerRoutine(entity);
            RoutineListComponent playerRoutineListComponent = Application.getInstance().getPlayerRoutineListComponent();
            DamageInformation damageInformation = new DamageInformation(angleToPlayer, combatComponent.KNOCKBACK, combatComponent.DAMAGE);

            if (!Application.getInstance().getPlayerCombatComponent().isInvincible()) {
                playerRoutineListComponent.takeDamage(damageInformation, Application.getInstance().getPlayer());
            }
            return true;
        }
        return false;
    }

    private void handleInvincibility(CombatComponent combatComponent) {
        if (combatComponent.inInvincibilityFrames) {
            combatComponent.invincibilityTimer += Gdx.graphics.getDeltaTime();
            if (combatComponent.invincibilityTimer > combatComponent.maxInvincibilityTime) {
                combatComponent.invincibilityTimer = 0;
                combatComponent.inInvincibilityFrames = false;
            }
        }
    }
}
