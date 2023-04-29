package com.mikm.rendering.cave;

import com.mikm.rendering.screens.CaveScreen;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpawnProbabilityTest {

    @Test
    void probabilitiesPerFloorShouldReturnEvenlySpacedNumbers() {
        SpawnProbability spawnProbability = new SpawnProbability(0, 100, 200, 300);
        for (int i = 0; i < CaveScreen.LAST_FLOOR; i++) {
            System.out.println(spawnProbability.getProbabilityByFloor(i));
        }
    }

    @Test
    void probabiltiesShouldEndAfterLastFloor() {
        SpawnProbability spawnProbability = new SpawnProbability(0, 100, 200, 300);
        assertThrows(Exception.class, () -> spawnProbability.getProbabilityByFloor(CaveScreen.LAST_FLOOR));
    }
}