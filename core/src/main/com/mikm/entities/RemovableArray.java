package com.mikm.entities;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.mikm.DoAfterRender;

import java.util.ArrayList;
import java.util.Comparator;

public class RemovableArray<T extends InanimateEntity> extends ArrayList<T> {
    private final ArrayList<T> toAdd = new ArrayList<>();
    private final ArrayList<T> toRemove = new ArrayList<>();
    private boolean executeMethodAfterRender;
    private DoAfterRender method;

    public void draw(Batch batch) {
        for (T inanimateEntity : this) {
            inanimateEntity.draw(batch);
        }
    }

    public void render(Batch batch) {
        for (T inanimateEntity : this) {
            inanimateEntity.render(batch);
        }
        if (toAdd.size() != 0) {
            super.addAll(toAdd);
            this.sort(Comparator.comparing(InanimateEntity::getZOrder));
        }
        if (toRemove.size() != 0) {
            super.removeAll(toRemove);
        }
        if (toAdd.size() != 0) {
            toAdd.clear();
        }
        if (toRemove.size() != 0) {
            toRemove.clear();
        }
        if (executeMethodAfterRender) {
            executeMethodAfterRender = false;
            method.doAfterRender();
        }
    }

    public void addInstantly(T t) {
        super.add(t);
        this.sort(Comparator.comparing(InanimateEntity::getZOrder));
    }

    public void removeInstantly(T t) {
        super.remove(t);
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
