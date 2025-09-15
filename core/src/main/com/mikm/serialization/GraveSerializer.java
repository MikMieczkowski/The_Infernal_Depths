package com.mikm.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.mikm.Vector2Int;
import com.mikm.entities.inanimateEntities.Grave;
import com.mikm.rendering.cave.RockType;

public class GraveSerializer extends Serializer<Grave> {
    @Override
    public void write(Kryo kryo, Output output, Grave object) {
        kryo.writeObject(output, new Vector2Int(object.x, object.y));
        for (int i = 0; i < RockType.SIZE; i++) {
            kryo.writeObject(output, object.ores[i]);
        }
    }

    @Override
    public Grave read(Kryo kryo, Input input, Class<? extends Grave> type) {
        Vector2Int position = kryo.readObject(input, Vector2Int.class);
        int[] ores = new int[RockType.SIZE];
        for (int i = 0; i < RockType.SIZE; i++) {
            ores[i] = kryo.readObject(input, int.class);
        }
        Grave g = new Grave(position.x, position.y, true);
        g.ores = ores;
        return g;
    }
}
