package com.mikm.serialization;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.mikm.Vector2Int;
import com.mikm.entities.enemies.Bat;
import com.mikm.entities.enemies.Rat;
import com.mikm.entities.enemies.Slime;
import com.mikm.rendering.cave.CaveFloorMemento;
import com.mikm.rendering.cave.Rock;
import com.mikm.rendering.cave.RockType;

import java.io.*;
import java.util.ArrayList;

public class Serializer {
    private static Serializer instance;
    //TODO
    private final String SAVEFILE_PATH = "testingFile";

    private final Kryo kryo;
    private Input[] input = new Input[10];
    private Output[] output = new Output[10];

    private Serializer() {
        kryo = new Kryo();
        registerKryo();
        try {
            for (int i = 0; i < 10; i++) {
                File yourFile = new File(SAVEFILE_PATH + i + ".bin");
                yourFile.createNewFile();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Serializer getInstance() {
        if (instance == null) {
            instance = new Serializer();
        }
        return instance;
    }

    public void write(Object object, int fileIndex) {
        try {
            output[fileIndex] = new Output(new FileOutputStream(SAVEFILE_PATH + fileIndex + ".bin"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        kryo.writeObject(output[fileIndex], object);
        output[fileIndex].flush();
    }

    public <T> T read(Class<T> type, int fileIndex) {

        try {
            input[fileIndex] = new Input(new FileInputStream(SAVEFILE_PATH + fileIndex + ".bin"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return kryo.readObject(input[fileIndex], type);
    }

    public <T> T copy(T o) {
        return kryo.copy(o);
    }

    public void dispose() {
        for (int i = 0; i < 10; i++) {
            if (input[i] != null) {
                input[i].close();
            }
            if (output[i] != null) {
                output[i].close();
            }

        }
    }

    private void registerKryo() {
        //Disallows multiple instances of an object
        kryo.setReferences(false);
        kryo.register(Slime.class, new EntitySerializer());
        kryo.register(Bat.class, new EntitySerializer());
        kryo.register(Rat.class, new EntitySerializer());
        kryo.register(ArrayList.class);
        kryo.register(Rock.class, new RockSerializer());
        kryo.register(RockType.class);
        kryo.register(boolean[][].class);
        kryo.register(boolean[].class);
        kryo.register(Vector2.class);
        kryo.register(Vector2Int.class);
        kryo.register(CaveFloorMemento.class, new CaveFloorMementoSerializer());
    }
}
