package com.mikm.entities.enemies.slimeBoss;

import com.badlogic.gdx.math.MathUtils;
import com.mikm.entities.State;
import com.mikm.entities.enemies.Slime;
import com.mikm.entities.player.Player;
import com.mikm.rendering.screens.Application;

public class SB_SplitAttack extends State {
    private SlimeBoss slimeBoss;
    private Player player;
    private final float ANGLE_BETWEEN_SLIMES = 30 * MathUtils.degRad;
    private Slime[] spawnedSlimes = new Slime[5];

    public SB_SplitAttack(SlimeBoss slimeBoss) {
        super(slimeBoss);
        this.slimeBoss = slimeBoss;
        this.player = Application.player;
        animationManager = slimeBoss.jumpState.animationManager;
    }

    @Override
    public void enter() {
        super.enter();
        slimeBoss.visible = false;
        slimeBoss.isAttackable = false;
        float angleToPlayer = MathUtils.atan2(-player.y + slimeBoss.y, -player.x + slimeBoss.x);
        for (int i = -2; i <= 2; i++) {
            spawnedSlimes[i+2] = new Slime(slimeBoss.x, slimeBoss.y, angleToPlayer + ANGLE_BETWEEN_SLIMES * i);
            slimeBoss.screen.addEntity(spawnedSlimes[i+2]);
        }
    }


    @Override
    public void checkForStateTransition() {
        if (allSpawnedSlimesAreDead()) {
            slimeBoss.visible = true;
            slimeBoss.isAttackable = true;
            slimeBoss.stateManager.updateState();
        }
    }

    private boolean allSpawnedSlimesAreDead() {
        for (Slime spawnedSlime : spawnedSlimes) {
            if (!spawnedSlime.damagedState.dead) {
                return false;
            }
        }
        return true;
    }

}