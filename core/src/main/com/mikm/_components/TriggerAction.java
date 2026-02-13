package com.mikm._components;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm.entities.DamageInformation;
import com.mikm.entities.inanimateEntities.particles.ParticleTypes;
import com.mikm.entities.prefabLoader.PrefabInstantiator;
import com.mikm.input.GameInput;
import com.mikm.rendering.cave.RockType;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.BlacksmithScreen;
import com.mikm.rendering.screens.CaveScreen;
import com.mikm.rendering.screens.GameScreen;
import com.mikm.rendering.sound.SoundEffects;
import com.mikm.utils.RandomUtils;
import com.mikm._systems.ComboSystem;
import com.mikm.utils.debug.DebugRenderer;

public interface TriggerAction {
    void run(Entity entity);

    public static TriggerAction increaseCaveFloor() {
        return new IncreaseCaveFloor();
    }

    public static TriggerAction decreaseCaveFloor() {
        return new DecreaseCaveFloor();
    }

    public static TriggerAction goToScreen() {
        return new GoToScreen();
    }

    public static TriggerAction graveCollect() {
        return new GraveCollect();
    }

    public static TriggerAction npcTalk() {
        return new NPCTalk();
    }

    public static TriggerAction breakRock() {
        return new BreakRock();
    }

    public static TriggerAction breakDestructible() {
        return new BreakDestructible();
    }

    public static TriggerAction displayIndicator() {
        return new DisplayIndicator();
    }

    public static TriggerAction playerHit() {
        return new PlayerHit();
    }

    public static TriggerAction enemyHit() {
        return new EnemyHit();
    }

    public static TriggerAction bump() {
        return new Bump();
    }

    class IncreaseCaveFloor implements TriggerAction {
        @Override
        public void run(Entity entity) {
            Application.getInstance().caveScreen.increaseFloor();
        }
    }

    class DecreaseCaveFloor implements TriggerAction {
        private String ROPE_CLIMB_SOUND_EFFECT = "ropeClimb.ogg";

        @Override
        public void run(Entity entity) {
            SoundEffects.playLoud(ROPE_CLIMB_SOUND_EFFECT);
            Application.getInstance().caveScreen.decreaseFloor();
        }
    }

    class GoToScreen implements TriggerAction {
        @Override
        public void run(Entity entity) {
            TriggerComponent triggerComponent = TriggerComponent.MAPPER.get(entity);
            Application.getInstance().setGameScreen(Application.getInstance().screens[triggerComponent.goToScreenTriggerActionScreen]);
        }
    }

    class GraveCollect implements TriggerAction {
        private final String REWARD_SOUND_EFFECT = "reward.ogg";
        @Override
        public void run(Entity entity) {
            GraveComponent graveComponent = GraveComponent.MAPPER.get(entity);
            Transform transform = Transform.MAPPER.get(entity);
            for (int i = 0; i < RockType.SIZE; i++) {
                RockType.get(i).increaseOreAmount(graveComponent.ores[i]);
            }
            PrefabInstantiator.addParticles(transform.x, transform.y, ParticleTypes.getLightningParameters());
            SoundEffects.playQuiet(REWARD_SOUND_EFFECT);
            Application.getInstance().currentScreen.removeEntity(entity);
            Application.getInstance().caveScreen.updateCurrentMemento();
        }
    }

    class NPCTalk implements TriggerAction {
        @Override
        public void run(Entity entity) {
            Application.getInstance().blacksmithScreen.showMenu = !Application.getInstance().blacksmithScreen.showMenu;
            Application.getInstance().blacksmithScreen.tipNumber = RandomUtils.getBoolean() ? 0 : 2;

            // Play blacksmith talking line at open
            if (Application.getInstance().blacksmithScreen.showMenu) {
                if (BlacksmithScreen.talkedToTimes >= 4) {
                    SoundEffects.play(BlacksmithScreen.BLACKSMITH_ANNOYED_SOUND_EFFECT);
                } else {
                    SoundEffects.play("blacksmith");
                }
                BlacksmithScreen.talkedToTimes++;
            }
        }
    }

    //destroy rock, play sound effect, gain ore if ore
    class BreakRock implements TriggerAction {
        private String ROCK_BREAK_SOUND_EFFECT = "rockBreak.ogg";
        private String REWARD_SOUND_EFFECT = "reward.ogg";
        @Override
        public void run(Entity entity) {
            Application.getInstance().currentScreen.removeEntity(entity);

            CaveScreen caveScreen = Application.getInstance().caveScreen;
            Transform transform = Transform.MAPPER.get(entity);

            //TODO memento rework
            boolean[][] rockGrid = caveScreen.caveTilemapCreator.rockCollidablePositions;
            boolean[][] grid = caveScreen.isCollidableGrid();
            grid[(int)transform.y/ Application.TILE_HEIGHT][(int)transform.x / Application.TILE_WIDTH] = false;
            rockGrid[(int)transform.y/ Application.TILE_HEIGHT][(int)transform.x / Application.TILE_WIDTH] = false;

            SoundEffects.play(ROCK_BREAK_SOUND_EFFECT);
            RockComponent rockComponent = RockComponent.MAPPER.get(entity);
            RockType rockType = rockComponent.rockType;
            if (rockType != RockType.NORMAL) {
                rockType.increaseOreAmount();
                SoundEffects.playQuiet(REWARD_SOUND_EFFECT);
            }
            PrefabInstantiator.addParticles(transform.x, transform.y, ParticleTypes.getRockParameters(rockType));
        }
    }

    //destroy destructible (grass, pots, etc), play sound effect, spawn particles
    class BreakDestructible implements TriggerAction {
        @Override
        public void run(Entity entity) {
            Transform transform = Transform.MAPPER.get(entity);
            DestructibleComponent destructible = DestructibleComponent.MAPPER.get(entity);

            Application.getInstance().currentScreen.removeEntity(entity);

            if (destructible.soundEffect != null) {
                SoundEffects.play(destructible.soundEffect);
            }
            if (destructible.particleType != null) {
                PrefabInstantiator.addParticles(transform.x, transform.y, destructible.particleType);
            }
        }
    }

    class DisplayIndicator implements TriggerAction {
        @Override
        public void run(Entity entity) {
            TriggerComponent triggerComponent = TriggerComponent.MAPPER.get(entity);
            // Show indicator when inputAction is "TALK" (for NPCs, etc.)
            if (triggerComponent.inputAction != null && triggerComponent.inputAction.equals("TALK")) {
                Application.batch.draw(GameInput.getTalkButtonImage(), Application.getInstance().getPlayerTransform().getCenteredX()-8, Application.getInstance().getPlayerTransform().getCenteredY()+20);
            } else {
                throw new RuntimeException("Unsupported indicator type: " + triggerComponent.inputAction);
            }
        }
    }

    //Damage enemy when player body overlaps - only during active attacks
    class PlayerHit implements TriggerAction {
        @Override
        public void run(Entity entity) {
            Entity player = Application.getInstance().getPlayer();
            // Safety check: don't damage the player
            if (entity == player || Transform.MAPPER.get(entity).ENTITY_NAME.equals("player")) {
                return;
            }

            // Only deal body contact damage during an active attack
            ComboStateComponent combo = ComboStateComponent.MAPPER.get(player);
            if (combo == null || !combo.isAttacking) {
                return;
            }

            CombatComponent playerCombatComponent = CombatComponent.MAPPER.get(player);
            CombatComponent enemyCombatComponent = CombatComponent.MAPPER.get(entity);
            RoutineListComponent enemyRoutineListComponent = RoutineListComponent.MAPPER.get(entity);
            Transform playerTransform = Transform.MAPPER.get(player);
            Transform enemyTransform = Transform.MAPPER.get(entity);

            if (playerCombatComponent == null || enemyCombatComponent == null || enemyRoutineListComponent == null || enemyCombatComponent.isInvincible()) {
                return;
            }

            float angleToEnemy = (float) Math.atan2(enemyTransform.y - playerTransform.y, enemyTransform.x - playerTransform.x);

            int hitstunFrames = combo.currentAttackData.HITSTUN_FRAMES;

            DamageInformation damageInformation = new DamageInformation(angleToEnemy, playerCombatComponent.KNOCKBACK, playerCombatComponent.DAMAGE, hitstunFrames);
            enemyRoutineListComponent.takeDamage(damageInformation, entity);
        }
    }

    //Damage enemy when player projectile stays on it
    class EnemyHit implements TriggerAction {
        @Override
        public void run(Entity entity) {
            // Find the intersecting player projectile
            TriggerComponent triggerComponent = TriggerComponent.MAPPER.get(entity);
            Transform transform = Transform.MAPPER.get(entity);
            Circle triggerHitbox = new Circle(transform.x+8, transform.y+8, triggerComponent.diameter/2f);

            CombatComponent enemyCombatComponent = CombatComponent.MAPPER.get(entity);
            RoutineListComponent enemyRoutineListComponent = RoutineListComponent.MAPPER.get(entity);
            EffectsComponent enemyEffectsComponent = EffectsComponent.MAPPER.get(entity);

            // Make sure enemy has all required components and is not invincible
            if (enemyCombatComponent == null || enemyRoutineListComponent == null || enemyEffectsComponent == null || enemyCombatComponent.isInvincible()) {
                return;
            }

            for (Entity projectile : Application.getInstance().currentScreen.engine.getEntitiesFor(Family.all(ProjectileComponent.class).get())) {
                if (ProjectileComponent.MAPPER.get(projectile).isPlayer) {
                    // Get projectile config and check if hitbox is still active
                    ProjectileConfigComponent config = ProjectileConfigComponent.MAPPER.get(projectile);
                    if (config != null && !config.isHitboxActive()) {
                        continue; // Hitbox is no longer active, skip this projectile
                    }

                    Transform projectileTransform = Transform.MAPPER.get(projectile);
                    float radius = (config != null) ? config.hitboxRadius : 16f;
                    Circle projectileHitbox = new Circle(projectileTransform.getCenteredX(), projectileTransform.getCenteredY(), radius);
                    if (Intersector.overlaps(projectileHitbox, triggerHitbox)) {
                        float angleToEnemy = (float) Math.atan2(transform.y - projectileHitbox.y, transform.x - projectileHitbox.x);

                        // Get damage from projectile config if available
                        int damage = (config != null) ? config.damage : 1;
                        int knockback = 5;

                        // Get hitstun from player's current attack data
                        Entity player = Application.getInstance().getPlayer();
                        ComboStateComponent playerCombo = ComboStateComponent.MAPPER.get(player);
                        int hitstunFrames = playerCombo.currentAttackData.HITSTUN_FRAMES;

                        DamageInformation damageInformation = new DamageInformation(angleToEnemy, knockback, damage, hitstunFrames);
                        enemyRoutineListComponent.takeDamage(damageInformation, entity);
                        Application.getInstance().currentScreen.removeEntity(projectile);

                        // Check if this was a launcher attack
                        if (playerCombo.currentAttackData.IS_LAUNCHER) {
                            // Trigger aerial state transition
                            ComboSystem comboSystem = Application.getInstance().currentScreen.engine.getSystem(ComboSystem.class);
                            if (comboSystem != null) {
                                comboSystem.onLauncherHit(player, entity);
                            }
                        }

                        return;
                    }
                }
            }
        }
    }

    //Knockback only, no damage when mining projectile stays on enemy
    class Bump implements TriggerAction {
        @Override
        public void run(Entity entity) {
            // Find the intersecting mining projectile
            TriggerComponent triggerComponent = TriggerComponent.MAPPER.get(entity);
            Transform transform = Transform.MAPPER.get(entity);
            Circle triggerHitbox = new Circle(transform.x+8, transform.y+8, triggerComponent.diameter/2f);

            CombatComponent enemyCombatComponent = CombatComponent.MAPPER.get(entity);
            RoutineListComponent enemyRoutineListComponent = RoutineListComponent.MAPPER.get(entity);

            if (enemyCombatComponent == null || enemyRoutineListComponent == null) {
                return;
            }

            for (Entity projectile : Application.getInstance().currentScreen.engine.getEntitiesFor(Family.all(MiningProjectileComponent.class).get())) {
                Transform projectileTransform = Transform.MAPPER.get(projectile);
                ProjectileConfigComponent config = ProjectileConfigComponent.MAPPER.get(projectile);
                float radius = (config != null) ? config.hitboxRadius : 16f;
                Circle projectileHitbox = new Circle(projectileTransform.getCenteredX(), projectileTransform.getCenteredY(), radius);
                if (Intersector.overlaps(projectileHitbox, triggerHitbox)) {
                    float angleToEnemy = (float) Math.atan2(transform.y - projectileHitbox.y, transform.x - projectileHitbox.x);
                    Transform enemyTransform = Transform.MAPPER.get(entity);
                    enemyTransform.xVel += Math.cos(angleToEnemy) * 2;
                    enemyTransform.yVel += Math.sin(angleToEnemy) * 2;
                    return;
                }
            }
        }
    }

}

