package com.mikm.rendering;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;

public class TextureAtlasUtils {
    public static ArrayList<TextureAtlas.AtlasRegion> findRegionsStartingWith(String startsWith, TextureAtlas atlas) {
        ArrayList<TextureAtlas.AtlasRegion> matched = new ArrayList<>();
        for (int i = 0, n = atlas.getRegions().size; i < n; i++) {
            TextureAtlas.AtlasRegion region = atlas.getRegions().get(i);
            if (region.name.startsWith(startsWith)) {
                matched.add(new TextureAtlas.AtlasRegion(region));
            }
        }
        return matched;
    }

    public static ArrayList<TextureRegion[]> splitAtlasRegionsTo1DArrays(ArrayList<TextureAtlas.AtlasRegion> regions, int tileWidth, int tileHeight) {
        ArrayList<TextureRegion[]> output = new ArrayList<>();
        for (TextureAtlas.AtlasRegion region : regions) {
            output.add(region.split(tileWidth, tileHeight)[0]);
        }
        return output;
    }
}

