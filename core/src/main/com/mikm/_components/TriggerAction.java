package com.mikm._components;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
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
        return new Break();
    }

    public static TriggerAction displayIndicator() {
        return new DisplayIndicator();
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
            for (int i = 0; i < RockType.SIZE; i++) {
                RockType.get(i).increaseOreAmount(graveComponent.ores[i]);
                //TODO particles
                //new ParticleEffect(ParticleTypes.getLightningParameters(), x, y);
                SoundEffects.playQuiet(REWARD_SOUND_EFFECT);
                Application.getInstance().currentScreen.removeEntity(entity);
                Application.getInstance().caveScreen.updateCurrentMemento();
            }
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

    //destroy, play sound effect, gain ore if ore
    //get RockComponent?
    class Break implements TriggerAction {
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

    class DisplayIndicator implements TriggerAction {
        @Override
        public void run(Entity entity) {
            TriggerComponent triggerComponent = TriggerComponent.MAPPER.get(entity);
            if (triggerComponent.inputAction.equals("TALK")) {
                throw new RuntimeException("Unsupported indicator type");
            }
            Application.batch.draw(GameInput.getTalkButtonImage(), Application.getInstance().getPlayerTransform().getCenteredX()-8, Application.getInstance().getPlayerTransform().getCenteredY()+20);
        }
    }

}

