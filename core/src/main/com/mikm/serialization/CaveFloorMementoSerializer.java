package com.mikm.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.mikm.Vector2Int;
import com.mikm.entities.Entity;
import com.mikm.entities.InanimateEntity;
import com.mikm.entities.RemovableArray;
import com.mikm.rendering.cave.CaveFloorMemento;
import com.mikm.rendering.cave.Rock;
import com.mikm.rendering.screens.Application;

import java.util.ArrayList;

public class CaveFloorMementoSerializer extends Serializer<CaveFloorMemento> {

    @Override
    public void write(Kryo kryo, Output output, CaveFloorMemento object) {
        kryo.writeObject(output, object.spawnPosition);
        kryo.writeObject(output, object.holePositions);

        ArrayList<InanimateEntity> rocks = new ArrayList<>();
        for (InanimateEntity inanimateEntity : object.inanimateEntities) {
            if (inanimateEntity.getClass().equals(Rock.class)) {
                rocks.add(inanimateEntity);
            }
        }
        kryo.writeObject(output, rocks);

        ArrayList<Entity> enemiesArrayList = new ArrayList<>(object.enemies);
        enemiesArrayList.remove(Application.player);
        kryo.writeObject(output, enemiesArrayList);

        kryo.writeObject(output, object.ruleCellPositions);
    }

    @SuppressWarnings("unchecked")
    @Override
    public CaveFloorMemento read(Kryo kryo, Input input, Class<? extends CaveFloorMemento> type) {
        Vector2Int spawnPosition = kryo.readObject(input, Vector2Int.class);

        ArrayList<Vector2Int> holePositions = kryo.readObject(input, ArrayList.class);

        ArrayList<InanimateEntity> inanimateEntitiesRaw = kryo.readObject(input, ArrayList.class);
        ArrayList<Entity> enemiesRaw = kryo.readObject(input, ArrayList.class);
        RemovableArray<InanimateEntity> inanimateEntities = new RemovableArray<>(inanimateEntitiesRaw);
        RemovableArray<Entity> enemies = new RemovableArray<>(enemiesRaw);

        boolean[][] ruleCellPositions = kryo.readObject(input, boolean[][].class);
        for (Vector2Int holePosition : holePositions) {
            ruleCellPositions[holePosition.y][holePosition.x] = false;
        }
        for (InanimateEntity rock : inanimateEntities) {
            ruleCellPositions[(int) rock.y/Application.TILE_HEIGHT][(int) rock.x / Application.TILE_WIDTH] = false;
        }

        return new CaveFloorMemento(spawnPosition, ruleCellPositions, holePositions, inanimateEntities, enemies);
    }
}
