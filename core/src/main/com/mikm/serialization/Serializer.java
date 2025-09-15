package com.mikm.serialization;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.mikm.Vector2Int;
import com.mikm.entities.inanimateEntities.Grave;
import com.mikm.entities.enemies.Bat;
import com.mikm.entities.enemies.Rat;
import com.mikm.entities.enemies.Slime;
import com.mikm.entities.player.weapons.Weapon;
import com.mikm.rendering.cave.CaveFloorMemento;
import com.mikm.rendering.cave.Rock;
import com.mikm.rendering.cave.RockType;

import java.util.ArrayList;

public class Serializer {
    private static Serializer instance;
    public static final String SAVEFILE_PATH = "InfernalDepthsSaveFiles/save";

    private final Kryo kryo;
    private Input[] input = new Input[11];
    private Output[] output = new Output[11];

    private Serializer() {
        kryo = new Kryo();
        registerKryo();
        try {
            FileHandle f = Gdx.files.local("InfernalDepthsSaveFiles");
            if (!f.exists()) {
                f.mkdirs();
            }
            for (int i = 0; i < 11; i++) {
                f = Gdx.files.local(SAVEFILE_PATH + i + ".bin");
                //Create file if it doesn't exist
                if (!f.exists()) {
                    f.writeString("", false);
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public static Serializer getInstance() {
        if (instance == null) {
            instance = new Serializer();
        }
        return instance;
    }

    public boolean saveFilesExist() {
        FileHandle f = Gdx.files.local("InfernalDepthsSaveFiles");
        if (!f.exists()) {
            return false;
        }
        for (int i = 0; i <= 10; i++) {
            f = Gdx.files.local(Serializer.SAVEFILE_PATH + i + ".bin");
            if (f.exists() && f.length() > 0) return true;
        }
        return false;
    }
    
    public void resetSaveFiles() {
        try {
            FileHandle f = Gdx.files.local("InfernalDepthsSaveFiles");
            if (!f.exists()) {
                throw new RuntimeException("InfernalDepthsSaveFiles should exist");
            }
            for (int i = 0; i < 11; i++) {
                f = Gdx.files.local(SAVEFILE_PATH + i + ".bin");
                //Create file if it doesn't exist
                if (!f.exists()) {
                    throw new RuntimeException(SAVEFILE_PATH + i + " should exist");
                }
                f.writeString("", false);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public void write(Object object, int fileIndex) {
        try {
            output[fileIndex] = getOutput(SAVEFILE_PATH + fileIndex + ".bin");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        kryo.writeObject(output[fileIndex], object);
        output[fileIndex].flush();
    }

    public <T> T read(Class<T> type, int fileIndex) {
        try {
            input[fileIndex] = getInput(SAVEFILE_PATH + fileIndex + ".bin");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return kryo.readObject(input[fileIndex], type);
    }

    private Input getInput(String filepath) {
        return new Input(Gdx.files.local(filepath).read());
    }

    private Output getOutput(String filepath) {
        return new Output(Gdx.files.local(filepath).write(false));
    }

    public <T> T copy(T o) {
        return kryo.copy(o);
    }

    public void dispose() {
        for (int i = 0; i < 11; i++) {
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
        kryo.register(Weapon.class);
        kryo.register(Grave.class, new GraveSerializer());
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
