package com.mikm.entities.player.weapons;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mikm.entities.projectiles.DamageInformation;
import com.mikm.entities.projectiles.Hurtbox;
import com.mikm.entities.player.Player;
import com.mikm.input.GameInput;
import com.mikm.rendering.screens.Application;

public abstract class Weapon {
    public TextureRegion image;
    Hurtbox hurtbox;
    final Player player = Application.player;

    float x, y;
    public int zIndex = 0;

    float weaponRotation;
    float angleOffset;
    int orbitDistance = 15;
    boolean mouseIsLeftOfPlayer = false;
    boolean shouldSwingRight = true;
    float angleToMouse;

    public Weapon(TextureRegion image) {
        this.image = image;
    }

    public abstract void checkForHit();

    public abstract void checkForStateTransition();

    public abstract void enterAttackState();

    public abstract void update();

    public abstract void updateDuringAttackState();

    public abstract float getTotalAttackTime();

    public abstract DamageInformation getDamageInformation();

    void orbitAroundMouse() {
        Vector2 playerPositionOrbitedAroundMouse = getPlayerPositionOrbitedAroundMouse();
        x = playerPositionOrbitedAroundMouse.x;
        y = playerPositionOrbitedAroundMouse.y;

        setWeaponZIndex();
    }

    private void setWeaponZIndex() {
        if (x > player.getCenteredPosition().x) {
            zIndex=1;
        } else {
            zIndex=0;
        }
    }

    private Vector2 getPlayerPositionOrbitedAroundMouse() {
        float x, y;
        angleToMouse = GameInput.getAttackingAngle();
        mouseIsLeftOfPlayer = -MathUtils.HALF_PI < angleToMouse && angleToMouse < MathUtils.HALF_PI;

        float weaponAngle = angleToMouse - MathUtils.HALF_PI;
        weaponRotation = angleToMouse + MathUtils.PI/2;
        final float ratioFromWeaponArcToFullRotation = (3+MathUtils.PI/2)/3;
        if (mouseIsLeftOfPlayer) {
            weaponAngle += MathUtils.PI;
            weaponRotation -= MathUtils.PI/2;
        }
        if (mouseIsLeftOfPlayer) {
            weaponRotation -= angleOffset*ratioFromWeaponArcToFullRotation;
            weaponAngle -= angleOffset;
        } else {
            weaponRotation += angleOffset*ratioFromWeaponArcToFullRotation;
            weaponAngle += angleOffset;
        }

        x = player.getCenteredPosition().x + orbitDistance * MathUtils.cos(weaponAngle) - getFullBounds().width/2;
        y = player.getCenteredPosition().y + orbitDistance * MathUtils.sin(weaponAngle) - getFullBounds().height/2 - 6;
        return new Vector2(x, y);
    }

    public Rectangle getFullBounds() {
        return new Rectangle(x, y, Application.TILE_WIDTH, Application.TILE_HEIGHT);
    }

    public void draw(Batch batch) {
        batch.draw(image, x, y, 8, 8, getFullBounds().width, getFullBounds().height, 1, 1, weaponRotation * MathUtils.radDeg);
    }
}
