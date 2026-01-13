package com.mikm._components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class ProjectileComponent implements Component {
    public static final ComponentMapper<ProjectileComponent> MAPPER = ComponentMapper.getFor(ProjectileComponent.class);
    public boolean isPlayer;
}
