package com.mikm.entities.actions;

import com.badlogic.ashley.core.Entity;
import com.mikm.entities.prefabLoader.PrefabInstantiator;
import com.mikm.rendering.screens.Application;

public class PlayerChargingAction extends AcceleratedMoveAction {
    @Override
    public void onExit(Entity entity) {
        System.out.println("CALLED");
        super.onExit(entity);
        PrefabInstantiator.addProjectile(Application.getInstance().getPlayerX(), Application.getInstance().getPlayerY());
    }
}
