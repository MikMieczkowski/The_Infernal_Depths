package com.mikm.rendering.cave;

public class SpawnProbabilityConstants {
    public static final SpawnProbability ROCK_FILL = new SpawnProbability(6, 9, 12, 15);

    public static final SpawnProbability NORMAL_CHANCE = new SpawnProbability(92, 76.69f, 54.8f, 27.5f);
    public static final SpawnProbability COPPER_CHANCE = new SpawnProbability(7.5f, 20, 5, .5f);
    public static final SpawnProbability IRON_CHANCE = new SpawnProbability(.5f, 3, 20, 10);
    public static final SpawnProbability CRYSTAL_CHANCE = new SpawnProbability(0, .3f, 20, 30);
    public static final SpawnProbability INFERNAL_CHANCE = new SpawnProbability(0, .01f, .1f, 30);
    public static final SpawnProbability GEM_CHANCE = new SpawnProbability(0, 0, .1f, 2);

    public static final SpawnProbability ENEMY_AMOUNT = new SpawnProbability(300, 3000, 5000, 0);
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
