package com.mikm.rendering;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;

public abstract class Screen extends ScreenAdapter {
    Application application;
    Stage stage;
    AssetManager assetManager;
    OrthographicCamera camera;

    Screen(Application application, AssetManager assetManager) {
        this.assetManager = assetManager;
        this.application = application;
        camera = new OrthographicCamera();
        stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera));
    }

    @Override
    public void render(float delta) {

    }

    @Override
    public void resize (int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        assetManager.dispose();
    }
}
