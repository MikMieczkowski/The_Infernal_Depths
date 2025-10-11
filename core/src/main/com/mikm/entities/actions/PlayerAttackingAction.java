package com.mikm.entities.actions;

import com.mikm.entities.Entity;
import com.mikm.entities.player.Player;
import com.mikm.entityLoader.Blackboard;
import com.mikm.input.GameInput;
import com.mikm.rendering.sound.SequentialSound;
import com.mikm.rendering.sound.SoundEffects;

public class PlayerAttackingAction extends AcceleratedMoveAction {
    Player player;
    public PlayerAttackingAction(Entity entity) {
        super(entity);
        player = (Player) entity;
        //check for entity == player somehow?
    }

    @Override
    public void postConfigRead() {
        super.postConfigRead();
    }

    @Override
    public void onExit() {
        player.currentHeldItem.exitAttackState();
        super.onExit();
    }

    @Override
    public void enter() {
        super.enter();
        Blackboard.getInstance().bind("idleTimer", entity, 0);
        System.out.println(Blackboard.getInstance().getVar(entity, "idleTimer"));
        player.currentHeldItem.enterAttackState();
        player.direction = GameInput.getAttackingDirectionInt();
        MAX_TIME = player.currentHeldItem.getTotalAttackTime();
    }

    @Override
    public void update() {
        player.currentHeldItem.updateDuringAttackState();
        super.update();
    }


}
