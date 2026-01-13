package com.mikm._components;

import com.badlogic.ashley.core.Component;

public class FollowComponent implements Component {
    @Copyable public Transform target;
    @Copyable public float offsetX;
    @Copyable public float offsetY;

    public FollowComponent() {

    }

    public FollowComponent(Transform target, float offsetX, float offsetY) {
        this.target = target;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }
}