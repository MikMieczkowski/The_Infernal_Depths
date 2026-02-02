package com.mikm._components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.mikm.entities.inanimateEntities.particles.ParticleTypes;

public class DestructibleComponent implements Component {
    public static final ComponentMapper<DestructibleComponent> MAPPER = ComponentMapper.getFor(DestructibleComponent.class);

    public ParticleTypes particleType;
    public String soundEffect;
}
