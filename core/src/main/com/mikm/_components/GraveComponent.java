package com.mikm._components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.mikm.rendering.cave.RockType;

@RuntimeDataComponent
public class GraveComponent implements Component {
    public static final ComponentMapper<GraveComponent> MAPPER = ComponentMapper.getFor(GraveComponent.class);
    public int[] ores = new int[RockType.SIZE];

    public GraveComponent() {

    }

    public GraveComponent(int[] ores) {
        this.ores = ores;
    }
}
