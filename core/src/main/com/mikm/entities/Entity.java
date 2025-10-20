
package com.mikm.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.mikm.ExtraMathUtils;
import com.mikm.Vector2Int;
import com.mikm.entities.actions.Action;
import com.mikm.entities.animation.Directions;
import com.mikm.entities.actions.DamagedAction;
import com.mikm.entities.animation.EntityAnimationHandler;
import com.mikm.entities.routineHandler.Routine;
import com.mikm.entities.routineHandler.RoutineHandler;
import com.mikm.entities.inanimateEntities.InanimateEntity;
import com.mikm.entityLoader.Blackboard;
import com.mikm.rendering.cave.SpawnProbability;
import com.mikm.rendering.screens.Application;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class Entity extends InanimateEntity {
    public boolean damagesPlayer = true;
    public boolean isCopied = false;
    public float rotation;

    /*From yaml config*/
    public int hp;
    public String NAME;
    public int DAMAGE;
    public int KNOCKBACK;
    public int MAX_HP;
    public float SPEED;
    public int ORIGIN_X;
    public int ORIGIN_Y;
    //rename to "invincible" - not constant
    public boolean isAttackable = true;
    public String HURT_SOUND_EFFECT = null;
    public SpawnProbability spawnProbability;
    public boolean HAS_SHADOW;

    public EntityAnimationHandler animationHandler = new EntityAnimationHandler(this);
    public EntityEffectsHandler effectsHandler = new EntityEffectsHandler(this);
    public RoutineHandler routineHandler = new RoutineHandler(this);

    public Rectangle SHADOW_BOUNDS_OFFSETS;
    public Vector2Int FULL_BOUNDS_DIMENSIONS;
    public Vector2Int HITBOX_OFFSETS;
    public int HITBOX_RADIUS;

    public DamagedAction damagedAction = new DamagedAction(this);
    public Set<String> usedActionClasses = new HashSet<>();

//    private Entity() {
//        super(0, 0);
//    }

    //only Entity EntityLoader and Player can access. They load the entity there.
    public Entity(float x, float y) {
        super(x,y);
    }

    @Override
    public void update() {
        if (damagedAction.active) {
            damagedAction.update();
        } else {
            routineHandler.update();
        }
        effectsHandler.handleSquishAndInvincibility();
        //this added some code to particle - is that breaking stuff?
        moveAndCheckCollisions();
        updateDirection();
    }

    private void updateDirection() {
        if (!(xVel == 0 && yVel == 0)) {
            direction = ExtraMathUtils.angleToVector2Int(MathUtils.atan2(yVel, xVel));
        }
    }

    @Override
    public void draw() {
        effectsHandler.handleFlash();
        animationHandler.draw();
    }

    public void die() {
        Application.getInstance().currentScreen.removeEntity(this);
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

    @Override
    public Circle getHitbox() {
        return new Circle(x+ getFullBounds().width/2f + HITBOX_OFFSETS.x, y + getFullBounds().height/2f + HITBOX_OFFSETS.y, HITBOX_RADIUS);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(getHitbox().x - getHitbox().radius, getHitbox().y - getHitbox().radius, getHitbox().radius*2, getHitbox().radius*2);
    }

    public Rectangle getFullBounds() {
        return new Rectangle(x, y, FULL_BOUNDS_DIMENSIONS.x, FULL_BOUNDS_DIMENSIONS.y);
    }

    public Rectangle getShadowBounds() {
        return new Rectangle(x + SHADOW_BOUNDS_OFFSETS.x, y + SHADOW_BOUNDS_OFFSETS.y, SHADOW_BOUNDS_OFFSETS.width, SHADOW_BOUNDS_OFFSETS.height);
    }


    @Override
    public boolean hasShadow() {
        return HAS_SHADOW;
    }

    public float angleTo(Entity entity) {
        return MathUtils.atan2(y - entity.y, x - entity.x);
    }
}

