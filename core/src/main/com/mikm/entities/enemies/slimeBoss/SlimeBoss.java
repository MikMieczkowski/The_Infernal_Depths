package com.mikm.entities.enemies.slimeBoss;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mikm.entities.Entity;
import com.mikm.entities.animation.EntityActionSpritesheets;

public class SlimeBoss extends Entity {
    public final TextureRegion image;

    public SlimeBossJumpState jumpState;
    public SlimeBossJumpBuildUpState jumpBuildUpState;
    public SlimeBossSimmerState simmerState;



    public SlimeBoss(float x, float y, TextureRegion image, EntityActionSpritesheets hitthing) {
        super(x, y, hitthing);
        this.image = image;
    }


    @Override
    public void createStates() {
        jumpState = new SlimeBossJumpState(this, 300);
        jumpBuildUpState = new SlimeBossJumpBuildUpState(this);
        simmerState = new SlimeBossSimmerState(this);
        simmerState.enter();
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, 32, 32);
    }

    public Vector2 getCenteredPosition() {
        return new Vector2(x + getBounds().width/2f, y + getBounds().height/2f);
    }

    @Override
    public float getOriginX() {
        return getBounds().width/2f;
    }

    @Override
    public int getMaxHp() {
        return 0;
    }
}
