package com.mikm.rendering.cave;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.mikm.Vector2Int;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

class CaveFloorMementoSerializerTest {

    Kryo kryo;
    Output output;
    Input input;
    String SAVED_CAVEFLOORS_FILEPATH = "D:/IntelliJprojects/The_Infernal_Depths-master(1)/The_Infernal_Depths-master/assets/test2.bin";

    @Test
    public void testWrite() {
        Output output;
        kryo = new Kryo();
        kryo.register(Vector2Int.class);
        try {
            output = new Output(new FileOutputStream(SAVED_CAVEFLOORS_FILEPATH));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        kryo.writeObject(output, new Vector2Int(2, 3));

        try {
            output = new Output(new FileOutputStream(SAVED_CAVEFLOORS_FILEPATH));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        kryo.writeObject(output, new Vector2Int(4, 4));
        output.close();
    }


    @Test
    public void testRead() {
        Input input;
        kryo = new Kryo();
        kryo.register(Vector2Int.class);
        try {
            input = new Input(new FileInputStream(SAVED_CAVEFLOORS_FILEPATH));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        assert kryo.readObject(input, Vector2Int.class).x == 4;
        input.close();
    }
    //@Test
//    public void mementoShouldBeWrittenToFile() {
//        initializeKryo();
//        RemovableArray<InanimateEntity> inanimateEntities = new RemovableArray<>(InanimateEntity.class);
//        RemovableArray<Entity> entities = new RemovableArray<>(Entity.class);
//        CaveFloorMemento memento = new CaveFloorMemento(Vector2Int.DOWNRIGHT, new boolean[6][6], new ArrayList<>(), inanimateEntities, entities);
//        CaveFloorMemento mementoRead;
//        try {
//            kryo.writeObject(output, memento);
//            output.flush();
//            mementoRead = kryo.readObject(input, CaveFloorMemento.class);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        } finally {
//            output.close();
//            input.close();
//        }
//        assertEquals(mementoRead, memento);
//        assertEquals(mementoRead.spawnPosition, memento.spawnPosition);
//        assertTrue(Arrays.deepEquals(mementoRead.ruleCellPositions, memento.ruleCellPositions));
//        assertEquals(mementoRead.inanimateEntities, memento.inanimateEntities);
//        assertEquals(mementoRead.enemies, memento.enemies);
//    }
/*
    @Test
    public void readDifferentParameterizedClass() {
        initializeKryo();
        ArrayList<Integer> ints = new ArrayList<>();
        ArrayList<String> strings = new ArrayList<>();
        ArrayList<Integer> intsRead;
        ArrayList<String> stringsRead;
        ints.add(12);
        strings.add("string");
        strings.add("string2");
        try {
            kryo.writeObject(output, ints);
            kryo.writeObject(output, strings);
            output.close();
            System.out.println(kryo.getGenerics().nextGenericClass());
            stringsRead = (ArrayList<String>) kryo.readObject(input, ArrayList.class);
            intsRead = (ArrayList<Integer>) kryo.readObject(input, ArrayList.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            output.close();
            input.close();
        }
        assertEquals(ints, intsRead);
        assertEquals(strings, stringsRead);
    }
 */

    private void initializeKryo() {
        kryo = new Kryo();
        kryo.register(Class.class);
        kryo.register(Entity.class);
        kryo.register(InanimateEntity.class);
        kryo.register(ArrayList.class);
        kryo.register(RemovableArray.class);
        kryo.register(boolean[][].class);
        kryo.register(boolean[].class);
        kryo.register(Vector2Int.class);
        kryo.register(CaveFloorMemento.class);
        try {
            input = new Input(new FileInputStream(SAVED_CAVEFLOORS_FILEPATH));
            output = new Output(new FileOutputStream(SAVED_CAVEFLOORS_FILEPATH));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /* 
    @Test
    public void kryoShouldDeepCopy() {
        boolean[][] bools = new boolean[5][5];
        boolean[][] boolsCopy = Serializer.getInstance().copy(bools);
        assertTrue(Arrays.deepEquals(bools, boolsCopy));
        boolsCopy[3][3] = true;
        assertFalse(bools[3][3]);
    }
        */
/*
    @Test
    public void ruleCellPositionsShouldBeDeepCopied() {
        RemovableArray<InanimateEntity> inanimateEntities = new RemovableArray<>(InanimateEntity.class);
        RemovableArray<Entity> entities = new RemovableArray<>(Entity.class);
        boolean[][] bools = new boolean[6][6];
        CaveFloorMemento memento = new CaveFloorMemento(Vector2Int.DOWNRIGHT, bools, new ArrayList<>(), inanimateEntities, entities);
        bools[3][3] = true;
        assertFalse(memento.ruleCellPositions[3][3]);
    }

    @Test
    public void inanimateEntitiesShouldBeDeepCopied() {
        RemovableArray<InanimateEntity> inanimateEntities = new RemovableArray<>(InanimateEntity.class);
        RemovableArray<Entity> entities = new RemovableArray<>(Entity.class);
        boolean[][] bools = new boolean[6][6];
        CaveFloorMemento memento = new CaveFloorMemento(Vector2Int.DOWNRIGHT, bools, new ArrayList<>(), inanimateEntities, entities);
        inanimateEntities.add(new TestObject());
        assertEquals(0, memento.inanimateEntities.size());
    }


    @Test
    public void vector2IntShouldBeDeepCopied() {
        RemovableArray<InanimateEntity> inanimateEntities = new RemovableArray<>(InanimateEntity.class);
        RemovableArray<Entity> entities = new RemovableArray<>(Entity.class);
        boolean[][] bools = new boolean[6][6];
        Vector2Int vector2Int = new Vector2Int(34, 45);
        CaveFloorMemento memento = new CaveFloorMemento(vector2Int, bools, new ArrayList<>(), inanimateEntities, entities);
        vector2Int = new Vector2Int(102498, 12039);
        assertNotEquals(memento.spawnPosition, vector2Int);
    }
    */

}
