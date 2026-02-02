package com.mikm._components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

/**
 * Marker component for particles that don't collide with walls.
 * Entities with this component will have simple velocity-based movement applied.
 */
public class ParticleComponent implements Component {
    public static final ComponentMapper<ParticleComponent> MAPPER = ComponentMapper.getFor(ParticleComponent.class);
}
