package com.mikm.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.mikm.Vector2Int;
import com.mikm.rendering.cave.Rock;
import com.mikm.rendering.cave.RockType;

public class RockSerializer extends Serializer<Rock> {
    @Override
    public void write(Kryo kryo, Output output, Rock object) {
        kryo.writeObject(output, new Vector2Int(object.x, object.y));
        kryo.writeObject(output, object.rockType);
    }

    @Override
    public Rock read(Kryo kryo, Input input, Class<? extends Rock> type) {
        Vector2Int position = kryo.readObject(input, Vector2Int.class);
        RockType rockType = kryo.readObject(input, RockType.class);
        return new Rock(position.x, position.y, rockType);
    }
}
