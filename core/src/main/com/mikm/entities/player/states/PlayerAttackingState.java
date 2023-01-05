package com.mikm.entities.player.states;

import com.badlogic.gdx.Gdx;
import com.mikm.entities.animation.AnimationManager;
import com.mikm.entities.animation.DirectionalAnimationSet;
import com.mikm.entities.player.Player;
import com.mikm.entities.State;
import com.mikm.input.InputAxis;

public class PlayerAttackingState extends State {
    Player player;
    private float attackTimer;

    public PlayerAttackingState(Player player) {
        super(player);
        this.player = player;
        DirectionalAnimationSet directionalAnimationSet = player.walkingState.directionalAnimationSet;
        animationManager = new AnimationManager(player, directionalAnimationSet);
    }

    @Override
    public void enter() {
        super.enter();
        player.currentWeapon.enterAttackState();
        attackTimer = 0;
        player.direction = InputAxis.getAttackingDirectionInt();
        super.update();
    }

    @Override
    public void update() {
        player.currentWeapon.attackUpdate();
        player.currentWeapon.checkForHit();
        attackTimer += Gdx.graphics.getDeltaTime();
        player.xVel = InputAxis.getHorizontalAxis() * player.speed;
        player.yVel = InputAxis.getVerticalAxis() * player.speed;
    }

    @Override
    public void checkForStateTransition() {
        if (attackTimer > player.currentWeapon.getTotalAttackTime()) {
            player.currentWeapon.exitAttackState();
            player.walkingState.enter();
        }
    }
}
