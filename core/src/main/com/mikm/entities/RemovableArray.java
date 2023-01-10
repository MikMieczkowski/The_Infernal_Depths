package com.mikm.entities;

import com.badlogic.gdx.graphics.g2d.Batch;

import java.util.ArrayList;

public class RemovableArray<T extends InanimateEntity> extends ArrayList<T> {
    private final ArrayList<T> toRemove = new ArrayList<>();
    public void render(Batch batch) {
        for (T inanimate : this) {
            inanimate.render(batch);
        }
        for (T inanimate : toRemove) {
            super.remove(inanimate);
        }
        if (toRemove.size() != 0) {
            toRemove.clear();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object o) {
        toRemove.add((T)o);
        return false;
    }
}
