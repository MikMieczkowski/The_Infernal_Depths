package com.mikm._systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.math.MathUtils;
import com.mikm._components.ComboStateComponent;
import com.mikm._components.LockOnComponent;
import com.mikm._components.MiningProjectileComponent;
import com.mikm._components.ProjectileComponent;
import com.mikm._components.ProjectileConfigComponent;
import com.mikm._components.SpriteComponent;
import com.mikm._components.Transform;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm.entities.prefabLoader.weapon.WeaponFormattedData;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.mikm.entities.actions.ChargeAction;
import com.mikm.entities.actions.OrbitPlayerAction;
import com.mikm.entities.animation.SingleAnimation;
import com.mikm.entities.animation.SingleFrame;
import com.mikm.entities.animation.SuperAnimation;
import com.mikm.entities.prefabLoader.PrefabInstantiator;
import com.mikm.entities.prefabLoader.attack.AttackFormattedData;
import com.mikm.rendering.screens.Application;

/**
 * System that handles spawning projectiles during attacks.
 * Filters projectiles by CREATE_ON and configures them from attack data.
 */
public class ProjectileSpawnSystem extends EntitySystem {

    /**
     * Spawns projectiles from attack data that match the given timing.
     * Applies PROJECTILE_DAMAGE overrides from weapon CONFIG if available.
     */
    public void spawnProjectiles(Entity entity, AttackFormattedData attackData, String createOn) {
        if (attackData == null || attackData.PROJECTILES == null) {
            return;
        }

        // Look up PROJECTILE_DAMAGE from weapon CONFIG
        java.util.List<Integer> projectileDamages = getProjectileDamages(entity);

        for (int i = 0; i < attackData.PROJECTILES.size(); i++) {
            AttackFormattedData.ProjectileData projData = attackData.PROJECTILES.get(i);
            if (createOn.equals(projData.CREATE_ON)) {
                if (projectileDamages != null && i < projectileDamages.size()) {
                    spawnProjectileWithDamage(entity, projData, projectileDamages.get(i));
                } else {
                    spawnSingleProjectile(entity, projData);
                }
            }
        }
    }

    /**
     * Gets the PROJECTILE_DAMAGE list from the weapon CONFIG for the current attack.
     */
    private java.util.List<Integer> getProjectileDamages(Entity entity) {
        ComboStateComponent combo = ComboStateComponent.MAPPER.get(entity);
        if (combo == null || combo.weaponConfig == null || combo.currentAttackName == null) {
            return null;
        }
        WeaponFormattedData.AttackConfigData configData = combo.weaponConfig.get(combo.currentAttackName);
        if (configData == null) {
            return null;
        }
        return configData.PROJECTILE_DAMAGE;
    }

    /**
     * Spawns a single projectile from the given config.
     */
    public void spawnSingleProjectile(Entity entity, AttackFormattedData.ProjectileData projData) {
        Transform transform = Transform.MAPPER.get(entity);
        if (transform == null) {
            return;
        }

        float spawnX = transform.getCenteredX();
        float spawnY = transform.getCenteredY();
        float angle = LockOnSystem.getAngleToLockedEnemy(entity);

        if (projData.ORBITS != null && projData.ORBITS) {
            spawnOrbitingProjectile(entity, projData, spawnX, spawnY, angle);
        } else {
            spawnMovingProjectile(entity, projData, spawnX, spawnY, angle);
        }
    }

    /**
     * Spawns an orbiting projectile (like a sword swing effect).
     */
    private void spawnOrbitingProjectile(Entity attacker, AttackFormattedData.ProjectileData projData,
                                          float x, float y, float angle) {
        Entity projectile = PrefabInstantiator.instantiatePrefab("projectile",
                Application.getInstance().currentScreen, x, y);

        Transform transform = Transform.MAPPER.get(projectile);
        transform.xVel = 0;
        transform.yVel = 0;

        if (PrefabInstantiator.isMiningProjectile()) {
            projectile.add(new MiningProjectileComponent());
        } else {
            ProjectileComponent projComp = new ProjectileComponent();
            projComp.isPlayer = true;
            projectile.add(projComp);
        }

        ProjectileConfigComponent config = new ProjectileConfigComponent();
        config.createOn = projData.CREATE_ON;
        config.damage = projData.DAMAGE != null ? projData.DAMAGE : 10;
        config.lifetime = projData.LIFETIME != null ? projData.LIFETIME : 1.0f;
        config.movementPattern = projData.MOVEMENT_PATTERN != null ? projData.MOVEMENT_PATTERN : "STRAIGHT";
        config.speed = projData.SPEED != null ? projData.SPEED : 0f;
        config.orbits = true;
        config.fps = projData.FPS != null ? projData.FPS : 10f;
        config.isPlayer = true;
        config.hitboxStartDelay = projData.HITBOX_START_DELAY != null ? projData.HITBOX_START_DELAY : 0f;
        config.hitboxActiveDuration = projData.HITBOX_ACTIVE_DURATION != null ? projData.HITBOX_ACTIVE_DURATION : 0f;
        config.hitboxRadius = projData.HITBOX_RADIUS != null ? projData.HITBOX_RADIUS : 16f;
        projectile.add(config);

        RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(projectile);
        if (routineListComponent != null) {
            float orbitDistance = 15f;
            float linearSpeed = config.speed;

            OrbitPlayerAction action;
            if (linearSpeed > 0) {
                action = OrbitPlayerAction.forMovingProjectile(angle, orbitDistance, linearSpeed, config.lifetime);
            } else {
                action = OrbitPlayerAction.forProjectile(angle, orbitDistance, config.lifetime);
            }

            String animName = projData.ANIMATION_NAME != null ? projData.ANIMATION_NAME : "swordSlice";

            SuperAnimation anim = new SingleAnimation(animName, 32, 32, config.fps, Animation.PlayMode.NORMAL);
            routineListComponent.initRoutines(action, projectile, anim);

            // Update transform dimensions to match the animation frame size
            transform.FULL_BOUNDS_DIMENSIONS.x = 32;
            transform.FULL_BOUNDS_DIMENSIONS.y = 32;

            // Set origin to center for proper rotation
            transform.ORIGIN_X = 16;
            transform.ORIGIN_Y = 16;

            // Set initial rotation so the first frame renders correctly
            transform.rotation = angle * MathUtils.radDeg;

            // Set initial position to the orbit location so the first frame is correct
            float orbitX = x + orbitDistance * MathUtils.cos(angle);
            float orbitY = y + orbitDistance * MathUtils.sin(angle);
            transform.x = orbitX - transform.FULL_BOUNDS_DIMENSIONS.x / 2f;
            transform.y = orbitY - transform.FULL_BOUNDS_DIMENSIONS.y / 2f;

            // Set initial sprite texture so it's visible immediately
            SpriteComponent sprite = SpriteComponent.MAPPER.get(projectile);
            if (sprite != null && anim.getKeyFrame(0) != null) {
                sprite.textureRegion = anim.getKeyFrame(0);
            }
        }

        Application.getInstance().currentScreen.engine.addEntity(projectile);
    }

    /**
     * Spawns a moving projectile (travels in a direction).
     */
    private void spawnMovingProjectile(Entity attacker, AttackFormattedData.ProjectileData projData,
                                        float x, float y, float angle) {
        Entity projectile = PrefabInstantiator.instantiatePrefab("projectile",
                Application.getInstance().currentScreen, x, y);

        Transform transform = Transform.MAPPER.get(projectile);
        float speed = projData.SPEED != null ? projData.SPEED : 100f;
        transform.xVel = MathUtils.cos(angle) * speed;
        transform.yVel = MathUtils.sin(angle) * speed;

        if (PrefabInstantiator.isMiningProjectile()) {
            projectile.add(new MiningProjectileComponent());
        } else {
            ProjectileComponent projComp = new ProjectileComponent();
            projComp.isPlayer = true;
            projectile.add(projComp);
        }

        ProjectileConfigComponent config = new ProjectileConfigComponent();
        config.createOn = projData.CREATE_ON;
        config.damage = projData.DAMAGE != null ? projData.DAMAGE : 10;
        config.lifetime = projData.LIFETIME != null ? projData.LIFETIME : 1.0f;
        config.movementPattern = projData.MOVEMENT_PATTERN != null ? projData.MOVEMENT_PATTERN : "STRAIGHT";
        config.speed = speed;
        config.orbits = false;
        config.fps = projData.FPS != null ? projData.FPS : 10f;
        config.isPlayer = true;
        config.hitboxStartDelay = projData.HITBOX_START_DELAY != null ? projData.HITBOX_START_DELAY : 0f;
        config.hitboxActiveDuration = projData.HITBOX_ACTIVE_DURATION != null ? projData.HITBOX_ACTIVE_DURATION : 0f;
        config.hitboxRadius = projData.HITBOX_RADIUS != null ? projData.HITBOX_RADIUS : 16f;
        projectile.add(config);

        RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(projectile);
        if (routineListComponent != null) {
            ChargeAction action = ChargeAction.simpleMoveTowardsAngle(config.lifetime);
            String animName = projData.ANIMATION_NAME != null ? projData.ANIMATION_NAME : "swordSlice";
            SuperAnimation anim = new SingleFrame(animName);
            routineListComponent.initRoutines(action, projectile, anim);

            // Update transform dimensions to match the animation frame size
            transform.FULL_BOUNDS_DIMENSIONS.x = 32;
            transform.FULL_BOUNDS_DIMENSIONS.y = 32;

            // Set origin to center for proper rotation
            transform.ORIGIN_X = 16;
            transform.ORIGIN_Y = 16;

            // Set initial rotation so the first frame renders correctly
            transform.rotation = angle * MathUtils.radDeg;

            // Set initial sprite texture so it's visible immediately
            SpriteComponent sprite = SpriteComponent.MAPPER.get(projectile);
            if (sprite != null && anim.getKeyFrame(0) != null) {
                sprite.textureRegion = anim.getKeyFrame(0);
            }
        }

        Application.getInstance().currentScreen.engine.addEntity(projectile);
    }

    /**
     * Spawns a projectile with custom damage (for weapon CONFIG overrides).
     */
    public void spawnProjectileWithDamage(Entity entity, AttackFormattedData.ProjectileData projData, int overrideDamage) {
        AttackFormattedData.ProjectileData modifiedData = new AttackFormattedData.ProjectileData();
        modifiedData.CREATE_ON = projData.CREATE_ON;
        modifiedData.CREATE_ON_HOLD_INTERVAL = projData.CREATE_ON_HOLD_INTERVAL;
        modifiedData.ANIMATION_NAME = projData.ANIMATION_NAME;
        modifiedData.FPS = projData.FPS;
        modifiedData.ORBITS = projData.ORBITS;
        modifiedData.SPEED = projData.SPEED;
        modifiedData.DAMAGE = overrideDamage;
        modifiedData.LIFETIME = projData.LIFETIME;
        modifiedData.MOVEMENT_PATTERN = projData.MOVEMENT_PATTERN;
        modifiedData.HITBOX_START_DELAY = projData.HITBOX_START_DELAY;
        modifiedData.HITBOX_ACTIVE_DURATION = projData.HITBOX_ACTIVE_DURATION;
        modifiedData.HITBOX_RADIUS = projData.HITBOX_RADIUS;

        spawnSingleProjectile(entity, modifiedData);
    }
}
