package com.mikm.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.mikm.Vector2Int;
import com.mikm.entities.Entity;
import com.mikm.entities.enemies.Rat;
import com.mikm.entities.enemies.Slime;
import com.mikm.rendering.cave.CaveFloorMemento;
import com.mikm.rendering.cave.Rock;
import com.mikm.rendering.cave.RockType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class Serializer {
    private static Serializer instance;
    //TODO
    private final String SAVEFILE_PATH = "E:/IntelliJprojects/The_Infernal_Depths/assets/testingFile.bin";

    private final Kryo kryo;
    private final Input input;
    private final Output output;

    private Serializer() {
        kryo = new Kryo();
        registerKryo();
        try {
            input = new Input(new FileInputStream(SAVEFILE_PATH));
            output = new Output(new FileOutputStream(SAVEFILE_PATH));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Serializer getInstance() {
        if (instance == null) {
            instance = new Serializer();
        }
        return instance;
    }

    public void write(Object object) {
        kryo.writeObject(output, object);
        output.flush();
    }

    public <T> T read(Class<T> type) {
        return kryo.readObject(input, type);
    }

    public <T> T copy(T o) {
        return kryo.copy(o);
    }

    public void dispose() {
        input.close();
        output.close();
    }

    private void registerKryo() {
        //Disallows multiple instances of an object
        kryo.setReferences(false);
        kryo.register(Slime.class, new EntitySerializer());
        kryo.register(Rat.class, new EntitySerializer());
        kryo.register(ArrayList.class);
        kryo.register(Rock.class, new RockSerializer());
        kryo.register(RockType.class);
        kryo.register(boolean[][].class);
        kryo.register(boolean[].class);
        kryo.register(Vector2Int.class);
        kryo.register(CaveFloorMemento.class, new CaveFloorMementoSerializer());
    }
}
