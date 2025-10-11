
package com.mikm.entities.enemies;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.mikm.Vector2Int;
import com.mikm.entities.animation.Directions;
import com.mikm.entities.inanimateEntities.InanimateEntity;
import com.mikm.rendering.cave.SpawnProbability;

import java.util.Map;


public class Entity extends InanimateEntity {
    public boolean damagesPlayer = true;
    public float rotation;
    public Vector2Int direction = Directions.DOWN.vector2Int;

    /*From yaml config*/
    public int hp;
    public String NAME;
    public int DAMAGE;
    public int MAX_HP;
    public int ORIGIN_X;
    public int ORIGIN_Y;
    //rename to INVINCIBLE
    public boolean isAttackable = true;
    public Behaviour INITIAL_BEHAVIOUR;
    public Behaviour POST_HIT_CYCLE;
    //public Behaviour ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO;
    public Sound HURT_SOUND_EFFECT = null;
    public SpawnProbability spawnProbability;

    //testing only
    public Map<String, EntityData.BehaviourData> BEHAVIOURS_TEST;
    public Map<String, EntityData.RoutineData> CYCLES_TEST;

    public AnimationHandler animationHandler = new AnimationHandler(this);
    private EntityEffectsHandler effectsHandler = new EntityEffectsHandler(this);
    public RoutineHandler routineHandler = new RoutineHandler(this);



    //only Entity and EntityLoader can access
    Entity(float x, float y) {
        super(x,y);
    }

    Entity(Entity entity) {
        super(entity.x,entity.y);
    }

    @Override
    public void update() {
        routineHandler.update();
        effectsHandler.handleSquishAndInvincibility();
        //this added some code to particle - is that breaking stuff?
        moveAndCheckCollisions();
    }

    @Override
    public void draw(Batch batch) {
        effectsHandler.handleFlash(batch);
        animationHandler.draw(batch);
    }

    public void die() {
        //Application.getInstance().currentScreen.removeEntity(this);
    }

    public void startInvincibilityFrames() {
        effectsHandler.startInvincibilityFrames();
    }
    public void startSquish(float squishDelay, float squishAmount) {
        effectsHandler.startSquish(squishDelay, squishAmount);
    }
    public void startSquish(float squishDelay, float squishAmount, float timeSpentSquishing, boolean overrideLastSquish) {
        effectsHandler.startSquish(squishDelay, squishAmount, timeSpentSquishing, overrideLastSquish);
    }
    public void stopSquish() {
        effectsHandler.stopSquish();
    }
    public void flash(Color color) {
        effectsHandler.flash(color);
    }
}

