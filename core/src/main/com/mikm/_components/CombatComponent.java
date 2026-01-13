package com.mikm._components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm.entities.DamageInformation;
import com.mikm.entities.actions.DamagedAction;
import com.mikm.entities.animation.SuperAnimation;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.sound.SoundEffects;

public class CombatComponent implements Component {
    public static final ComponentMapper<CombatComponent> MAPPER = ComponentMapper.getFor(CombatComponent.class);
    public static final float MAX_ENEMY_INVINCIBILITY_TIME = .3f;
    public static final float MAX_PLAYER_INVINCIBILITY_TIME = 1f;
    @Copyable public float maxInvincibilityTime;
    @Copyable public int MAX_HP;
    @Copyable public int DAMAGE;
    @Copyable public int KNOCKBACK;
    @CopyReference public SuperAnimation HURT_ANIMATION; //shallow copy?
    @Copyable public String HURT_SOUND_EFFECT;
    //Permanent invincibility
    @Copyable private boolean isAttackable;

    public float invincibilityTimer;
    //Temporary invincibility
    public boolean inInvincibilityFrames = false;

    public int hp;
    public boolean dead = false;

    public CombatComponent() {

    }

    public CombatComponent(boolean isPlayer) {
        if (isPlayer) {
            maxInvincibilityTime = MAX_PLAYER_INVINCIBILITY_TIME;
        } else {
            maxInvincibilityTime = MAX_ENEMY_INVINCIBILITY_TIME;
        }
    }

    public CombatComponent(int maxHp, int damage, int knockback, SuperAnimation hurtAnimation, String hurtSoundEffect, boolean isAttackable, boolean isPlayer) {
        MAX_HP = maxHp;
        hp = MAX_HP;
        DAMAGE = damage;
        KNOCKBACK = knockback;
        HURT_ANIMATION = hurtAnimation;
        HURT_SOUND_EFFECT = hurtSoundEffect;
        this.isAttackable = isAttackable;

        if (isPlayer) {
            maxInvincibilityTime = MAX_PLAYER_INVINCIBILITY_TIME;
        } else {
            maxInvincibilityTime = MAX_ENEMY_INVINCIBILITY_TIME;
        }
    }

    public void startInvincibilityFrames() {
        inInvincibilityFrames = true;
        invincibilityTimer = 0;
    }

    private final String FAILED_HIT_SOUND_EFFECT = "bowImpact.ogg";
    public void takeDamage(DamageInformation damageInformation, Entity entity) {
        CombatComponent combatComponent = CombatComponent.MAPPER.get(entity);
        DamagedAction.DamagedActionComponent data = DamagedAction.MAPPER.get(entity);
        RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);
        EffectsComponent effectsComponent = EffectsComponent.MAPPER.get(entity);

        //play sounds
        if (damageInformation.damage == 0) {
            SoundEffects.playLoud(FAILED_HIT_SOUND_EFFECT);
            return;
        }
        SoundEffects.play(combatComponent.HURT_SOUND_EFFECT);

        //take damage
        combatComponent.hp -= damageInformation.damage;
        if (combatComponent.hp <= 0) {
            combatComponent.dead = true;
        }

        //handle effects
        effectsComponent.startSquish(DamagedAction.TOTAL_KNOCKBACK_TIME * .75f, 1.2f);
        if (combatComponent.hp <= 0) {
            effectsComponent.flash(Color.RED);
        } else {
            effectsComponent.flash(Color.WHITE);
        }
        combatComponent.startInvincibilityFrames();
        if (transform.ENTITY_NAME.equals("player")) {
            Application.getInstance().freezeTime();
            //TODO weapons
            //Application.playerOLD.equippedWeapon.exitAttackState();
        }
    }

    public boolean isInvincible() {
        return inInvincibilityFrames || !isAttackable;
    }

    public void setInvincibility(boolean b) {
        inInvincibilityFrames = b;
    }

    public void setAttackable(boolean b) {
        isAttackable = b;
    }
}
