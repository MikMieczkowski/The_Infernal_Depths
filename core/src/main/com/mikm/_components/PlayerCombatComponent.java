package com.mikm._components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class PlayerCombatComponent implements Component {
    public static final ComponentMapper<PlayerCombatComponent> MAPPER = ComponentMapper.getFor(PlayerCombatComponent.class);

    public static final float RESPAWN_TIME = 3;
    public float respawnTimer = 0;
    public int swordLevel = 0;
    public int bowLevel = 0;
    public boolean holdingPickaxe = false;
    //Current attack's max time / other info



}
