package com.mikm.entities.enemies.slimeBoss;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mikm.entities.Entity;
import com.mikm.entities.animation.EntityActionSpritesheets;
import com.mikm.rendering.screens.SlimeBossRoomScreen;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class SlimeBoss extends Entity {
    public final TextureRegion image;
    public boolean visible = true;
    static ShapeDrawer shapeDrawer;

    public SB_JumpBuildUpState jumpBuildUpState;
    public SB_SimmerAttack simmerState;
    public SB_JumpAttack jumpState;
    public SB_DashAttack dashState;
    public SB_DashBuildUpState dashBuildUpState;
    public SB_SimmerBuildUpState simmerBuildUpState;
    public SB_SplitAttack splitState;

    public SB_StateManager stateManager;

    public SlimeBossRoomScreen screen;

    public SlimeBoss(SlimeBossRoomScreen screen, float x, float y, TextureRegion image, TextureRegion shapeDrawerTexture, EntityActionSpritesheets hitthing) {
        super(x, y, hitthing);
        this.image = image;
        isAttackable = true;

        this.screen = screen;
        createStates();
    }


    @Override
    public void createStates() {
        stateManager = new SB_StateManager(this);

        damagedState = new SB_DamagedState(this);
        jumpState = new SB_JumpAttack(this);
        jumpBuildUpState = new SB_JumpBuildUpState(this);
        simmerState = new SB_SimmerAttack(this);
        dashState = new SB_DashAttack(this);
        simmerBuildUpState = new SB_SimmerBuildUpState(this);
        dashBuildUpState = new SB_DashBuildUpState(this);
        splitState = new SB_SplitAttack(this);

        stateManager.updateState();
    }

    @Override
    public void draw(Batch batch) {
        if (visible) {
            super.draw(batch);
        }
    }

    @Override
    public Circle getHitbox() {
        return new Circle(getBounds().x+getBounds().width/2f, getBounds().y+getBounds().height/2f, getBounds().width/2f+4);
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
        return 100;
    }
}
