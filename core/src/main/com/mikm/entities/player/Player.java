//player

// die() : TODO graves
//if (RockType.playerHasAnyTempOre()) {
//      Application.getInstance().currentScreen
//          .addInanimateEntity(new Grave(16 * (int) Application.playerOLD.x / 16, 16 * (int) Application.playerOLD.y / 16));
//    }
//  public final float RESPAWN_TIME = 3;

// TODO weapons
//  private void handleInput() {
//    if (GameInput.isSwitchButtonJustPressed()) {
//      if (holdingPickaxe) {
//        currentHeldItem = equippedWeapon;
//      } else {
//        currentHeldItem.exitAttackState();
//        currentHeldItem = weaponInstances.pickaxe;
//      }
//      holdingPickaxe = !holdingPickaxe;
//    }
//    if (GameInput.isPickaxeButtonJustPressed()) {
//      currentHeldItem.exitAttackState();
//      currentHeldItem = weaponInstances.pickaxe;
//      holdingPickaxe = true;
//    }
//    if (GameInput.isWeaponButtonJustPressed()) {
//      currentHeldItem = equippedWeapon;
//      holdingPickaxe = false;
//    }

//TODO weapons
//  @Override
//  public void draw() {
//    drawPlayerAndWeaponBasedOnZIndex();
//  }
//
//  private void drawPlayerAndWeaponBasedOnZIndex() {
//    if (currentHeldItem.zIndex == 0) {
//      currentHeldItem.draw();
//      drawPlayer();
//      return;
//    }
//    drawPlayer();
//    currentHeldItem.draw();
//  }
//
//  private void drawPlayer() {
//    effectsHandler.handleFlash();
//    if (effectsHandler.inInvincibility) {
//      Application.batch.setColor(new Color(1, 1, 1, .5f));
//    }
//    animationHandler.draw();
//    Application.batch.setColor(Color.WHITE);
//  }
