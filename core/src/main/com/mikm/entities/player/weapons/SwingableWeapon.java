package com.mikm.entities.player.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.mikm.entities.player.states.PlayerAttackingAndWalkingState;
import com.mikm.entities.projectiles.Hurtbox;
import com.mikm.rendering.BatchUtils;

public abstract class SwingableWeapon extends Weapon {
    private final Animation<TextureRegion> sliceAnimation;
    private float sliceAnimationTimer;
    private boolean showSlice = false;
    private float attackTimer;
    private final float SWORD_MOVEMENT_ANIMATION_SPEED = .75f;

    private final float SLICE_ANIMATION_SPEED = .1f;
    //how fast one swing is
    private float timePerSwing;
    private int sliceWidth;


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
    public void checkForStateTransition() {
        if (attackTimer > player.currentHeldItem.getTotalAttackTime()) {
            shouldSwingRight = !shouldSwingRight;
            PlayerAttackingAndWalkingState.ready = true;
            showSlice = false;
            player.walkingState.enter();
        }
    }

    @Override
    public void enterAttackState() {
        showSlice = true;
        attackTimer = 0;
        sliceAnimationTimer = 0;
        checkForHit();
    }

    @Override
    public void update() {
        attackTimer += Gdx.graphics.getDeltaTime();
        orbitAroundMouse();
        hurtbox.setPosition(player.getCenteredPosition().x, player.getCenteredPosition().y, getSliceBounds().width/2f, angleToMouse);
    }

    public void updateDuringAttackState() {
        angleOffset += (shouldSwingRight? 1 : -1) * SWORD_MOVEMENT_ANIMATION_SPEED;
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
    public void draw(Batch batch) {
        super.draw(batch);
        if (showSlice) {
            drawSlice(batch);
        }
    }

    private void drawSlice(Batch batch) {
        sliceAnimationTimer += Gdx.graphics.getDeltaTime();
        Rectangle sliceRectangle = getSliceBounds();
        if (shouldSwingRight == mouseIsLeftOfPlayer) {
            batch.draw(sliceAnimation.getKeyFrame(sliceAnimationTimer), sliceRectangle.x, sliceRectangle.y - sliceRectangle.height/2,
                    0, sliceRectangle.height/2, sliceRectangle.width, sliceRectangle.height, 1, 1, angleToMouse * MathUtils.radDeg);
        } else {
            BatchUtils.drawFlipped(batch, sliceAnimation.getKeyFrame(sliceAnimationTimer), sliceRectangle.x, sliceRectangle.y - sliceRectangle.height/2,
                    0, sliceRectangle.height/2, sliceRectangle.width, sliceRectangle.height, 1, 1, angleToMouse * MathUtils.radDeg, false);
        }

    }

    private Rectangle getSliceBounds() {
        return new Rectangle(player.getCenteredPosition().x, player.getCenteredPosition().y, sliceWidth, 32);
    }
}
