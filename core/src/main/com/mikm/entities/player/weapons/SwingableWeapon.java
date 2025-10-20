package com.mikm.entities.player.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.mikm.entities.actions.PlayerAttackingAction;
import com.mikm.entities.inanimateEntities.projectiles.Hurtbox;
import com.mikm.rendering.BatchUtils;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.sound.SequentialSound;
import com.mikm.rendering.sound.SoundEffects;

public abstract class SwingableWeapon extends Weapon {
    private final Animation<TextureRegion> sliceAnimation;
    private float sliceAnimationTimer;
    private boolean showSlice = false;
    private float attackTimer;
    private final float SWORD_MOVEMENT_ANIMATION_SPEED = .5f;

    private final float SLICE_ANIMATION_SPEED = .1f;
    //how fast one swing is
    private float timePerSwing;
    private int sliceWidth;
    private SequentialSound swing;
    private String SWING_SOUND_EFFECT_STARTS_WITH = "swing";


    public SwingableWeapon(TextureRegion image, TextureRegion[] sliceSpritesheet, float timePerSwing, int sliceWidth) {
        super(image);
        this.timePerSwing = timePerSwing;
        sliceAnimation = new Animation<>(SLICE_ANIMATION_SPEED, sliceSpritesheet);
        sliceAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        this.sliceWidth = sliceWidth;
        hurtbox = new Hurtbox(getSliceBounds().width, false);
    }

    @Override
    public float getTotalAttackTime() {
        return timePerSwing;
    }

    @Override
    public void checkForHit() {
        hurtbox.setDamageInformation(getDamageInformation());
        hurtbox.checkIfHitEntities(true);
    }

    @Override
    public void exitAttackState() {
        shouldSwingRight = !shouldSwingRight;
        showSlice = false;
    }

    @Override
    public void enterAttackState() {
        showSlice = true;
        attackTimer = 0;
        sliceAnimationTimer = 0;
        SoundEffects.play(SWING_SOUND_EFFECT_STARTS_WITH);
        checkForHit();
    }

    @Override
    public void update() {
        attackTimer += Gdx.graphics.getDeltaTime();
        orbitAroundMouse();
        hurtbox.setPosition(player.getHitbox().x, player.getHitbox().y, getSliceBounds().width/2f, angleToMouse);
    }

    public void updateDuringAttackState() {
        angleOffset += (shouldSwingRight? 1 : -1) * SWORD_MOVEMENT_ANIMATION_SPEED * Gdx.graphics.getDeltaTime() * 60f;
        clampAngleOffset();
    }

    private void clampAngleOffset() {
        if (angleOffset < 0) {
            angleOffset = 0;
        }
        if (angleOffset > 3) {
            angleOffset = 3;
        }
    }

    @Override
    public void draw() {
        super.draw();
        if (showSlice) {
            drawSlice();
        }
    }

    private void drawSlice() {
        sliceAnimationTimer += Gdx.graphics.getDeltaTime();
        Rectangle sliceRectangle = getSliceBounds();
        if (shouldSwingRight == mouseIsLeftOfPlayer) {
            Application.batch.draw(sliceAnimation.getKeyFrame(sliceAnimationTimer), sliceRectangle.x, sliceRectangle.y - sliceRectangle.height/2,
                    0, sliceRectangle.height/2, sliceRectangle.width, sliceRectangle.height, 1, 1, angleToMouse * MathUtils.radDeg);
        } else {
            BatchUtils.drawFlipped(Application.batch, sliceAnimation.getKeyFrame(sliceAnimationTimer), sliceRectangle.x, sliceRectangle.y - sliceRectangle.height/2,
                    0, sliceRectangle.height/2, sliceRectangle.width, sliceRectangle.height, 1, 1, angleToMouse * MathUtils.radDeg, false);
        }

    }

    private Rectangle getSliceBounds() {
        return new Rectangle(player.getHitbox().x, player.getHitbox().y, sliceWidth, 32);
    }
}
