package com.mikm._systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm._components.AttackInputComponent;
import com.mikm._components.ComboStateComponent;
import com.mikm._components.Transform;
import com.mikm.entities.prefabLoader.PrefabInstantiator;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.sound.SoundEffects;

/**
 * System that spawns visual effects when charge thresholds are reached.
 * - Light effect: spawns immediately when attack button is pressed (0 seconds)
 * - Heavy effect: spawns when HEAVY_THRESHOLD is reached
 */
public class ChargeEffectSystem extends IteratingSystem {

    private static final int FRAME_COUNT = 3;
    private static final float FPS = 10f;

    private TextureRegion[] lightFrames;
    private TextureRegion[] heavyFrames;

    // Track which thresholds have triggered effects (reset when attack ends)
    private boolean lightEffectSpawned;
    private boolean heavyEffectSpawned;

    public ChargeEffectSystem() {
        super(Family.all(AttackInputComponent.class, ComboStateComponent.class, Transform.class).get());
        loadEffectTextures();
    }

    private void loadEffectTextures() {
        lightFrames = loadSpritesheet("images/source/lightEffect.png");
        heavyFrames = loadSpritesheet("images/source/heavyEffect.png");
    }

    private TextureRegion[] loadSpritesheet(String path) {
        Texture texture = new Texture(Gdx.files.internal(path));
        int frameWidth = texture.getWidth() / FRAME_COUNT;
        int frameHeight = texture.getHeight();

        TextureRegion[] frames = new TextureRegion[FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; i++) {
            frames[i] = new TextureRegion(texture, i * frameWidth, 0, frameWidth, frameHeight);
        }
        return frames;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        if (!Application.getInstance().systemShouldTick()) {
            return;
        }

        AttackInputComponent input = AttackInputComponent.MAPPER.get(entity);
        ComboStateComponent combo = ComboStateComponent.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);

        if (input.isHolding && !combo.isAttacking) {

            // Heavy effect: spawn when heavy threshold is reached
            if (!heavyEffectSpawned && input.holdTimer >= AttackInputComponent.HEAVY_THRESHOLD) {
                spawnEffect(transform, heavyFrames);
                SoundEffects.play("hammer1.ogg");
                heavyEffectSpawned = true;
            }
        } else {
            // Reset when not holding
            lightEffectSpawned = false;
            heavyEffectSpawned = false;
        }
    }

    private void spawnEffect(Transform playerTransform, TextureRegion[] frames) {
        // Center the effect on the player
        float x = playerTransform.getCenteredX() - frames[0].getRegionWidth() / 2f;
        float y = playerTransform.getCenteredY() - frames[0].getRegionHeight() / 2f;

        PrefabInstantiator.addAnimatedDecoration(
                Application.getInstance().currentScreen,
                x, y,
                frames,
                FPS
        );
    }
}
