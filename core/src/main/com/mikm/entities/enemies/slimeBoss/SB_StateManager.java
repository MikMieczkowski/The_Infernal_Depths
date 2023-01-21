package com.mikm.entities.enemies.slimeBoss;

import com.badlogic.gdx.math.Vector2;
import com.mikm.ExtraMathUtils;

public class SB_StateManager {
    private boolean dashing;
    private int phase = 0;
    private SlimeBoss slimeBoss;
    private int attacksCompleted, dashesCompleted;
    private Vector2 dashOriginalPosition = new Vector2();
    private boolean firstDash = true;

    private int SIMMER_CHANCE_PERCENT = 25;
    private int LONG_JUMP_CHANCE_PERCENT = 50;
    private int jumpsPerDash = 5;


    public SB_StateManager(SlimeBoss slimeBoss) {
        this.slimeBoss = slimeBoss;
    }

    public void updateState() {
        if (dashing) {
            updateDashState();
        } else {
            updateAttackByPhase();
        }
    }

    private void updateDashState() {
        dashesCompleted++;
        if (dashesCompleted > jumpsPerDash) {
            dashing = false;
            dashesCompleted = 0;
        }
        if (dashesCompleted == 1) {
            dashOriginalPosition = new Vector2(slimeBoss.x, slimeBoss.y);
            firstDash = true;
        }
        enterAttack(SlimeBossAttackType.DASH);
    }

    private void updateAttackByPhase() {
        if (phase == 0) {
            updateStartingPhase();
        } else if (phase == 1){
            updatePhase1();
        } else {
            enterAttack(SlimeBossAttackType.SPLIT);
        }
    }

    private void updateStartingPhase() {
        if (attacksCompleted < 3) {
            enterAttack(SlimeBossAttackType.SHORTJUMP);
        } else if (attacksCompleted < 5) {
            enterAttack(SlimeBossAttackType.LONGJUMP);
        } else if (attacksCompleted == 5) {
            phase = 1;
            enterAttack(SlimeBossAttackType.SIMMER);
        }
        attacksCompleted++;
    }

    private void updatePhase1() {
        if (slimeBoss.hp < slimeBoss.getMaxHp() /2f) {
            phase = 2;
        }
        int randomAttackPercent = ExtraMathUtils.randomInt(100);
        if (randomAttackPercent < SIMMER_CHANCE_PERCENT) {
            enterAttack(SlimeBossAttackType.SIMMER);
        } else if (randomAttackPercent < SIMMER_CHANCE_PERCENT+LONG_JUMP_CHANCE_PERCENT) {
            enterAttack(SlimeBossAttackType.LONGJUMP);
        } else {
            enterAttack(SlimeBossAttackType.SHORTJUMP);
        }
    }


    private void enterAttack(SlimeBossAttackType attackType) {
        switch (attackType) {
            case SHORTJUMP:
                slimeBoss.jumpBuildUpState.enter(true);
                break;
            case LONGJUMP:
                slimeBoss.jumpBuildUpState.enter(false);
                if (ExtraMathUtils.randomInt(100) < 50) {
                    dashing = true;
                }
                break;
            case SIMMER:
                slimeBoss.simmerBuildUpState.enter();
                break;
            case DASH:
                if (firstDash) {
                    slimeBoss.dashBuildUpState.enter(dashOriginalPosition);
                }  else {
                    slimeBoss.dashState.enter(dashOriginalPosition, false);
                }
                firstDash = false;
                break;
            case SPLIT:
                slimeBoss.splitState.enter();
        }
    }
}
