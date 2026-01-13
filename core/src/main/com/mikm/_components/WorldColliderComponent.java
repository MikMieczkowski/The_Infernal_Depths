package com.mikm._components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.mikm.Vector2Int;

//Attaching this has many side effects. The entity will now be moved in it's velocity. It will also collide with the world grid and escape collision.
public class WorldColliderComponent implements Component {
    public static final ComponentMapper<WorldColliderComponent> MAPPER = ComponentMapper.getFor(WorldColliderComponent.class);

    @Copyable public Vector2Int HITBOX_OFFSETS = new Vector2Int();
    @Copyable public float RADIUS;
    @Copyable public boolean IS_BAT = false;
    //is used by any system
    public boolean active = true;

    // Temporary fields for per-frame calculations    public final Vector2 nextPosition = new Vector2();
    public Vector2Int tilePosition = new Vector2Int();
    public Vector2 nextPosition = new Vector2();
    public Vector2Int nextTilePosition = new Vector2Int();

    public Circle getHitbox(Transform transform) {
        return new Circle(transform.getCenteredX() + HITBOX_OFFSETS.x, transform.getCenteredY() + HITBOX_OFFSETS.y, RADIUS);
    }

    public void onWallCollision() {

    }
}