package com.mikm.entities.actions;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.mikm._components.ComboStateComponent;
import com.mikm._components.SpriteComponent;
import com.mikm._components.Transform;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm.entities.animation.SingleAnimation;
import com.mikm.entities.prefabLoader.attack.AttackFormattedData;
import com.mikm._systems.LockOnSystem;
import com.mikm.input.GameInput;
import com.mikm.rendering.screens.Application;
import com.badlogic.gdx.Gdx;
import com.mikm.utils.debug.DebugRenderer;

import java.util.HashMap;

/**
 * Action for entities that orbit around the player.
 * Supports two modes:
 * 1. Weapon mode (default): Centers on player, rotates toward mouse, switches animations on attack
 * 2. Projectile mode: Fixed angle at spawn, optional linear speed, has lifetime
 */
public class OrbitPlayerAction extends Action {

    public static final ComponentMapper<OrbitActionComponent> MAPPER = ComponentMapper.getFor(OrbitActionComponent.class);

    // Configuration for projectile mode
    private boolean isProjectile = false;
    private float fixedAngle = 0;
    private float linearSpeed = 0;
    private float lifetime = 0;
    private float orbitDistance = 15;

    // Orbit configuration for weapons
    private String orbitType = "SWAP";
    private String animationPrefix = null;

    /**
     * Default constructor for weapon orbit (tracks mouse continuously).
     */
    public OrbitPlayerAction() {
        this.isProjectile = false;
    }

    /**
     * Constructor for weapon orbit with orbit configuration.
     */
    public OrbitPlayerAction(String orbitType, String animationPrefix) {
        this.isProjectile = false;
        this.orbitType = orbitType != null ? orbitType : "SWAP";
        this.animationPrefix = animationPrefix;
    }

    /**
     * Constructor for projectile orbit with fixed angle.
     *
     * @param fixedAngle The angle to orbit at (in radians)
     * @param orbitDistance Distance from player center
     * @param linearSpeed Speed to move outward (0 = stay at fixed distance)
     * @param lifetime How long before the projectile is removed (0 = no auto-remove)
     */
    public OrbitPlayerAction(float fixedAngle, float orbitDistance, float linearSpeed, float lifetime) {
        this.isProjectile = true;
        this.fixedAngle = fixedAngle;
        this.orbitDistance = orbitDistance;
        this.linearSpeed = linearSpeed;
        this.lifetime = lifetime;
    }

    /**
     * Factory method for a simple orbiting projectile (like sword slice).
     */
    public static OrbitPlayerAction forProjectile(float angle, float orbitDistance, float lifetime) {
        return new OrbitPlayerAction(angle, orbitDistance, 0, lifetime);
    }

    /**
     * Factory method for an orbiting projectile that also moves outward.
     */
    public static OrbitPlayerAction forMovingProjectile(float angle, float orbitDistance, float linearSpeed, float lifetime) {
        return new OrbitPlayerAction(angle, orbitDistance, linearSpeed, lifetime);
    }

    public static class OrbitActionComponent implements Component {
        // Weapon mode fields
        float angleToMouse;
        float lockedAttackAngle;

        // Orbit configuration for weapons
        String orbitType = "SWAP";
        String animationPrefix;
        boolean wasAttacking = false;
        int lastAttackSequenceId = -1;
        String currentWeaponAnimationName;
        // Cache trimmed TextureRegion[] by resolved animation name (atlas lookup + trim is expensive)
        HashMap<String, TextureRegion[]> frameCache = new HashMap<>();

        // Shared/Projectile mode fields
        float orbitDistance = 15;
        boolean isProjectile = false;
        float fixedAngle = 0;
        float linearSpeed = 0;
        float lifetime = 0;
        float timer = 0;
        float currentDistance = 0;
        boolean shouldRemove = false;
    }

    @Override
    public Component createActionComponent() {
        OrbitActionComponent comp = new OrbitActionComponent();
        comp.isProjectile = this.isProjectile;
        comp.fixedAngle = this.fixedAngle;
        comp.orbitDistance = this.orbitDistance;
        comp.currentDistance = this.orbitDistance;
        comp.linearSpeed = this.linearSpeed;
        comp.lifetime = this.lifetime;
        comp.timer = 0;
        comp.shouldRemove = false;
        comp.orbitType = this.orbitType;
        comp.animationPrefix = this.animationPrefix;
        comp.wasAttacking = false;
        return comp;
    }

    @Override
    public void update(Entity entity) {
        super.update(entity);

        OrbitActionComponent data = MAPPER.get(entity);
        if (data == null) return;

        if (data.isProjectile) {
            updateProjectile(entity, data);
        } else {
            updateWeapon(entity, data);
        }
    }

    /**
     * Update for weapon mode - centers on player, rotates toward mouse, switches animations.
     */
    private void updateWeapon(Entity entity, OrbitActionComponent data) {
        Entity player = Application.getInstance().getPlayer();
        ComboStateComponent comboState = player != null ? ComboStateComponent.MAPPER.get(player) : null;
        boolean isAttacking = comboState != null && comboState.isAttacking;

        // Handle animation switching
        boolean newAttack = isAttacking && comboState.attackSequenceId != data.lastAttackSequenceId;
        if (newAttack) {
            // Attack just started (or new attack in combo) - lock rotation and switch animation
            data.lastAttackSequenceId = comboState.attackSequenceId;
            data.lockedAttackAngle = LockOnSystem.getAngleToLockedEnemy(player);
            AttackFormattedData attackData = comboState.currentAttackData;
            String animType = attackData.WEAPON_ANIMATION;
            float fps = attackData.WEAPON_ANIMATION_FPS;
            boolean loop = attackData.WEAPON_ANIMATION_LOOP;
            Animation.PlayMode playMode = loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL;
            switchWeaponAnimation(entity, data, animType, fps, playMode);
        } else if (!isAttacking && data.wasAttacking) {
            // Attack just ended - switch back to NonAttack (always loops)
            switchWeaponAnimation(entity, data, "NonAttack", 10f, Animation.PlayMode.LOOP);
        }
        data.wasAttacking = isAttacking;

        // Position: center weapon on player (offset for 192x192 centering)
        Transform transform = Transform.MAPPER.get(entity);
        Transform playerTransform = Application.getInstance().getPlayerTransform();
        if (transform == null || playerTransform == null) return;

        transform.x = playerTransform.getCenteredX() - transform.FULL_BOUNDS_DIMENSIONS.x / 2f;
        transform.y = playerTransform.getCenteredY() - transform.FULL_BOUNDS_DIMENSIONS.y / 2f;

        // Rotation: during attack, lock to the direction at attack start; otherwise track enemy/mouse
        if (isAttacking) {
            data.angleToMouse = data.lockedAttackAngle;
        } else {
            data.angleToMouse = LockOnSystem.getAngleToLockedEnemy(player);
        }
        transform.rotation = data.angleToMouse * MathUtils.radDeg;

        // Z-order based on facing angle
        setZOrder(entity, data.angleToMouse);
    }

    /**
     * Switches the weapon's animation, using prefix fallback and frame caching.
     * Frames are cached; Animation objects are created fresh with the given fps/playMode.
     */
    private void switchWeaponAnimation(Entity entity, OrbitActionComponent data, String animType, float fps, Animation.PlayMode playMode) {
        // Resolve animation name with prefix fallback
        String animName = resolveAnimationName(data, animType);

        // Get or load trimmed frames (cached by resolved name)
        TextureRegion[] frames = data.frameCache.get(animName);
        if (frames == null) {
            frames = SingleAnimation.loadFrames(animName, 192, 192);
            data.frameCache.put(animName, frames);
        }

        data.currentWeaponAnimationName = animName;

        // Create animation with the attack-specific fps and play mode
        SingleAnimation anim = new SingleAnimation(frames, fps, playMode);

        // Set the animation on the entity's routine
        RoutineListComponent routineList = RoutineListComponent.MAPPER.get(entity);
        if (routineList != null) {
            routineList.setCurrentActionAnimation(anim);
        }

        // Reset animation time
        SpriteComponent spriteComponent = SpriteComponent.MAPPER.get(entity);
        if (spriteComponent != null) {
            spriteComponent.animationTime = 0;
        }
    }

    /**
     * Resolves animation name with prefix fallback.
     * Tries "{prefix}{animType}" first, falls back to "{animType}".
     */
    private String resolveAnimationName(OrbitActionComponent data, String animType) {
        if (data.animationPrefix != null) {
            String prefixedName = data.animationPrefix + animType;
            // Check if the prefixed version exists in the frame cache or atlas
            if (data.frameCache.containsKey(prefixedName)) {
                return prefixedName;
            }
            try {
                com.mikm.utils.Assets.getInstance().getSplitTextureRegion(prefixedName, 192, 192);
                return prefixedName;
            } catch (Exception e) {
                // Prefix variant not found, fall back to generic
            }
        }
        return animType;
    }

    /**
     * Update for projectile mode - fixed angle, optional linear movement, lifetime.
     */
    private void updateProjectile(Entity entity, OrbitActionComponent data) {
        Transform transform = Transform.MAPPER.get(entity);
        Transform playerTransform = Application.getInstance().getPlayerTransform();

        if (transform == null || playerTransform == null) return;

        float dt = Gdx.graphics.getDeltaTime();

        // Update timer and check lifetime
        data.timer += dt;
        if (data.lifetime > 0 && data.timer >= data.lifetime) {
            data.shouldRemove = true;
            Application.getInstance().currentScreen.removeEntity(entity);
            return;
        }

        // Update distance if moving linearly
        if (data.linearSpeed != 0) {
            data.currentDistance += data.linearSpeed * dt;
        }

        // Calculate position at fixed angle
        float x = playerTransform.getCenteredX() + data.currentDistance * MathUtils.cos(data.fixedAngle);
        float y = playerTransform.getCenteredY() + data.currentDistance * MathUtils.sin(data.fixedAngle);

        // Center the sprite
        transform.x = x - transform.FULL_BOUNDS_DIMENSIONS.x / 2f;
        transform.y = y - transform.FULL_BOUNDS_DIMENSIONS.y / 2f;

        // Rotate to face the attack direction
        transform.rotation = data.fixedAngle * MathUtils.radDeg;

        // Set z-order based on angle
        setZOrder(entity, data.fixedAngle);
    }

    private void setZOrder(Entity entity, float angle) {
        Transform transform = Transform.MAPPER.get(entity);
        // Normalize angle to 0-2PI range
        float normalizedAngle = angle;
        while (normalizedAngle < 0) normalizedAngle += MathUtils.PI2;
        while (normalizedAngle >= MathUtils.PI2) normalizedAngle -= MathUtils.PI2;

        // Behind player when attacking upward (angle between PI/2 and 3PI/2)
        if (normalizedAngle > MathUtils.HALF_PI && normalizedAngle < MathUtils.PI + MathUtils.HALF_PI) {
            transform.Z_ORDER = 1; // In front
        } else {
            transform.Z_ORDER = -1; // Behind
        }
    }

    /**
     * Checks if the projectile should be removed (lifetime expired).
     */
    public boolean shouldRemove(Entity entity) {
        OrbitActionComponent data = MAPPER.get(entity);
        return data != null && data.shouldRemove;
    }
}
