package com.mikm.rendering;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;

public class TextureAtlasUtils {
    private TextureAtlasUtils() {

    }

    public static ArrayList<TextureRegion[]> findSplitTextureRegionsStartingWith(String startsWith, TextureAtlas textureAtlas, int tileWidth, int tileHeight) {
        ArrayList<TextureAtlas.AtlasRegion> atlasRegions = TextureAtlasUtils.findAtlasRegionsStartingWith(startsWith, textureAtlas);
        ArrayList<TextureRegion[]> splitTextureRegions = TextureAtlasUtils.splitAtlasRegionsTo1DArrays(atlasRegions, tileWidth, tileHeight);
        return splitTextureRegions;
    }

    private static ArrayList<TextureAtlas.AtlasRegion> findAtlasRegionsStartingWith(String startsWith, TextureAtlas atlas) {
        ArrayList<TextureAtlas.AtlasRegion> matched = new ArrayList<>();
        for (int i = 0, n = atlas.getRegions().size; i < n; i++) {
            TextureAtlas.AtlasRegion region = atlas.getRegions().get(i);
            if (region.name.startsWith(startsWith)) {
                matched.add(new TextureAtlas.AtlasRegion(region));
            }
        }
        return matched;
    }

    private static ArrayList<TextureRegion[]> splitAtlasRegionsTo1DArrays(ArrayList<TextureAtlas.AtlasRegion> regions, int tileWidth, int tileHeight) {
        ArrayList<TextureRegion[]> output = new ArrayList<>();
        for (TextureAtlas.AtlasRegion region : regions) {
            output.add(region.split(tileWidth, tileHeight)[0]);
        }
        return output;
    }
}

