package com.mikm.entities.animation;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;

public class ActionSpritesheetsAllDirections {
    public ArrayList<TextureRegion[]> list = new ArrayList<>();

    ActionSpritesheetsAllDirections() {

    }

    public TextureRegion[] getSpritesheetByDirection(int direction) {
        return list.get(direction);
    }

    public void add(TextureRegion[] oneDirection) {
        list.add(oneDirection);
    }

    public static ActionSpritesheetsAllDirections createFromSpritesheetRange(
            ArrayList<TextureRegion[]> rawSplitSpritesheets, int startingAnimationIndex) {
        ActionSpritesheetsAllDirections actionSpritesheets = new ActionSpritesheetsAllDirections();
        for (int i = 0; i < Directions.TOTAL; i++) {
            int animationIndex = i + startingAnimationIndex;
            actionSpritesheets.add(rawSplitSpritesheets.get(animationIndex));
        }
        return actionSpritesheets;
    }

    public static ActionSpritesheetsAllDirections createFromSpritesheetRange(
            ArrayList<TextureRegion[]> rawSplitSpritesheets, int startingAnimationIndex, boolean useFirstImages) {
        if (!useFirstImages) {
            return createFromSpritesheetRange(rawSplitSpritesheets, startingAnimationIndex);
        }
        ActionSpritesheetsAllDirections actionSpritesheets = new ActionSpritesheetsAllDirections();
        for (int i = 0; i < Directions.TOTAL; i++) {
            int animationIndex = i + startingAnimationIndex;
            actionSpritesheets.add(new TextureRegion[]{rawSplitSpritesheets.get(animationIndex)[0]});
        }
        return actionSpritesheets;
    }

    public static ActionSpritesheetsAllDirections createOneDirectional(TextureRegion[] spritesheet) {
        ActionSpritesheetsAllDirections actionSpritesheets = new ActionSpritesheetsAllDirections();
        actionSpritesheets.add(spritesheet);
        return actionSpritesheets;
    }
}
