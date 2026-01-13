package com.mikm.serialization;

import com.badlogic.ashley.core.Entity;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.mikm.Vector2Int;
import com.mikm.entities.*;
import com.mikm.entities.prefabLoader.EntityData;
import com.mikm.rendering.cave.CaveFloorMemento;
import com.mikm.rendering.screens.Application;

import java.util.ArrayList;

public class CaveFloorMementoSerializer extends Serializer<CaveFloorMemento> {
    @Override
    public void write(Kryo kryo, Output output, CaveFloorMemento object) {
        //Vector2Int spawnPosition, Vector2Int ropePosition, boolean[][] ruleCellPositions, ArrayList<Vector2Int> holePositions, ArrayList< EntityData > entities

        kryo.writeObject(output, object.spawnPosition);
        kryo.writeObject(output, object.ropePosition);
        kryo.writeObject(output, object.ruleCellPositions);
        kryo.writeObject(output, object.holePositions);
        kryo.writeObject(output, object.enemies);
        kryo.writeObject(output, object.rocks);
    }

    @SuppressWarnings("unchecked")
    @Override
    public CaveFloorMemento read(Kryo kryo, Input input, Class<? extends CaveFloorMemento> type) {
        Vector2Int spawnPosition = kryo.readObject(input, Vector2Int.class);
        Vector2Int ropePosition = kryo.readObject(input, Vector2Int.class);
        boolean[][] ruleCellPositions = kryo.readObject(input, boolean[][].class);
        ArrayList<Vector2Int> holePositions = (ArrayList<Vector2Int>)kryo.readObject(input, ArrayList.class);
        ArrayList<EntityData> enemies = (ArrayList<EntityData>)kryo.readObject(input, ArrayList.class);
        ArrayList<EntityData> rocks = (ArrayList<EntityData>)kryo.readObject(input, ArrayList.class);

        CaveFloorMemento memento = new CaveFloorMemento(spawnPosition, ropePosition, ruleCellPositions, holePositions, enemies, rocks);
        return memento;

    }
}
