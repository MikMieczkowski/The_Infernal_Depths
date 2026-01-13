package com.mikm._components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm.rendering.cave.SpawnProbability;

public class SpawnComponent implements Component {
    public static final ComponentMapper<SpawnComponent> MAPPER = ComponentMapper.getFor(SpawnComponent.class);
    @Copyable public SpawnProbability spawnProbability;
}
