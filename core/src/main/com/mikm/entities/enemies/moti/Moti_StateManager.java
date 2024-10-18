package com.mikm.entities.enemies.moti;

import com.badlogic.gdx.math.Vector2;
import com.mikm.RandomUtils;

public class Moti_StateManager {
    private boolean dashing;
    private int phase = 0;
    private Moti moti;
    private int attacksCompleted, dashesCompleted;
    private Vector2 dashOriginalPosition = new Vector2();
    private boolean firstDash = true;

    private int SIMMER_CHANCE_PERCENT = 25;
    private int LONG_JUMP_CHANCE_PERCENT = 50;
    private int jumpsPerDash = 5;


    public Moti_StateManager(Moti moti) {
        this.moti = moti;
    }

    public void updateState() {
        if (!moti.damagedState.dead) {
            if (dashing) {
                updateDashState();
            } else {
                updateAttackByPhase();
            }
        }
    }

    private void updateDashState() {
        dashesCompleted++;
        if (dashesCompleted > jumpsPerDash) {
            dashing = false;
            dashesCompleted = 0;
        }
        if (dashesCompleted == 1) {
            dashOriginalPosition = new Vector2(moti.x, moti.y);
            firstDash = true;
        }
        //enterAttack(SlimeBossAttackType.DASH);
    }

    private void updateAttackByPhase() {
        if (phase == 0) {
            updateStartingPhase();
        } else if (phase == 1){
            updatePhase1();
        } else {
            //no phase 2 in the game
            updatePhase1();
        }
    }

    private void updateStartingPhase() {
        if (attacksCompleted < 3) {
            //enterAttack(SlimeBossAttackType.SHORTJUMP);
        } else if (attacksCompleted < 5) {
            //enterAttack(SlimeBossAttackType.LONGJUMP);
        } else if (attacksCompleted == 5) {
            phase = 1;
            //enterAttack(SlimeBossAttackType.SIMMER);
        }
        attacksCompleted++;
    }

    private void updatePhase1() {
        //if (slimeBoss.hp < slimeBoss.getMaxHp() /2f) {
        //    phase = 2;
        //}
        int randomAttackPercent = RandomUtils.getInt(100);
        if (randomAttackPercent < SIMMER_CHANCE_PERCENT) {
        //    enterAttack(SlimeBossAttackType.SIMMER);
        } else if (randomAttackPercent < SIMMER_CHANCE_PERCENT+LONG_JUMP_CHANCE_PERCENT) {
        //    enterAttack(SlimeBossAttackType.LONGJUMP);
        } else {
        //    enterAttack(SlimeBossAttackType.SHORTJUMP);
        }
    }


    private void enterAttack(Moti attackType) {
        /*
        switch (attackType) {
            case SHORTJUMP:
                slimeBoss.jumpBuildUpState.enter(true);
                break;
            case LONGJUMP:
                slimeBoss.jumpBuildUpState.enter(false);
                if (RandomUtils.getPercentage(50)) {
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
        }

         */
    }
}
