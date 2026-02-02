package com.mikm._components;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.mikm.input.GameInput;
import com.mikm.rendering.screens.Application;

import java.util.function.Predicate;

public enum Event {
    ON_ENTER(e-> playerIntersectsAndColliderActive(e) && !TriggerComponent.MAPPER.get(e).playerInsideLastFrame),
    ON_STAY(e-> playerIntersectsAndColliderActive(e)),
    ON_STAY_AND_INPUT_JUST_PRESSED(e-> {
        TriggerComponent t = TriggerComponent.MAPPER.get(e);
        return playerIntersectsAndColliderActive(e) && t.inputAction != null && GameInput.isActionJustPressed(t.inputAction);
    }),
    ON_PLAYER_PROJECTILE_STAY(e->anyPlayerProjectileIntersects(e)),
    ON_PLAYER_MINING_PROJECTILE_STAY(e->anyPlayerMiningProjectileIntersects(e));


    private final Predicate<Entity> condition;

    Event(Predicate<Entity> condition) {
        this.condition = condition;
    }

    public boolean getCondition(Entity entity) {
        return condition.test(entity);
    }

    private static boolean playerIntersectsAndColliderActive(Entity trigger) {
        TriggerComponent triggerComponent = TriggerComponent.MAPPER.get(trigger);
        Transform transform = Transform.MAPPER.get(trigger);

        //if triggeredByPProj or triggeredByMiningPProj then if proj is miningPProj and check all collisions for mining pproj's vs rock and then pproj's vs destr,
        //then return true. They will only have onEnter, which will be TriggerAction.break.


        //+8 so that the hitbox coords can be of the nearest tile
        //DebugRenderer.getInstance().drawHitboxes( new Circle(transform.x+8, transform.y+8, triggerComponent.diameter/2f));
        return Application.getInstance().getPlayerCollider().active &&
                Intersector.overlaps(Application.getInstance().getPlayerHitbox(), new Circle(transform.x+8, transform.y+8, triggerComponent.diameter/2));
    }

    private static final float PROJECTILE_RADIUS = 4f;

    //Can be optimized with a listener component for each type and then only looping through those entities. unless this comment ends up being wrong
    private static boolean anyPlayerProjectileIntersects(Entity e) {
        TriggerComponent triggerComponent = TriggerComponent.MAPPER.get(e);
        Transform transform = Transform.MAPPER.get(e);
        Circle triggerHitbox = new Circle(transform.x+8, transform.y+8, triggerComponent.diameter/2f);

        for (Entity projectile : Application.getInstance().currentScreen.engine.getEntitiesFor(Family.all(ProjectileComponent.class).get())) {
            if (ProjectileComponent.MAPPER.get(projectile).isPlayer) {
                Transform projectileTransform = Transform.MAPPER.get(projectile);
                Circle projectileHitbox = new Circle(projectileTransform.getCenteredX(), projectileTransform.getCenteredY(), PROJECTILE_RADIUS);
                if (Intersector.overlaps(projectileHitbox, triggerHitbox)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean anyPlayerMiningProjectileIntersects(Entity e) {
        TriggerComponent triggerComponent = TriggerComponent.MAPPER.get(e);
        Transform transform = Transform.MAPPER.get(e);
        Circle triggerHitbox = new Circle(transform.x+8, transform.y+8, triggerComponent.diameter/2f);

        for (Entity projectile : Application.getInstance().currentScreen.engine.getEntitiesFor(Family.all(MiningProjectileComponent.class).get())) {
            Transform projectileTransform = Transform.MAPPER.get(projectile);
            Circle projectileHitbox = new Circle(projectileTransform.getCenteredX(), projectileTransform.getCenteredY(), PROJECTILE_RADIUS);
            if (Intersector.overlaps(projectileHitbox, triggerHitbox)) {
                return true;
            }
        }
        return false;
    }
}


