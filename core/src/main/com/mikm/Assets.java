package com.mikm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.rendering.screens.Application;

import java.util.ArrayList;

import static com.mikm.rendering.screens.Application.TILE_HEIGHT;
import static com.mikm.rendering.screens.Application.TILE_WIDTH;

public class Assets {
    private static Assets instance;

    public static TextureRegion testTexture;
    private AssetManager assetManager;
    private TextureAtlas textureAtlas;
    public static BitmapFont font;

    public static TextureRegion light;
    public static TextureRegion dark;
    public static TextureRegion gray;
    public static TextureRegion shadowImage;
    public static TextureRegion[][] particleImages;

    private Assets() {
        assetManager = createAssetManager();
        textureAtlas = getAsset("images/The Infernal Depths.atlas", TextureAtlas.class);

        testTexture = textureAtlas.findRegion("sand").split(TILE_WIDTH, TILE_HEIGHT)[0][0];
        shadowImage = textureAtlas.findRegion("shadow").split(Application.TILE_WIDTH,Application.TILE_HEIGHT)[0][0];
        particleImages = textureAtlas.findRegion("particles").split(8,8);
        light = new TextureRegion(new Texture(Gdx.files.internal("images/R0x4x.png")));
        dark = new TextureRegion(new Texture(Gdx.files.internal("images/dark.png")));
        gray = new TextureRegion(new Texture(Gdx.files.internal("images/gray.png")));

        font = new BitmapFont(Gdx.files.internal("fonts/EquipmentPro.fnt"));
        font.getData().setScale(1f);

    }

    public static Assets getInstance() {
        if (instance == null) {
            instance = new Assets();
        }
        return instance;
    }

    public void dispose() {
        textureAtlas.dispose();
        assetManager.dispose();
        font.dispose();
    }

    public <T> T getAsset(String name, Class<T> type) {
        return assetManager.get(name, type);
    }

    public TextureRegion getTextureRegion(String name) {
        return getSplitTextureRegion(name)[0][0];
    }

    public TextureRegion getTextureRegion(String name, int width, int height) {
        return getSplitTextureRegion(name, width, height)[0][0];
    }

    public TextureRegion[][] getSplitTextureRegion(String name) {
        return textureAtlas.findRegion(name).split(TILE_WIDTH, TILE_HEIGHT);
    }

    public TextureRegion[][] getSplitTextureRegion(String name, int width, int height) {
        return textureAtlas.findRegion(name).split(width, height);
    }

    public ArrayList<TextureRegion[]> findImagesStartingWith(String startsWith) {
        return findImagesStartingWith(startsWith, Application.TILE_WIDTH, Application.TILE_HEIGHT);
    }

    public ArrayList<TextureRegion[]> findImagesStartingWith(String startsWith, int tileWidth, int tileHeight) {
        ArrayList<TextureAtlas.AtlasRegion> atlasRegions = findAtlasRegionsStartingWith(startsWith);
        return splitAtlasRegionsTo1DArrays(atlasRegions, tileWidth, tileHeight);
    }

    private ArrayList<TextureAtlas.AtlasRegion> findAtlasRegionsStartingWith(String startsWith) {
        ArrayList<TextureAtlas.AtlasRegion> matched = new ArrayList<>();
        for (int i = 0, n = textureAtlas.getRegions().size; i < n; i++) {
            TextureAtlas.AtlasRegion region = textureAtlas.getRegions().get(i);
            if (region.name.startsWith(startsWith)) {
                matched.add(new TextureAtlas.AtlasRegion(region));
            }
        }
        return matched;
    }

    private ArrayList<TextureRegion[]> splitAtlasRegionsTo1DArrays(ArrayList<TextureAtlas.AtlasRegion> regions, int tileWidth, int tileHeight) {
        ArrayList<TextureRegion[]> output = new ArrayList<>();
        for (TextureAtlas.AtlasRegion region : regions) {
            output.add(region.split(tileWidth, tileHeight)[0]);
        }
        return output;
    }

    private AssetManager createAssetManager() {
        AssetManager assetManager = new AssetManager();
        assetManager.load("images/The Infernal Depths.atlas", TextureAtlas.class);
        assetManager.load("sound/caveTheme.mp3", Music.class);
        assetManager.load("sound/townTheme.mp3", Music.class);
        assetManager.load("sound/hubba_bubba.mp3", Music.class);
        assetManager.finishLoading();
        return assetManager;
    }
}
