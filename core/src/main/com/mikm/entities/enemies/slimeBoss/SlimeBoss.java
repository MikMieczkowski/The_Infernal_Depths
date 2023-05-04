package com.mikm.entities.enemies.slimeBoss;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mikm.Assets;
import com.mikm.entities.Entity;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.animation.DirectionalAnimation;
import com.mikm.rendering.screens.SlimeBossRoomScreen;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.HashMap;
import java.util.Map;

public class SlimeBoss extends Entity {
    public boolean visible = true;
    static ShapeDrawer shapeDrawer;

    public SB_JumpBuildUpState jumpBuildUpState;
    public SB_SimmerAttack simmerState;
    public SB_JumpAttack jumpState;
    public SB_DashAttack dashState;
    public SB_DashBuildUpState dashBuildUpState;
    public SB_SimmerBuildUpState simmerBuildUpState;
    public SB_SplitAttack splitState;
    private static Map<AnimationName, DirectionalAnimation> animations = new HashMap<>();

    public SB_StateManager stateManager;

    public SlimeBossRoomScreen screen;

    public static TextureRegion slimeBossImage = Assets.getInstance().getTextureRegion("slimeBoss", 32, 32);
    private static TextureRegion shapeDrawerTexture = Assets.getInstance().getTextureRegion("shapeDrawerTexture", 4, 4);

    public SlimeBoss(SlimeBossRoomScreen screen, float x, float y) {
        super(x, y);
        isAttackable = true;

        this.screen = screen;
        createAnimations();
        createStates();
    }

    @Override
    public void draw(Batch batch) {
        if (visible) {
            super.draw(batch);
        }
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
    protected void createAnimations() {
        DirectionalAnimation animation = new DirectionalAnimation("slimeBoss", 32, 32);
        animations.put(AnimationName.HIT, animation);
        animations.put(AnimationName.SLIMEBOSS_STAND, animation);
    }

    @Override
    protected Map<?,?> getAnimations() {
        return animations;
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
