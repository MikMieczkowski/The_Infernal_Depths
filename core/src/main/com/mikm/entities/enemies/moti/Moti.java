package com.mikm.entities.enemies.moti;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mikm.Assets;
import com.mikm.entities.Entity;
import com.mikm.entities.animation.AnimationName;
import com.mikm.entities.animation.DirectionalAnimation;
import com.mikm.entities.enemies.states.StandingState;
import com.mikm.rendering.SoundEffects;
import com.mikm.rendering.screens.MotiScreen;

import java.util.HashMap;
import java.util.Map;

public class Moti extends Entity {
    public static final float secPerBeat = 0.41379310344f;
    private static Map<AnimationName, DirectionalAnimation> animations = new HashMap<>();

    public Moti_StateManager stateManager;
    public Moti_TripleDashState tripleDashState;
    public Moti_WebAttack webAttack;
    public Moti_Dig digAttack;
    public MotiScreen screen;

    public static TextureRegion motiImage = Assets.getInstance().getTextureRegion("moti", 48, 48);

    public Moti(MotiScreen screen, float x, float y) {
        super(x, y);
        isAttackable = true;
        this.screen = screen;
    }

    @Override
    public void draw(Batch batch) {
        super.draw(batch);
        //System.out.println(currentState);
    }

    @Override
    public void createStates() {
        stateManager = new Moti_StateManager(this);
        tripleDashState = new Moti_TripleDashState(this);
        webAttack = new Moti_WebAttack(this);
        digAttack = new Moti_Dig(this);
        damagedState = new Moti_DamagedState(this);
        damagedState.interruptState = false;
        stateManager.updateState();
    }

    @Override
    protected void createAnimations() {
        DirectionalAnimation animation = new DirectionalAnimation("moti", 48, 48);
        animations.put(AnimationName.HIT, animation);
        animations.put(AnimationName.STAND, animation);
        animations.put(AnimationName.WALK, animation);
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
        return new Rectangle(x, y, 96, 96);
    }

    public Vector2 getCenteredPosition() {
        return new Vector2(x + getBounds().width/2f, y + getBounds().height/2f);
    }

    @Override
    public Rectangle getShadowBounds() {
        return new Rectangle(x, y+getBounds().height/4f, getBounds().width, getBounds().height*1.25f);
    }

    @Override
    public float getOriginX() {
        return getBounds().width/2f;
    }

    @Override
    public int getMaxHp() {
        return 100;
    }

    @Override
    public Sound getHitSound() {
        return SoundEffects.slimeHit;
    }
}
