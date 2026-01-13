package com.mikm.rendering.cave;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.mikm._components.GraveComponent;
import com.mikm._components.RockComponent;
import com.mikm._components.Transform;
import com.mikm.entities.prefabLoader.EntityData;
import com.mikm.entities.prefabLoader.PrefabInstantiator;
import com.mikm.rendering.screens.CaveScreen;
import com.mikm.serialization.Serializer;
import com.mikm.Vector2Int;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CaveFloorMemento {

    //TODO change to load Memento, then create from memento
    //these two will merge into Tile[][]
    public boolean[][] ruleCellPositions;
    //No need to store rock positions for collision because we can loop through tile[][] to find rocks
    //No need to store hole positions because the only usage, cavescreen will use tile[][] instead.
    public ArrayList<Vector2Int> holePositions;


    //These three will merge because you can look for rope and you can look for player
    public Vector2Int ropePosition;
    public Vector2Int spawnPosition;
    //important field. Will contain enemies, player w/ spawn pos, rope w/ spawn pos, hole behaviourEntities, rock behaviourEntities, (Both are
    //This is all of the entities in the floor just moved to EntityData version.
    //Do this OR have enemy names+pos, player pos, rope pos, graveData (ores, pos), rockData (pos, type)

    public ArrayList<EntityData> enemies = new ArrayList<>();
    public ArrayList<EntityData> rocks = new ArrayList<>();

    //helper field to find graves
    //will be in entities
    //TODO add back graves
//    public ArrayList<InanimateEntity> graves;

    //turns Entity objects into EntityData objects and adds them to enemies or rocks
    private void loadEntityData(ImmutableArray<Entity> entities) {
        for (Entity entity : entities) {
            Transform transform = Transform.MAPPER.get(entity);
            GraveComponent graveComponent = GraveComponent.MAPPER.get(entity);
            RockComponent rockComponent = RockComponent.MAPPER.get(entity);

            EntityData entityData = new EntityData(new Vector2(transform.x, transform.y), transform.ENTITY_NAME);
            if (graveComponent != null) {
                entityData.ores = graveComponent.ores;
            }
            if (rockComponent != null) {
                entityData.rockType = rockComponent.rockType;
                rocks.add(entityData);
            } else {
                enemies.add(entityData);
            }
        }
    }
    public CaveFloorMemento(Vector2Int spawnPosition, Vector2Int ropePosition, boolean[][] ruleCellPositions, ArrayList<Vector2Int> holePositions) {
        initPartial(spawnPosition, ropePosition, ruleCellPositions, holePositions);

        //loadEntityData(unloadedEntities);
    }

    public CaveFloorMemento(Vector2Int spawnPosition, Vector2Int ropePosition, boolean[][] ruleCellPositions, ArrayList<Vector2Int> holePositions, ArrayList<EntityData> enemies, ArrayList<EntityData> rocks) {
        init(spawnPosition, ropePosition, ruleCellPositions, holePositions, enemies, rocks);
    }

    private void init(Vector2Int spawnPosition, Vector2Int ropePosition, boolean[][] ruleCellPositions, ArrayList<Vector2Int> holePositions, ArrayList<EntityData> enemies, ArrayList<EntityData> rocks) {
        initPartial(spawnPosition, ropePosition, ruleCellPositions, holePositions);
        this.enemies = enemies;
        this.rocks = rocks;
    }

    private void init(Vector2Int spawnPosition, Vector2Int ropePosition, boolean[][] ruleCellPositions, ArrayList<Vector2Int> holePositions, ArrayList<EntityData> entities) {
        initPartial(spawnPosition, ropePosition, ruleCellPositions, holePositions);
        //split rocks and enemies
        for (EntityData entityData : entities) {
            if (entityData.rockType != null) {
                rocks.add(entityData);
            } else {
                enemies.add(entityData);
            }
        }
    }
    private void initPartial(Vector2Int spawnPosition, Vector2Int ropePosition, boolean[][] ruleCellPositions, ArrayList<Vector2Int> holePositions) {
        this.spawnPosition = spawnPosition;
        this.ropePosition = ropePosition;
        this.ruleCellPositions = ruleCellPositions;
        this.holePositions = holePositions;
    }

    public void updateRocks(ImmutableArray<Entity> rocks) {
        //this.rocks.clear();
        //loadEntityData(rocks);
    }

    public static CaveFloorMemento create(Vector2Int spawnPosition, Vector2Int ropePosition, boolean[][] ruleCellPositions, ArrayList<Vector2Int> holePositions) {
        Serializer.getInstance().write(new CaveFloorMemento(spawnPosition, ropePosition, ruleCellPositions, holePositions), CaveScreen.floor-1);
        return Serializer.getInstance().read(CaveFloorMemento.class, CaveScreen.floor-1);
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
                && rocks.equals(other.rocks);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + spawnPosition.hashCode();
        result = 31 * result + Arrays.deepHashCode(ruleCellPositions);
        result = 31 * result + enemies.hashCode();
        result = 31 * result + rocks.hashCode();
        return result;
    }
}