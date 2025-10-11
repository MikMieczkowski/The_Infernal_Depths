package com.mikm.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.mikm.Vector2Int;
import com.mikm.entities.*;
import com.mikm.entities.inanimateEntities.Grave;
import com.mikm.entities.inanimateEntities.InanimateEntity;
import com.mikm.rendering.cave.CaveFloorMemento;
import com.mikm.rendering.cave.Rock;
import com.mikm.rendering.screens.Application;

import java.util.ArrayList;
import java.util.Iterator;

public class CaveFloorMementoSerializer extends Serializer<CaveFloorMemento> {

    @Override
    public void write(Kryo kryo, Output output, CaveFloorMemento object) {
        // write simple objects (class+object so we don't need to pre-register subclasses)
        kryo.writeClassAndObject(output, object.spawnPosition);   // Vector2Int
        kryo.writeClassAndObject(output, object.ropePosition);    // Vector2Int

        // holePositions is an ArrayList<Vector2Int>
        kryo.writeClassAndObject(output, object.holePositions);

        // Convert RemovableArray -> ArrayList so we don't need to register RemovableArray
        kryo.writeClassAndObject(output, new ArrayList<>(object.inanimateEntities));
        kryo.writeClassAndObject(output, new ArrayList<>(object.enemies));

        // ruleCellPositions (boolean[][])
        kryo.writeClassAndObject(output, object.ruleCellPositions);
    }

    @SuppressWarnings("unchecked")
    @Override
    public CaveFloorMemento read(Kryo kryo, Input input, Class<? extends CaveFloorMemento> type) {
        Vector2Int spawnPosition = (Vector2Int) kryo.readClassAndObject(input);
        Vector2Int ropePosition = (Vector2Int) kryo.readClassAndObject(input);

        ArrayList<Vector2Int> holePositions = (ArrayList<Vector2Int>) kryo.readClassAndObject(input);
        ArrayList<InanimateEntity> inanimateEntitiesRaw = (ArrayList<InanimateEntity>) kryo.readClassAndObject(input);
        ArrayList<Entity> enemiesRaw = (ArrayList<Entity>) kryo.readClassAndObject(input);

        boolean[][] ruleCellPositions = (boolean[][]) kryo.readClassAndObject(input);

        // Null-safety fallback
        if (holePositions == null) holePositions = new ArrayList<>();
        if (inanimateEntitiesRaw == null) inanimateEntitiesRaw = new ArrayList<>();
        if (enemiesRaw == null) enemiesRaw = new ArrayList<>();
        if (ruleCellPositions == null) ruleCellPositions = new boolean[0][0];

        // Extract graves from inanimateEntities if you want to keep them separate like you used to
        ArrayList<InanimateEntity> graves = new ArrayList<>();
        Iterator<InanimateEntity> iter = inanimateEntitiesRaw.iterator();
        while (iter.hasNext()) {
            InanimateEntity ie = iter.next();
            if (ie instanceof Grave) {
                graves.add(ie);
                iter.remove();
            }
        }

        RemovableArray<InanimateEntity> inanimateEntities = new RemovableArray<>(inanimateEntitiesRaw);
        RemovableArray<Entity> enemies = new RemovableArray<>(enemiesRaw);

        // Apply hole/rock modifications to ruleCellPositions (same logic as before)
        for (Vector2Int holePosition : holePositions) {
            if (holePosition != null && holePosition.y >= 0 && holePosition.x >= 0
                    && holePosition.y < ruleCellPositions.length
                    && holePosition.x < ruleCellPositions[0].length) {
                ruleCellPositions[holePosition.y][holePosition.x] = false;
            }
        }
        for (InanimateEntity rock : inanimateEntities) {
            int ry = (int) rock.y / Application.TILE_HEIGHT;
            int rx = (int) rock.x / Application.TILE_WIDTH;
            if (ry >= 0 && ry < ruleCellPositions.length && rx >= 0 && rx < ruleCellPositions[0].length) {
                ruleCellPositions[ry][rx] = false;
            }
        }

        CaveFloorMemento memento = new CaveFloorMemento(spawnPosition, ropePosition, ruleCellPositions, holePositions, inanimateEntities, enemies);
        memento.graves = graves;
        return memento;
    }
}
