package com.mikm.entities.inanimateEntities;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.mikm.RandomUtils;
import com.mikm.entities.player.Player;
import com.mikm.input.GameInput;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.BlacksmithScreen;
import com.mikm.rendering.sound.SoundEffects;

public class NPC extends InanimateEntity {
    private Player player;
    private final float TALKING_RANGE_DIAMETER = 48;

    public NPC(float x, float y) {
        super(x, y);
        player = Application.player;
    }

    @Override
    public void update() {
        if (GameInput.isTalkButtonJustPressed() && isPlayerInTalkingRange()) {
            Application.getInstance().blacksmithScreen.showMenu = !Application.getInstance().blacksmithScreen.showMenu;

            Application.getInstance().blacksmithScreen.tipNumber = RandomUtils.getBoolean() ? 0 : 2;
            // Play blacksmith talking line at open
            if (Application.getInstance().blacksmithScreen.showMenu) {
                if (BlacksmithScreen.talkedToTimes >= 4) {
                    SoundEffects.play(BlacksmithScreen.BLACKSMITH_ANNOYED_SOUND_EFFECT);
                } else {
                    SoundEffects.play("blacksmith");
                }
                BlacksmithScreen.talkedToTimes++;
            }
        }
        if (GameInput.isMenuCancelButtonJustPressed()) {
            Application.getInstance().blacksmithScreen.showMenu = false;
        }
    }

    @Override
    public void draw() {

    }

    private void sellPlayerOres() {

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
