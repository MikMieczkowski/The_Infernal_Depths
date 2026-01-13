package com.mikm._components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.entities.animation.SuperAnimation;
import com.mikm.utils.Assets;

//Animated or non-animated
public class SpriteComponent implements Component {
    public static final ComponentMapper<SpriteComponent> MAPPER = ComponentMapper.getFor(SpriteComponent.class);
    @CopyReference public Color color = Color.WHITE;

    public boolean flipped = false;
    public TextureRegion textureRegion = Assets.testTexture;
    public float animationTime;
    public boolean visible = true;

    public SpriteComponent() {

    }

    public SpriteComponent(TextureRegion textureRegion) {
        this.textureRegion = textureRegion;
    }

}
