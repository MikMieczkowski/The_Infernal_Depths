package com.mikm.entities.actions;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.mikm._components.AttackInputComponent;
import com.mikm._components.ComboStateComponent;
import com.mikm._components.LockOnComponent;
import com.mikm._components.Transform;
import com.mikm._systems.ComboSystem;
import com.mikm._systems.LockOnSystem;
import com.mikm._systems.ProjectileSpawnSystem;
import com.mikm.entities.prefabLoader.Blackboard;
import com.mikm.entities.prefabLoader.weapon.AttackDuration;
import com.mikm.rendering.screens.Application;

/**
 * Action for player charging/attacking.
 * Tracks charge time to determine attack type (light/heavy).
 * Spawns projectiles through ProjectileSpawnSystem on exit.
 */
public class PlayerChargingAction extends AcceleratedMoveAction {

    private static final ComponentMapper<PlayerChargingActionComponent> MAPPER =
            ComponentMapper.getFor(PlayerChargingActionComponent.class);

    static class PlayerChargingActionComponent implements Component {
        float chargeTime;
    }

    @Override
    public Component createActionComponent() {
        return new PlayerChargingActionComponent();
    }

    @Override
    public void enter(Entity entity) {
        super.enter(entity);
        PlayerChargingActionComponent data = MAPPER.get(entity);
        data.chargeTime = 0;
        Blackboard.getInstance().bind("idleTimer", entity, 0);

        // Cache distance for combo evaluation
        LockOnComponent lockOn = LockOnComponent.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);
        if (lockOn != null && transform != null) {
            LockOnSystem lockOnSystem = Application.getInstance().currentScreen.engine.getSystem(LockOnSystem.class);
            if (lockOnSystem != null) {
                lockOnSystem.cacheDistanceForCombo(lockOn, transform);
            }
        }
    }

    @Override
    public void update(Entity entity) {
        super.update(entity);
        PlayerChargingActionComponent data = MAPPER.get(entity);
        data.chargeTime += Gdx.graphics.getDeltaTime();
    }

    @Override
    public void onExit(Entity entity) {
        super.onExit(entity);

        PlayerChargingActionComponent data = MAPPER.get(entity);
        ComboStateComponent combo = ComboStateComponent.MAPPER.get(entity);
        LockOnComponent lockOn = LockOnComponent.MAPPER.get(entity);

        AttackDuration duration = getAttackDuration(data.chargeTime);
        float distance = (lockOn != null) ? lockOn.cachedDistance : 0f;

        // Execute attack through combo system
        ComboSystem comboSystem = Application.getInstance().currentScreen.engine.getSystem(ComboSystem.class);
        if (comboSystem != null && combo != null) {
            comboSystem.executeAttack(entity, duration, distance);
        }

        // Spawn projectiles for this attack
        if (combo != null && combo.currentAttackData != null) {
            ProjectileSpawnSystem projectileSystem =
                    Application.getInstance().currentScreen.engine.getSystem(ProjectileSpawnSystem.class);
            if (projectileSystem != null) {
                projectileSystem.spawnProjectiles(entity, combo.currentAttackData, "PRESS");
            }
        }
    }

    private AttackDuration getAttackDuration(float chargeTime) {
        if (chargeTime >= AttackInputComponent.HEAVY_THRESHOLD) {
            return AttackDuration.HEAVY;
        } else {
            return AttackDuration.LIGHT;
        }
    }
}
