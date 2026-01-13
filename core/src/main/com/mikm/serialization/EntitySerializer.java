package com.mikm.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.mikm.Vector2Int;

//public class EntitySerializer extends Serializer<Entity> {
//    @Override
//    public void write(Kryo kryo, Output output, Entity object) {
//        kryo.writeObject(output, new Vector2Int(object.x, object.y));
//        kryo.writeObject(output, object.hp);
//        kryo.writeObject(output, object.NAME);
//    }
//
//    @Override
//    public Entity read(Kryo kryo, Input input, Class<? extends Entity> type) {
//        Vector2Int position = kryo.readObject(input, Vector2Int.class);
//        int hp = kryo.readObject(input, int.class);
//        String NAME = kryo.readObject(input, String.class);
//        Entity obj = EntityLoaderOLD.create(NAME);
//        obj.hp = hp;
//        obj.x = position.x;
//        obj.y = position.y;
//        return obj;
//    }
//}
