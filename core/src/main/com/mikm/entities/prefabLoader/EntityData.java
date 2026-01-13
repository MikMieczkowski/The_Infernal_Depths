package com.mikm.entities.prefabLoader;

import com.badlogic.gdx.math.Vector2;
import com.mikm.rendering.cave.RockType;

import java.util.Arrays;

public class EntityData {
    public Vector2 pos;
    public String prefabName;
    //grave component data
    public int[] ores;
    //rock component data
    public RockType rockType;

    public EntityData() {

    }
    public EntityData(Vector2 pos, String prefabName) {
        this.pos = pos;
        this.prefabName = prefabName;
    }

    public EntityData(Vector2 pos, String prefabName, int[] ores) {
        this.pos = pos;
        this.prefabName = prefabName;
        this.ores = ores;
    }

    public EntityData(Vector2 pos, String prefabName, RockType rockType) {
        this.pos = pos;
        this.prefabName = prefabName;
        this.rockType = rockType;
    }

    @Override
    public String toString() {
        return "EntityData{" +
                "pos=" + pos +
                ", prefabName='" + prefabName + '\'' +
                ", ores=" + Arrays.toString(ores) +
                ", rockType=" + rockType +
                '}';
    }
}
