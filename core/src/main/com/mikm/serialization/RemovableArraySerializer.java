//package com.mikm.serialization;
//
//import com.esotericsoftware.kryo.Kryo;
//import com.esotericsoftware.kryo.Serializer;
//import com.esotericsoftware.kryo.io.Input;
//import com.esotericsoftware.kryo.io.Output;
//import com.mikm.entities.InanimateEntity;
//import com.mikm.entities.RemovableArray;
//
//public class RemovableArraySerializer<T extends InanimateEntity> extends Serializer<RemovableArray<T>> {
//
//    @Override
//    public void write(Kryo kryo, Output output, RemovableArray<T> object) {
//        // write list size to output
//        output.writeInt(object.size());
//
//        // write each list element to output
//        for (T element : object) {
//            kryo.writeObject(output, element);
//        }
//    }
//
//    @Override
//    public RemovableArray<T> read(Kryo kryo, Input input, Class<? extends RemovableArray<T>> type) {
//        // read list size from input
//        int size = input.readInt();
//
//        // create new list instance
//        RemovableArray<T> list = new RemovableArray<>(null);
//
//        // read each list element from input and add it to list
//        for (int i = 0; i < size; i++) {
//            T element = (T)kryo.readClassAndObject(input);
//            list.add(element);
//        }
//
//        return list;
//    }
//
//
//    @Override
//    public RemovableArray<T> copy(Kryo kryo, RemovableArray<T> original) {
//        // create a deep copy of the list
//        RemovableArray<T> copy = new RemovableArray<>(null);
//        for (T element : original) {
//            copy.add(kryo.copy(element));
//        }
//        return copy;
//    }
//}
