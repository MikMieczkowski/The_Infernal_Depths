package com.mikm._components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.mikm.rendering.cave.RockType;

public class RockComponent implements Component {
    public static final ComponentMapper<RockComponent> MAPPER = ComponentMapper.getFor(RockComponent.class);
    @Copyable public RockType rockType;

    public RockComponent() {

    }

    public RockComponent(RockType rockType) {
        this.rockType = rockType;
    }
}
