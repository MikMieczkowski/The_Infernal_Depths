package com.mikm.entities.actions;

import com.badlogic.gdx.math.MathUtils;
import com.mikm.ExtraMathUtils;
import com.mikm.entities.Entity;
import com.mikm.entities.player.Player;
import com.mikm.entityLoader.Blackboard;
import com.mikm.input.GameInput;
import com.mikm.rendering.screens.Application;

public class PlayerAttackingAction extends AcceleratedMoveAction {
  Player player;
  float sliceWidthMultiplier = 1;

  public PlayerAttackingAction(Entity entity) {
    super(entity);
    player = (Player) entity;
    // check for entity == player somehow?
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
    player.currentHeldItem.enterAttackState();
    player.direction = GameInput.getAttackingDirectionInt();
    MAX_TIME = player.currentHeldItem.getTotalAttackTime();
  }

  @Override
  public void update() {
    player.currentHeldItem.updateDuringAttackState();
    super.update();
    float angleToLocked = Application.player.angleTo(Application.player.lockedIn);
    float skewLerp = ExtraMathUtils.skewedSinLerp(timeElapsedInState, MAX_TIME, 3, .2f, 100);
    player.xVel = -MathUtils.cos(angleToLocked) * skewLerp;
    player.yVel = -MathUtils.sin(angleToLocked) * skewLerp;
  }

}
