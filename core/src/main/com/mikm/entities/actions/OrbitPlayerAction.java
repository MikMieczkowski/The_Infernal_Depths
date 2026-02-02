package com.mikm.entities.actions;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mikm._components.Transform;
import com.mikm.input.GameInput;
import com.mikm.rendering.screens.Application;
import com.badlogic.gdx.Gdx;

/**
 * Action for entities that orbit around the player.
 * Supports two modes:
 * 1. Weapon mode (default): Continuously tracks mouse angle, used for held weapons
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

    /**
     * Default constructor for weapon orbit (tracks mouse continuously).
     */
    public OrbitPlayerAction() {
        this.isProjectile = false;
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
        float weaponRotation;
        float angleOffset;
        boolean mouseIsLeftOfPlayer = false;
        boolean shouldSwingRight = true;
        float angleToMouse;
        float weaponAngle;

        // Shared/Projectile mode fields
        float orbitDistance = 15;
        boolean isProjectile = false;
        float fixedAngle = 0;
        float linearSpeed = 0;
        float lifetime = 0;
        float timer = 0;
        float currentDistance = 0; // For linear movement
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
     * Update for weapon mode - tracks mouse angle.
     */
    private void updateWeapon(Entity entity, OrbitActionComponent data) {
        Transform transform = Transform.MAPPER.get(entity);
        Vector2 position = getPlayerPositionOrbitedAroundMouse(entity, data);
        transform.x = position.x;
        transform.y = position.y;

        setZOrder(entity, data.weaponAngle);
        transform.rotation = data.weaponRotation * MathUtils.radDeg;
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

    private Vector2 getPlayerPositionOrbitedAroundMouse(Entity entity, OrbitActionComponent data) {
        float x, y;

        data.angleToMouse = GameInput.getAttackingAngle();
        data.mouseIsLeftOfPlayer = -MathUtils.HALF_PI < data.angleToMouse && data.angleToMouse < MathUtils.HALF_PI;

        data.weaponAngle = data.angleToMouse - MathUtils.HALF_PI;
        data.weaponRotation = data.angleToMouse + MathUtils.PI/2;
        final float weaponArcRotationProportion = (3+MathUtils.PI/2)/3;
        if (data.mouseIsLeftOfPlayer) {
            data.weaponAngle += MathUtils.PI;
            data.weaponRotation -= MathUtils.PI/2;
        }
        if (data.mouseIsLeftOfPlayer) {
            data.weaponRotation -= data.angleOffset*weaponArcRotationProportion;
            data.weaponAngle -= data.angleOffset;
        } else {
            data.weaponRotation += data.angleOffset*weaponArcRotationProportion;
            data.weaponAngle += data.angleOffset;
        }

        Transform playerTransform = Application.getInstance().getPlayerTransform();

        x = playerTransform.getCenteredX() + data.orbitDistance * MathUtils.cos(data.weaponAngle) - playerTransform.getFullBounds().width/2;
        y = playerTransform.getCenteredY() + data.orbitDistance * MathUtils.sin(data.weaponAngle) - playerTransform.getFullBounds().height/2 - 6;
        return new Vector2(x, y);
    }

    /**
     * Checks if the projectile should be removed (lifetime expired).
     */
    public boolean shouldRemove(Entity entity) {
        OrbitActionComponent data = MAPPER.get(entity);
        return data != null && data.shouldRemove;
    }
}
