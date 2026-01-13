package com.mikm.entities.actions;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mikm._components.*;
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
        CombatComponent combatComponent = CombatComponent.MAPPER.get(entity);
        if (combatComponent.isInvincible()) {
            throw new RuntimeException("Should not call damagedAction.enter() if invincible");
        }
    }

    @Override
    public void update(Entity entity) {
        super.update(entity);
        DamagedActionComponent data = MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);
        CombatComponent combatComponent = CombatComponent.MAPPER.get(entity);
        
        Vector2 knockbackForce = new Vector2(MathUtils.cos(data.damageInformation.knockbackAngle) * data.damageInformation.knockbackForceMagnitude,
                MathUtils.sin(data.damageInformation.knockbackAngle) * data.damageInformation.knockbackForceMagnitude);

        RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(entity);
        Vector2 sinLerpedKnockbackForce = ExtraMathUtils.sinLerpVector2(routineListComponent.timeElapsedInCurrentAction,TOTAL_KNOCKBACK_TIME,.1f, 1f, knockbackForce);
        float jumpOffset = ExtraMathUtils.sinLerp(routineListComponent.timeElapsedInCurrentAction, TOTAL_KNOCKBACK_TIME * (combatComponent.dead ? 1 : .75f), .1f, 1f, JUMP_HEIGHT);
        if (combatComponent.dead) {
            sinLerpedKnockbackForce = sinLerpedKnockbackForce.scl(DEATH_KNOCKBACK_MULTIPLIER);
            jumpOffset *= 1.5f;
        }
        transform.height = jumpOffset;
        transform.xVel = sinLerpedKnockbackForce.x;
        transform.yVel = sinLerpedKnockbackForce.y;

        float totalTime = TOTAL_KNOCKBACK_TIME * (combatComponent.dead ? 1f : .75f);
        if (routineListComponent.timeElapsedInCurrentAction >= totalTime && data.active) {
            // finish damaged state and trigger death if applicable
            onExit(entity);
            data.active = false;
            if (!combatComponent.dead) {
                transform.xVel = 0;
                transform.yVel = 0;
            }
        }
    }

    @Override
    public void onExit(Entity entity) {
        DamagedActionComponent data = MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);
        CombatComponent combatComponent = CombatComponent.MAPPER.get(entity);

        if (combatComponent.dead) {
            //TODO particle usage
//            new ParticleEffect(ParticleTypes.getKnockbackDustParameters(), data.damageInformation.knockbackAngle, transform.x, transform.y);
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
                    //TODO particles
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
