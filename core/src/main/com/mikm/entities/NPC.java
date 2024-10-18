package com.mikm.entities;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.mikm.RandomUtils;
import com.mikm.debug.DebugRenderer;
import com.mikm.entities.player.Player;
import com.mikm.input.GameInput;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.cave.RockType;

public class NPC extends InanimateEntity {
    private Player player;
    private final float TALKING_RANGE_DIAMETER = 48;

    public NPC(float x, float y) {
        super(x, y);
        player = Application.player;
    }

    @Override
    public void update() {
        if ((GameInput.isTalkButtonJustPressed()||GameInput.isSwitchButtonJustPressed()) && isPlayerInTalkingRange()) {
            Application.getInstance().blacksmithScreen.showMenu = !Application.getInstance().blacksmithScreen.showMenu;

            Application.getInstance().blacksmithScreen.tipNumber = RandomUtils.getBoolean() ? 0 : 2;
        }
    }

    private void sellPlayerOres() {

    }

    @Override
    public void draw(Batch batch) {
    }

    @Override
    public Rectangle getShadowBounds() {
        return new Rectangle(x + 8, y + 6, Application.TILE_WIDTH, Application.TILE_HEIGHT);
    }

    public boolean isPlayerInTalkingRange() {
        return Intersector.overlaps(player.getHitbox(), new Circle(x, y, TALKING_RANGE_DIAMETER/2));
    }


    @Override
    public boolean hasShadow() {
        return false;
    }
}
