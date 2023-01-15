package com.mikm.rendering.cave;

import com.mikm.ExtraMathUtils;

public class SpawnerDistribution
{
    float floor1, floor5, floor10, floor15;
    float[] distributionByFloor;

    public SpawnerDistribution(float floor1, float floor5, float floor10, float floor15) {
        //all in percentages
        this.floor1 = floor1;
        this.floor5 = floor5;
        this.floor10 = floor10;
        this.floor15 = floor15;
        float[] floorDistributionsMultiplesOf5 = new float[]{floor1, floor5, floor10, floor15};

        distributionByFloor = new float[SpawnerDistributions.LAST_LEVEL];
        floorDistributionsMultiplesOf5[0] -= .2f * (floor5-floor1);
        for (int i = 1; i < SpawnerDistributions.LAST_LEVEL; i++) {
            int multipleOf5Index = i / 5;
            float percentOfWayPastMultipleOf5 = ((i)%5)/5f;
            distributionByFloor[i-1] = ExtraMathUtils.lerp(percentOfWayPastMultipleOf5, 1, floorDistributionsMultiplesOf5[multipleOf5Index], floorDistributionsMultiplesOf5[multipleOf5Index + 1]);
        }
        distributionByFloor[0] = floor1;
        distributionByFloor[SpawnerDistributions.LAST_LEVEL-1] = floor15;
    }

    public float getDistributionByFloor(int floor) {
        return distributionByFloor[floor]/100f;
    }
}
