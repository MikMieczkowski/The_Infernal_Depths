package com.mikm.entities.actions;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mikm._components.*;
import com.mikm.entities.inanimateEntities.particles.ParticleTypes;
import com.mikm.entities.prefabLoader.PrefabInstantiator;
import com.mikm.rendering.cave.RockType;
import com.mikm.utils.ExtraMathUtils;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm.entities.DamageInformation;
import com.mikm.rendering.screens.Application;

@RuntimeDataComponent
public class DamagedAction extends Action {

    public static final float TOTAL_KNOCKBACK_TIME = .25f;
    private final float JUMP_HEIGHT = 8f;
    final float DEATH_KNOCKBACK_MULTIPLIER = 3f;

    public static final ComponentMapper<DamagedActionComponent> MAPPER = ComponentMapper.getFor(DamagedActionComponent.class);
    public class DamagedActionComponent implements Component {
        public DamageInformation damageInformation;
        public boolean active;
    }

    public DamagedAction(){}

    @Override
    public Component createActionComponent() {
        return new DamagedActionComponent();
    }

    @Override
    public void enter(Entity entity) {
        throw new RuntimeException();
    }

    public void enter(Entity entity, DamageInformation damageInformation) {
        super.enter(entity);

        DamagedAction.DamagedActionComponent damagedActionComponent = DamagedAction.MAPPER.get(entity);
        damagedActionComponent.active = true;
        damagedActionComponent.damageInformation = damageInformation;
    }

    @Override
    public void update(Entity entity) {
        super.update(entity);
        DamagedActionComponent data = MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);
        CombatComponent combatComponent = CombatComponent.MAPPER.get(entity);
        RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(entity);

        // Compute hitstun duration: if hitstunFrames > 0, use it; otherwise fall back to TOTAL_KNOCKBACK_TIME
        float hitstunSeconds = data.damageInformation.hitstunFrames > 0
                ? data.damageInformation.hitstunFrames / 60.0f
                : TOTAL_KNOCKBACK_TIME;

        float knockbackMultiplier = combatComponent.KNOCKBACK_MULTIPLIER;
        float knockbackTime = TOTAL_KNOCKBACK_TIME * (combatComponent.dead ? 1f : .75f);
        float elapsed = routineListComponent.timeElapsedInCurrentAction;

        // Apply knockback while within knockback time
        if (elapsed < knockbackTime) {
            Vector2 knockbackForce = new Vector2(
                    MathUtils.cos(data.damageInformation.knockbackAngle) * data.damageInformation.knockbackForceMagnitude * knockbackMultiplier,
                    MathUtils.sin(data.damageInformation.knockbackAngle) * data.damageInformation.knockbackForceMagnitude * knockbackMultiplier);

            Vector2 sinLerpedKnockbackForce = ExtraMathUtils.sinLerpVector2(elapsed, TOTAL_KNOCKBACK_TIME, .1f, 1f, knockbackForce);
            float jumpOffset = ExtraMathUtils.sinLerp(elapsed, knockbackTime, .1f, 1f, JUMP_HEIGHT) * knockbackMultiplier;
            if (combatComponent.dead) {
                sinLerpedKnockbackForce = sinLerpedKnockbackForce.scl(DEATH_KNOCKBACK_MULTIPLIER);
                jumpOffset *= 1.5f;
            }
            transform.height = jumpOffset;
            transform.xVel = sinLerpedKnockbackForce.x;
            transform.yVel = sinLerpedKnockbackForce.y;
        } else {
            // Knockback finished but still in hitstun — hold position
            transform.xVel = 0;
            transform.yVel = 0;
            transform.height = 0;
        }

        // Exit when hitstun expires (or knockback for dead entities)
        float exitTime = combatComponent.dead ? knockbackTime : hitstunSeconds;
        if (elapsed >= exitTime && data.active) {
            boolean knockbackStillActive = elapsed < knockbackTime;
            onExit(entity);
            data.active = false;
            if (!combatComponent.dead) {
                if (knockbackStillActive) {
                    // Hitstun ended before knockback — carry residual velocity into idle
                    combatComponent.inResidualKnockback = true;
                } else {
                    transform.xVel = 0;
                    transform.yVel = 0;
                }
            }
        }
    }

    @Override
    public void onExit(Entity entity) {
        DamagedActionComponent data = MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);
        CombatComponent combatComponent = CombatComponent.MAPPER.get(entity);

        if (combatComponent.dead) {
            PrefabInstantiator.addParticles(transform.x, transform.y, data.damageInformation.knockbackAngle, ParticleTypes.getKnockbackDustParameters());
            if (transform.ENTITY_NAME.equals("slime")) {
                com.mikm.rendering.sound.SoundEffects.play("slimeDeath.ogg");
            }
            if (transform.ENTITY_NAME.equals("player")) {
                //onDeath
                //Bad code - maybe make an "active" attribute on transform? Although that has problems too
                SpriteComponent.MAPPER.get(entity).visible = false;
                RoutineListComponent.MAPPER.get(entity).active = false;
                WorldColliderComponent.MAPPER.get(entity).active = false;
                CombatComponent.MAPPER.get(entity).setInvincibility(false);
                if (RockType.playerHasAnyTempOre()) {
                    PrefabInstantiator.addGrave(Application.getInstance().currentScreen);
                }
            } else {
                Application.getInstance().currentScreen.removeEntity(entity);
            }

        }
        data.active = false;

        RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(entity);
        routineListComponent.enterPostHitRoutine(entity);
        super.onExit(entity);
    }
}
