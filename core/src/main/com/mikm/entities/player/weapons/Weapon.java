package com.mikm.entities.player.weapons;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.Hurtbox;
import com.mikm.entities.player.Player;
import com.mikm.input.InputAxis;
import com.mikm.rendering.screens.Application;

public abstract class Weapon {
    TextureRegion image;
    Hurtbox hurtbox;
    Player player;

    float x, y;
    public int zIndex = 0;

    float weaponRotation;
    float angleOffset;
    int orbitDistance = 15;
    boolean mouseIsLeftOfPlayer = false;
    boolean shouldSwingRight = true;
    float angleToMouse;

    public Weapon(TextureRegion image, Player player) {
        this.image = image;
        this.player = player;
    }

    public abstract void checkForHit();

    public abstract void enterAttackState();

    public abstract void exitAttackState();

    public abstract void update();

    public abstract void attackUpdate();

    public abstract float getTotalAttackTime();

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
        angleToMouse = InputAxis.getAttackingAngle();
        mouseIsLeftOfPlayer = -MathUtils.HALF_PI < angleToMouse && angleToMouse < MathUtils.HALF_PI;

        float weaponAngle = angleToMouse - MathUtils.HALF_PI;
        weaponRotation = angleToMouse + MathUtils.PI * .75f;
        if (mouseIsLeftOfPlayer) {
            weaponAngle += MathUtils.PI;
            weaponRotation += MathUtils.PI;
        }
        if (mouseIsLeftOfPlayer) {
            weaponRotation -= angleOffset;
            weaponAngle -= angleOffset;
        } else {
            weaponRotation += angleOffset;
            weaponAngle += angleOffset;
        }

        x = ExtraMathUtils.roundToTenths(player.getCenteredPosition().x + orbitDistance * MathUtils.cos(weaponAngle) - getFullBounds().width/2);
        y = ExtraMathUtils.roundToTenths(player.getCenteredPosition().y + orbitDistance * MathUtils.sin(weaponAngle) - getFullBounds().height/2) - 6;
        return new Vector2(x, y);
    }

    public Rectangle getFullBounds() {
        return new Rectangle(x, y, Application.defaultTileWidth, Application.defaultTileHeight);
    }

    public void draw(Batch batch) {
        batch.draw(image, x, y, 8, 8, getFullBounds().width, getFullBounds().height, 1, 1, weaponRotation * MathUtils.radDeg);
    }
}
