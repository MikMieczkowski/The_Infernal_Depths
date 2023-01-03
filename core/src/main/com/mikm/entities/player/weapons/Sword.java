package com.mikm.entities.player.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.mikm.entities.player.Player;
import com.mikm.rendering.BatchUtils;

public class Sword extends Weapon {
    private final Animation<TextureRegion> sliceAnimation;
    private float sliceAnimationTimer;
    private boolean showSlice = false;

    private final float sliceAnimationSpeed = .1f;
    private final float swingSpeed = .75f;

    public Sword(TextureRegion image, TextureRegion[] sliceSpritesheet, Player player) {
        super(image, player);
        sliceAnimation = new Animation<>(sliceAnimationSpeed, sliceSpritesheet);
        sliceAnimation.setPlayMode(Animation.PlayMode.NORMAL);
    }

    @Override
    public float getTotalAttackTime() {
        return .4f;
    }

    @Override
    public void enterAttackState() {
        showSlice = true;
        sliceAnimationTimer = 0;
    }

    @Override
    public void exitAttackState() {
        shouldSwingRight = !shouldSwingRight;
        showSlice = false;
    }

    @Override
    public void update() {
        orbitAroundMouse();
    }

    public void attackUpdate() {
        angleOffset += (shouldSwingRight? 1 : -1) * swingSpeed;
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
