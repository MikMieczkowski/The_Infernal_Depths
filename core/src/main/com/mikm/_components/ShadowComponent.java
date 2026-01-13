package com.mikm._components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.Rectangle;

public class ShadowComponent implements Component {
    public static final ComponentMapper<ShadowComponent> MAPPER = ComponentMapper.getFor(ShadowComponent.class);
    @Copyable public Rectangle BOUNDS_OFFSETS;
    public boolean active = true;
}
