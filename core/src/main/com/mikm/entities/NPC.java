package com.mikm.entities;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.mikm.entities.player.Player;
import com.mikm.input.GameInput;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.cave.RockType;

public class NPC extends InanimateEntity {
    private Player player;
    private TextureRegion image;
    private final float TALKING_RANGE_DIAMETER = Application.TILE_WIDTH;

    public NPC(TextureRegion image, float x, float y) {
        super(x, y);
        this.image = image;
        player = Application.player;
    }

    @Override
    public void update() {
        if (GameInput.isTalkButtonJustPressed() && isPlayerInTalkingRange()) {
            sellPlayerOres();
        }
    }

    private void sellPlayerOres() {
        for (RockType rockType : RockType.values()) {
            player.money += rockType.oreAmount * rockType.sellPrice;
            rockType.oreAmount = 0;
        }
    }

    @Override
    public void draw(Batch batch) {
        batch.draw(image, x, y);
        drawHitboxes(batch,  new Circle(x+16, y+16, TALKING_RANGE_DIAMETER/2));
    }

    @Override
    public Rectangle getShadowBounds() {
        return new Rectangle(x + 8, y + 6, Application.TILE_WIDTH, Application.TILE_HEIGHT);
    }

    private boolean isPlayerInTalkingRange() {
        return Intersector.overlaps(player.getHitbox(), new Circle(x+16, y+16, TALKING_RANGE_DIAMETER/2));
    }
}
