package com.mikm.entities.player.states;

import com.badlogic.gdx.Gdx;
import com.mikm.entities.animation.AnimationManager;
import com.mikm.entities.animation.ActionAnimationAllDirections;
import com.mikm.entities.player.Player;
import com.mikm.entities.State;
import com.mikm.input.GameInput;

public class PlayerAttackingState extends State {
    Player player;
    private float attackTimer;

    public PlayerAttackingState(Player player) {
        super(player);
        this.player = player;
        ActionAnimationAllDirections actionAnimationAllDirections = player.walkingState.actionAnimationAllDirections;
        animationManager = new AnimationManager(player, actionAnimationAllDirections);
    }

    @Override
    public void enter() {
        super.enter();
        player.currentWeapon.enterAttackState();
        attackTimer = 0;
        player.direction = GameInput.getAttackingDirectionInt();
        super.update();
    }

    @Override
    public void update() {
        player.currentWeapon.attackUpdate();
        attackTimer += Gdx.graphics.getDeltaTime();
        player.xVel = GameInput.getHorizontalAxis() * player.speed;
        player.yVel = GameInput.getVerticalAxis() * player.speed;
    }

    @Override
    public void checkForStateTransition() {
        if (attackTimer > player.currentWeapon.getTotalAttackTime()) {
            player.currentWeapon.exitAttackState();
            player.walkingState.enter();
        }
    }
}
