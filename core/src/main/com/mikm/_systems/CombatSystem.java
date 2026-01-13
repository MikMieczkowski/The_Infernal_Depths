package com.mikm._systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.mikm._components.WorldColliderComponent;
import com.mikm._components.CombatComponent;
import com.mikm._components.Transform;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm.entities.DamageInformation;
import com.mikm.rendering.screens.Application;

//only if Application !timestop and !paused
public class CombatSystem extends IteratingSystem {
    public CombatSystem() {
        super(Family.all(CombatComponent.class, WorldColliderComponent.class, RoutineListComponent.class, Transform.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float v) {
        if (!Application.getInstance().systemShouldTick()) {
            return;
        }
        checkIfDamagedPlayer(entity);
        handleInvincibility(CombatComponent.MAPPER.get(entity));
    }

    //enemy vs player
    //(eproj not made yet)
    private boolean checkIfDamagedPlayer(Entity entity) {
        Transform transform = Transform.MAPPER.get(entity);
        CombatComponent combatComponent = CombatComponent.MAPPER.get(entity);
        WorldColliderComponent collider = WorldColliderComponent.MAPPER.get(entity);
        RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(entity);

        Circle playerHitbox = Application.getInstance().getPlayerHitbox();
        if (combatComponent.DAMAGE == 0 || transform.ENTITY_NAME.equals("player")) {
            return false;
        }
        boolean hitboxesOverlap = Intersector.overlaps(collider.getHitbox(transform), playerHitbox);
        if (hitboxesOverlap) {
            float angleToPlayer = MathUtils.atan2(playerHitbox.y - transform.y, playerHitbox.x - transform.x);
            routineListComponent.enterCurrentOnHittingPlayerRoutine(entity);
            RoutineListComponent playerRoutineListComponent = Application.getInstance().getPlayerRoutineListComponent();
            DamageInformation damageInformation = new DamageInformation(angleToPlayer, combatComponent.DAMAGE, combatComponent.KNOCKBACK);

            if (!Application.getInstance().getPlayerCombatComponent().isInvincible()) {
                playerRoutineListComponent.takeDamage(damageInformation, Application.getInstance().getPlayer());
            }
            return true;
        }
        return false;
    }

    private void handleInvincibility(CombatComponent effectsComponent) {
        if (effectsComponent.inInvincibilityFrames) {
            effectsComponent.invincibilityTimer += Gdx.graphics.getDeltaTime();
            if (effectsComponent.invincibilityTimer > effectsComponent.maxInvincibilityTime) {
                effectsComponent.invincibilityTimer = 0;
                effectsComponent.inInvincibilityFrames = false;
            }
        }
    }
}
