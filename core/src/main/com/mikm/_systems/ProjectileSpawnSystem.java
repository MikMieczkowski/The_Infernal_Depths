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

        // Get index 0's damage for INHERITS_DAMAGE_FROM_ZERO
        Integer indexZeroDamage = attackData.PROJECTILES.get(0).DAMAGE;

        for (int i = 0; i < attackData.PROJECTILES.size(); i++) {
            AttackFormattedData.ProjectileData projData = attackData.PROJECTILES.get(i);
            if (createOn.equals(projData.CREATE_ON)) {
                // Resolve effective damage: CONFIG override > INHERITS_DAMAGE_FROM_ZERO > own DAMAGE
                int effectiveDamage;
                if (projectileDamages != null && i < projectileDamages.size()) {
                    effectiveDamage = projectileDamages.get(i);
                } else if (i > 0 && projData.INHERITS_DAMAGE_FROM_ZERO != null && projData.INHERITS_DAMAGE_FROM_ZERO && indexZeroDamage != null) {
                    effectiveDamage = indexZeroDamage;
                } else {
                    effectiveDamage = projData.DAMAGE;
                }
                spawnProjectileWithDamage(entity, projData, effectiveDamage);
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
        config.damage = projData.DAMAGE;
        // Orbiting projectiles with no lifetime default to the attack duration
        float lifetimeFrames = projData.LIFETIME_FRAMES != null ? projData.LIFETIME_FRAMES : 0;
        if (lifetimeFrames <= 0) {
            ComboStateComponent combo = ComboStateComponent.MAPPER.get(attacker);
            if (combo != null && combo.currentAttackData != null && combo.currentAttackData.ATTACK_FRAMES != null) {
                lifetimeFrames = combo.currentAttackData.ATTACK_FRAMES;
            }
        }
        config.lifetime = lifetimeFrames / 60.0f;
        config.movementPattern = projData.MOVEMENT_PATTERN;
        config.speed = projData.SPEED;
        config.orbits = true;
        config.fps = projData.FPS;
        config.isPlayer = true;
        config.startupTime = projData.STARTUP_FRAMES / 60.0f;
        config.activeTime = projData.ACTIVE_FRAMES / 60.0f;
        float sliceWidthMultiplier = projData.WIDTH_MULTIPLIER;
        config.hitboxRadius = projData.HITBOX_RADIUS * sliceWidthMultiplier;
        projectile.add(config);

        float spawnDistance = projData.SPAWN_DISTANCE;

        RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(projectile);
        if (routineListComponent != null) {
            float linearSpeed = config.speed;

            OrbitPlayerAction action;
            if (linearSpeed > 0) {
                action = OrbitPlayerAction.forMovingProjectile(angle, spawnDistance, linearSpeed, config.lifetime);
            } else {
                action = OrbitPlayerAction.forProjectile(angle, spawnDistance, config.lifetime);
            }

            String animName = projData.ANIMATION_NAME;
            SuperAnimation anim;
            if (animName != null) {
                anim = new SingleAnimation(animName, 32, 32, config.fps, Animation.PlayMode.NORMAL);
            } else {
                anim = new SingleFrame("swordSlice"); // placeholder, sprite hidden below
            }
            routineListComponent.initRoutines(action, projectile, anim);

            // Update transform dimensions to match the animation frame size
            transform.FULL_BOUNDS_DIMENSIONS.x = 32;
            transform.FULL_BOUNDS_DIMENSIONS.y = 32;

            // Set origin to center for proper rotation
            transform.ORIGIN_X = 16;
            transform.ORIGIN_Y = 16;

            // Apply slice width multiplier to visual scale
            transform.xScale = sliceWidthMultiplier;

            // Set initial rotation so the first frame renders correctly
            transform.rotation = angle * MathUtils.radDeg;

            // Set initial position using SPAWN_DISTANCE toward locked enemy/mouse
            float orbitX = x + spawnDistance * MathUtils.cos(angle);
            float orbitY = y + spawnDistance * MathUtils.sin(angle);
            transform.x = orbitX - transform.FULL_BOUNDS_DIMENSIONS.x / 2f;
            transform.y = orbitY - transform.FULL_BOUNDS_DIMENSIONS.y / 2f;

            SpriteComponent sprite = SpriteComponent.MAPPER.get(projectile);
            if (animName == null) {
                // No animation — hitbox-only invisible projectile
                if (sprite != null) sprite.visible = false;
            } else if (sprite != null && anim.getKeyFrame(0) != null) {
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
        // Offset spawn position using SPAWN_DISTANCE toward locked enemy/mouse
        float spawnDistance = projData.SPAWN_DISTANCE;
        float spawnX = x + spawnDistance * MathUtils.cos(angle);
        float spawnY = y + spawnDistance * MathUtils.sin(angle);

        Entity projectile = PrefabInstantiator.instantiatePrefab("projectile",
                Application.getInstance().currentScreen, spawnX, spawnY);

        Transform transform = Transform.MAPPER.get(projectile);
        float speed = projData.SPEED;
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
        config.damage = projData.DAMAGE;
        config.lifetime = projData.LIFETIME_FRAMES / 60.0f;
        config.movementPattern = projData.MOVEMENT_PATTERN;
        config.speed = speed;
        config.orbits = false;
        config.fps = projData.FPS;
        config.isPlayer = true;
        config.startupTime = projData.STARTUP_FRAMES / 60.0f;
        config.activeTime = projData.ACTIVE_FRAMES / 60.0f;
        float movingSliceWidth = projData.WIDTH_MULTIPLIER;
        config.hitboxRadius = projData.HITBOX_RADIUS * movingSliceWidth;
        projectile.add(config);

        RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(projectile);
        if (routineListComponent != null) {
            ChargeAction action = ChargeAction.simpleMoveTowardsAngle(config.speed);
            String animName = projData.ANIMATION_NAME;
            SuperAnimation anim = new SingleFrame(animName != null ? animName : "swordSlice");
            routineListComponent.initRoutines(action, projectile, anim);

            // Update transform dimensions to match the animation frame size
            transform.FULL_BOUNDS_DIMENSIONS.x = 32;
            transform.FULL_BOUNDS_DIMENSIONS.y = 32;

            // Set origin to center for proper rotation
            transform.ORIGIN_X = 16;
            transform.ORIGIN_Y = 16;

            // Apply slice width multiplier to visual scale
            transform.xScale = movingSliceWidth;

            // Center the entity on the spawn point
            transform.x = spawnX - transform.FULL_BOUNDS_DIMENSIONS.x / 2f;
            transform.y = spawnY - transform.FULL_BOUNDS_DIMENSIONS.y / 2f;

            // Set initial rotation so the first frame renders correctly
            transform.rotation = angle * MathUtils.radDeg;

            SpriteComponent sprite = SpriteComponent.MAPPER.get(projectile);
            if (animName == null) {
                if (sprite != null) sprite.visible = false;
            } else if (sprite != null && anim.getKeyFrame(0) != null) {
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
        modifiedData.LIFETIME_FRAMES = projData.LIFETIME_FRAMES;
        modifiedData.MOVEMENT_PATTERN = projData.MOVEMENT_PATTERN;
        modifiedData.STARTUP_FRAMES = projData.STARTUP_FRAMES;
        modifiedData.ACTIVE_FRAMES = projData.ACTIVE_FRAMES;
        modifiedData.HITBOX_RADIUS = projData.HITBOX_RADIUS;
        modifiedData.WIDTH_MULTIPLIER = projData.WIDTH_MULTIPLIER;
        modifiedData.SPAWN_POSITION = projData.SPAWN_POSITION;
        modifiedData.SPAWN_DISTANCE = projData.SPAWN_DISTANCE;
        modifiedData.INHERITS_DAMAGE_FROM_ZERO = projData.INHERITS_DAMAGE_FROM_ZERO;

        spawnSingleProjectile(entity, modifiedData);
    }
}
