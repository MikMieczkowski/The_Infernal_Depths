package com.mikm.rendering.cave;

import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.CaveScreen;

public class SpawnProbabilityConstants {
    public static final SpawnProbability ROCK_FILL = new SpawnProbability(6, 9, 12, 15);

    public static final SpawnProbability NORMAL_CHANCE = new SpawnProbability(92, 76.59f, 54.5f, 27.5f);
    public static final SpawnProbability COPPER_CHANCE = new SpawnProbability(7.5f, 20, 5, .5f);
    public static final SpawnProbability IRON_CHANCE = new SpawnProbability(.5f, 3, 20, 10);
    public static final SpawnProbability CRYSTAL_CHANCE = new SpawnProbability(0, .4f, 20, 30);
    public static final SpawnProbability INFERNAL_CHANCE = new SpawnProbability(0, .01f, 1.5f, 30);
    public static final SpawnProbability GEM_CHANCE = new SpawnProbability(0, 0, 0, 0);

    private static final float mapScaleConstant = (float) (Application.getInstance().caveScreen.getMapWidth() * Application.getInstance().caveScreen.getMapHeight()) / 6400;

    //percentages are from 0 to 100 for some reason
    public static final SpawnProbability ENEMY_AMOUNT = new SpawnProbability(
            mapScaleConstant*300, mapScaleConstant*1700, mapScaleConstant*3000, mapScaleConstant*0);

    public static float[] getOreDistributionsByFloor(int floor) {
        return new float[]{
                NORMAL_CHANCE.getProbabilityByFloor(floor),
                COPPER_CHANCE.getProbabilityByFloor(floor),
                IRON_CHANCE.getProbabilityByFloor(floor),
                CRYSTAL_CHANCE.getProbabilityByFloor(floor),
                INFERNAL_CHANCE.getProbabilityByFloor(floor),
                GEM_CHANCE.getProbabilityByFloor(floor)
        };
    }
}
