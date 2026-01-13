package com.mikm.rendering.cave;

import com.mikm.utils.ExtraMathUtils;
import com.mikm.rendering.screens.CaveScreen;

public class SpawnProbability
{
    float level1ProbabilityPercent, level2ProbabilityPercent, level3ProbabiltyPercent, level4ProbabilityPercent;
    float[] probabilitiesByFloor;

    public SpawnProbability() {
        
    }

    public SpawnProbability(float level1ProbabilityPercent, float level2ProbabilityPercent, float level3ProbabiltyPercent, float level4ProbabilityPercent) {
        this.level1ProbabilityPercent = level1ProbabilityPercent;
        this.level2ProbabilityPercent = level2ProbabilityPercent;
        this.level3ProbabiltyPercent = level3ProbabiltyPercent;
        this.level4ProbabilityPercent = level4ProbabilityPercent;
        float[] firstFloorOfLevelProbabilities = new float[]{ level1ProbabilityPercent, level2ProbabilityPercent, level3ProbabiltyPercent, level4ProbabilityPercent};

        createProbabilitiesByFloor(firstFloorOfLevelProbabilities);
    }

    //This method fills in the probabilities for each floor given the probabilties for the first floor of each level.
    private void createProbabilitiesByFloor(float[] probabilitiesByLevel) {
        probabilitiesByFloor = new float[CaveScreen.LAST_FLOOR];
        for (int floorNumber = 1; floorNumber <= CaveScreen.LAST_FLOOR; floorNumber++) {
            int level = floorNumber / CaveScreen.FLOORS_PER_LEVEL;
            float progressThroughLevel = getProgressThroughLevel(floorNumber, level);

            int zeroBasedIndex = floorNumber - 1;
            int nextLevel = level + 1;
            probabilitiesByFloor[zeroBasedIndex] = ExtraMathUtils.lerp(progressThroughLevel, 1, probabilitiesByLevel[level], probabilitiesByLevel[nextLevel]);
        }
    }

    private float getProgressThroughLevel(int i, int level) {
        float progressThroughLevel;
        if (level == 0) {
            //Because the floors don't start from 0, there are only 4 floors from 1-4.
            progressThroughLevel = (i-1) / (float)(CaveScreen.FLOORS_PER_LEVEL - 1);
        } else {
            progressThroughLevel = i % 5 / (float)CaveScreen.FLOORS_PER_LEVEL;
        }
        return progressThroughLevel;
    }

    public float getProbabilityByFloor(int floor) {
        return probabilitiesByFloor[floor]/100f;
    }
}
