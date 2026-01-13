package com.mikm.serialization;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.mikm.Vector2Int;
import com.mikm.entities.prefabLoader.EntityData;
import com.mikm.rendering.cave.CaveFloorMemento;
import com.mikm.rendering.cave.RockType;

import java.util.ArrayList;

public class Serializer {
    private static Serializer instance;
    public static final String SAVEFILE_PATH = "InfernalDepthsSaveFiles/save";

    private final Kryo kryo, prefabKryo;
    private Input[] input = new Input[11];
    private Output[] output = new Output[11];

    private Serializer() {
        kryo = new Kryo();
        prefabKryo = new Kryo();
        prefabKryo.setRegistrationRequired(false);
        prefabKryo.setReferences(false);
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

    private Serializer(boolean prefabCopyOnly) {
        prefabKryo = new Kryo();
        prefabKryo.setRegistrationRequired(false);
        prefabKryo.setReferences(false);
        kryo = null;
    }

    public static Serializer getInstance() {
        if (instance == null) {
            instance = new Serializer();
        }
        return instance;
    }


    public static Serializer getInstance(boolean prefabCopyOnly) {
        if (instance == null) {
            instance = new Serializer(prefabCopyOnly);
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
        return prefabKryo.copy(o);
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

        //--caveFloorMementoSerializer--
        //TODO change after memento refactor
        kryo.register(Vector2Int.class);
        kryo.register(Vector2.class);
        kryo.register(boolean[].class);
        kryo.register(boolean[][].class);
        kryo.register(ArrayList.class);
        //entityData
        kryo.register(EntityData.class);
        kryo.register(int[].class);
        kryo.register(RockType.class);

        kryo.register(CaveFloorMemento.class, new CaveFloorMementoSerializer());
//        kryo.register(Weapon.class);
//        kryo.register(Grave.class, new GraveSerializer());
//        kryo.register(Entity.class, new EntitySerializer());
//        kryo.register(Rock.class, new RockSerializer());
//        kryo.register(CaveFloorMemento.class, new CaveFloorMementoSerializer());
    }
}
