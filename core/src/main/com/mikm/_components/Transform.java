package com.mikm._components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mikm.Vector2Int;
import com.mikm.entities.animation.Directions;
import com.mikm.rendering.screens.Application;

public class Transform implements Component, Comparable<Transform> {
    public static final ComponentMapper<Transform> MAPPER = ComponentMapper.getFor(Transform.class);
    public float x, y;
    public float xVel, yVel;
    public float xScale = 1, yScale = 1;
    public float height;
    public Vector2Int direction = Directions.DOWN.vector2Int;
    public float rotation;

    //YAML fields, set to defaults (for enemies not loaded through yaml like rope)
    @Copyable public float SPEED = 1;
    @Copyable public Vector2Int FULL_BOUNDS_DIMENSIONS = new Vector2Int(Application.TILE_WIDTH, Application.TILE_HEIGHT);
    @Copyable public int HALF_BOUNDS_WIDTH = 8;
    @Copyable public int ORIGIN_X = 8;
    @Copyable public int ORIGIN_Y = 0;
    @Copyable public int Z_ORDER = 0;
    @Copyable public int SORT_OFFSET_Y = 0;

    //Non-transform ID data
    @Copyable public String ENTITY_NAME;


    public Transform() {
        x = 0;
        y = 0;
    }

    public Transform(Vector2 pos) {
        this.x = pos.x;
        this.y = pos.y;
    }

    public Transform(Vector2Int pos) {
        this.x = pos.x;
        this.y = pos.y;
    }

    public Transform(float x, float y) {
        this.x = x;
        this.y = y;
    }


    public int getXInt() {
        return (int)x;
    }

    public int getYInt() {
        return (int)y;
    }

    //Margin distances between x,y of fullBounds and bounds
    public Vector2Int getBoundsOffset() {
        return new Vector2Int(x - getBounds().x, y - getBounds().y);
    }

    public Rectangle getBounds() {
        return new Rectangle(getCenteredX() - HALF_BOUNDS_WIDTH, getCenteredY() - HALF_BOUNDS_WIDTH, HALF_BOUNDS_WIDTH*2, HALF_BOUNDS_WIDTH*2);
    }

    public float getCenteredX() {
        return x + getFullBounds().width/2f;
    }

    public float getCenteredY() {
        return y + getFullBounds().height/2f;
    }

    public Rectangle getFullBounds() {
        return new Rectangle(x, y, FULL_BOUNDS_DIMENSIONS.x, FULL_BOUNDS_DIMENSIONS.y);
    }

    @Override
    public int compareTo(Transform other) {
        if (this.Z_ORDER != other.Z_ORDER) {
            return Float.compare(this.Z_ORDER, other.Z_ORDER);
        }
        if (this.y + this.SORT_OFFSET_Y != other.y + other.SORT_OFFSET_Y) {
            return Float.compare(other.y + other.SORT_OFFSET_Y, this.y + this.SORT_OFFSET_Y);
        }
        return Float.compare(this.x, other.x);
    }
}
