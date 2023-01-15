package com.mikm;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.mikm.entities.InanimateEntity;

import java.util.ArrayList;

public class RemovableArray<T extends InanimateEntity> extends ArrayList<T> {
    private final ArrayList<T> toAdd = new ArrayList<>();
    private final ArrayList<T> toRemove = new ArrayList<>();
    private boolean executeMethodAfterRender;
    private DoAfterRender method;

    public void render(Batch batch) {
        for (T inanimateEntity : this) {
            inanimateEntity.render(batch);
        }
        super.addAll(toAdd);
        super.removeAll(toRemove);
        if (executeMethodAfterRender) {
            executeMethodAfterRender = false;
            method.doAfterRender();
        }
        if (toAdd.size() != 0) {
            toAdd.clear();
        }
        if (toRemove.size() != 0) {
            toRemove.clear();
        }
    }

    public boolean addInstantly(T t) {
        return super.add(t);
    }

    public boolean removeInstantly(T t) {
        return super.remove(t);
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

    public void doAfterRender(DoAfterRender method) {
        this.method = method;
        executeMethodAfterRender = true;
    }
}
