package com.mikm.entities.inanimateEntities;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mikm.DeltaTime;
import com.mikm.Vector2Int;
import com.mikm.entities.animation.AnimationHandler;
import com.mikm.entities.animation.Directions;
import com.mikm.entities.animation.EntityAnimationHandler;
import com.mikm.entities.collision.Collider;
import com.mikm.rendering.screens.Application;

public abstract class InanimateEntity {

    public float x, y;
    public float xVel, yVel;
    public float xScale = 1, yScale = 1;
    public float height;
    public InanimateEntity shadow;
    public Collider collider = new Collider(this);
    public AnimationHandler animationHandler;
    //Not really used for inanimate entities but needed for support with AnimationHandler
    public Vector2Int direction = Directions.DOWN.vector2Int;
    public float rotation;

    public InanimateEntity(float x, float y) {
        this.x = x;
        this.y = y;
        animationHandler = new AnimationHandler(this);
    }

    public void render() {
        update();
        draw();
    }

    public void die() {
        Application.getInstance().currentScreen.removeInanimateEntity(this);
    }

    public abstract void update();

    public void moveAndCheckCollisions() {
        float dt = DeltaTime.deltaTime();
        collider.moveWithCollisions(dt);
        if (collider.inWall()) {
            collider.ejectFromWalls();
        }
    }

    public void move() {
        if (DeltaTime.deltaTime() < 3) {
            x += xVel * DeltaTime.deltaTime();
            y += yVel * DeltaTime.deltaTime();
        }
    }

    public void onWallCollision() {

    }

    public abstract void draw();

    public int getXInt() {
        return (int)x;
    }

    public int getYInt() {
        return (int)y;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, Application.TILE_WIDTH, Application.TILE_HEIGHT);
    }

    public Rectangle getFullBounds() {
        return new Rectangle(x, y, getBounds().width, getBounds().height);
    }

    public Circle getHitbox() {
        return new Circle(getBounds().x+getBounds().width/2f, getBounds().y+getBounds().height/2f, getBounds().width/2f);
    }

    public Rectangle getShadowBounds() {
        return getBounds();
    }

    public Vector2 getBoundsOffset() {
        return new Vector2(x - getBounds().x, y - getBounds().y);
    }

    public boolean hasShadow() {
        return true;
    }
}
