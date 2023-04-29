package com.mikm.rendering.cave;

import com.mikm.serialization.Serializer;
import com.mikm.Vector2Int;
import com.mikm.entities.Entity;
import com.mikm.entities.InanimateEntity;
import com.mikm.entities.RemovableArray;

import java.util.ArrayList;
import java.util.Arrays;

public class CaveFloorMemento {
    public Vector2Int spawnPosition;
    public boolean[][] ruleCellPositions;
    public ArrayList<Vector2Int> holePositions;
    public RemovableArray<Entity> enemies;
    public RemovableArray<InanimateEntity> inanimateEntities;

    public CaveFloorMemento(Vector2Int spawnPosition, boolean[][] ruleCellPositions, ArrayList<Vector2Int> holePositions, RemovableArray<InanimateEntity> inanimateEntities, RemovableArray<Entity> enemies) {
        this.spawnPosition = spawnPosition;
        this.ruleCellPositions = ruleCellPositions;
        this.holePositions = holePositions;
        this.inanimateEntities = inanimateEntities;
        this.enemies = enemies;
    }

    public static CaveFloorMemento create(Vector2Int spawnPosition, boolean[][] ruleCellPositions, ArrayList<Vector2Int> holePositions, RemovableArray<InanimateEntity> inanimateEntities, RemovableArray<Entity> enemies) {
        Serializer.getInstance().write(new CaveFloorMemento(spawnPosition, ruleCellPositions, holePositions, inanimateEntities, enemies));
        return Serializer.getInstance().read(CaveFloorMemento.class);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CaveFloorMemento)) {
            return false;
        }
        CaveFloorMemento other = (CaveFloorMemento) obj;
        return spawnPosition.equals(other.spawnPosition)
                && Arrays.deepEquals(ruleCellPositions, other.ruleCellPositions)
                && enemies.equals(other.enemies)
                && inanimateEntities.equals(other.inanimateEntities);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + spawnPosition.hashCode();
        result = 31 * result + Arrays.deepHashCode(ruleCellPositions);
        result = 31 * result + enemies.hashCode();
        result = 31 * result + inanimateEntities.hashCode();
        return result;
    }
}