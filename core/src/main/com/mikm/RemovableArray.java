package com.mikm;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.mikm.entities.InanimateEntity;

import java.util.ArrayList;

public class RemovableArray<T extends InanimateEntity> extends ArrayList<T> {
    private final ArrayList<T> toAdd = new ArrayList<>();
    private final ArrayList<T> toRemove = new ArrayList<>();

    public void render(Batch batch) {
        for (T inanimate : this) {
            inanimate.render(batch);
        }
        super.addAll(toAdd);
        super.removeAll(toRemove);
        if (toAdd.size() != 0) {
            toAdd.clear();
        }
        if (toRemove.size() != 0) {
            toRemove.clear();
        }
    }

    @Override
    public boolean add(T t) {
        return toAdd.add(t);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object o) {
        return toRemove.add((T)o);
    }
}
