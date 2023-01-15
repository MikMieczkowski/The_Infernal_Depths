package com.mikm.rendering.cave;

public class SpawnerDistributions {
    public static final int LAST_LEVEL = 15;
    public static final SpawnerDistribution ROCK_FILL = new SpawnerDistribution(6, 9, 12, 15);

    public static final SpawnerDistribution NORMAL_CHANCE = new SpawnerDistribution(95, 76.69f, 54.8f, 27.5f);
    public static final SpawnerDistribution COPPER_CHANCE = new SpawnerDistribution(4.5f, 20, 5, .5f);
    public static final SpawnerDistribution IRON_CHANCE = new SpawnerDistribution(.5f, 3, 20, 10);
    public static final SpawnerDistribution CRYSTAL_CHANCE = new SpawnerDistribution(0, .3f, 20, 30);
    public static final SpawnerDistribution INFERNAL_CHANCE = new SpawnerDistribution(0, 0, .1f, 30);
    public static final SpawnerDistribution GEM_CHANCE = new SpawnerDistribution(0, .01f, .1f, 2);

    public static float[] getOreDistributionsByFloor(int floor) {
        return new float[]{
                NORMAL_CHANCE.getDistributionByFloor(floor),
                COPPER_CHANCE.getDistributionByFloor(floor),
                IRON_CHANCE.getDistributionByFloor(floor),
                CRYSTAL_CHANCE.getDistributionByFloor(floor),
                INFERNAL_CHANCE.getDistributionByFloor(floor),
                GEM_CHANCE.getDistributionByFloor(floor)
        };
    }
}
