package com.mikm.entities.player.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.mikm.entities.Entity;
import com.mikm.entities.player.Player;
import com.mikm.entities.projectiles.StaticHurtbox;
import com.mikm.rendering.BatchUtils;
import com.mikm.rendering.screens.Application;

public abstract class SwingableWeapon extends Weapon {
    private final Animation<TextureRegion> sliceAnimation;
    private float sliceAnimationTimer;
    private boolean showSlice = false;

    private final float SLICE_ANIMATION_SPEED = .1f;
    private final float SWING_SPEED = .75f;

    public SwingableWeapon(TextureRegion image, TextureRegion[] sliceSpritesheet, Player player) {
        super(image, player);
        sliceAnimation = new Animation<>(SLICE_ANIMATION_SPEED, sliceSpritesheet);
        sliceAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        staticHurtbox = new StaticHurtbox(getSliceBounds().width, false);
    }

    @Override
    public float getTotalAttackTime() {
        return .4f;
    }

    @Override
    public void checkForHit() {
        for (Entity entity : player.screen.entities) {
            if (entity != player && entity.isAttackable() && Intersector.overlaps(staticHurtbox.getHurtbox(), entity.getHitbox())) {
                entity.damagedState.enter(getDamageInformation());
            }
        }
    }

    @Override
    public void enterAttackState() {
        showSlice = true;
        sliceAnimationTimer = 0;
        checkForHit();
    }

    @Override
    public void exitAttackState() {
        shouldSwingRight = !shouldSwingRight;
        showSlice = false;
    }

    @Override
    public void update() {
        orbitAroundMouse();
        staticHurtbox.setPosition(player.getCenteredPosition().x, player.getCenteredPosition().y, Application.TILE_WIDTH, angleToMouse);
    }

    public void attackUpdate() {
        angleOffset += (shouldSwingRight? 1 : -1) * SWING_SPEED;
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
        return new Rectangle(player.getCenteredPosition().x, player.getCenteredPosition().y, 32, 32);
    }
}
