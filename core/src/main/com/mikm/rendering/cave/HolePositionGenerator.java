package com.mikm.rendering.cave;

import com.badlogic.gdx.math.MathUtils;
import com.mikm.RandomUtils;
import com.mikm.Vector2Int;

import java.util.*;

public class HolePositionGenerator {
    private CaveTilemapCreator caveTilemap;
    private ArrayList<Vector2Int> openTiles;
    private Set<Vector2Int> output;

    HolePositionGenerator(CaveTilemapCreator caveTilemap) {
        this.caveTilemap = caveTilemap;
        openTiles = caveTilemap.openTiles;
    }

    ArrayList<Vector2Int> createHolePositions() {
        output = new HashSet<>();
        generateLines();
        deleteOverlappingHoles();
        deleteIfSmall();
        openTiles.removeAll(output);
        return new ArrayList<>(output);
    }

    private void generateLines() {
        ArrayList<Vector2Int> shuffledOpenTiles = new ArrayList<>(openTiles);
        Collections.shuffle(shuffledOpenTiles);
        if (shuffledOpenTiles.isEmpty()) {
            return;
        }
        for (int i = 0; i < CaveTilemapCreator.CHASM_AMOUNT; i++) {
            Vector2Int randomizedNearbyPosition = new Vector2Int(shuffledOpenTiles.get(i));
            int forks = 0;
            while (RandomUtils.getPercentage(70)) {
                randomizedNearbyPosition = new Vector2Int(randomizedNearbyPosition.x + RandomUtils.getInt(-2, 2), randomizedNearbyPosition.y + RandomUtils.getInt(-2, 2));
                addLineAt(randomizedNearbyPosition);
                forks++;
                if (forks > 20) {
                    break;
                }
            }
            //We made forks amount of new holes
            i += forks;
        }
    }

    private void addLineAt(Vector2Int tile) {
        ArrayList<Vector2Int> line = TileGenerationUtils.getLinePositions(tile, RandomUtils.getFloat(0, MathUtils.PI2),
                RandomUtils.getInt(CaveTilemapCreator.CHASM_LENGTH_MIN, CaveTilemapCreator.CHASM_LENGTH_MAX), CaveTilemapCreator.CHASM_WIDTH);
        output.addAll(line);
    }

    private void deleteOverlappingHoles() {
        output.removeIf(holePosition -> !openTiles.contains(holePosition));
    }

    private void deleteIfSmall() {
        ArrayList<ArrayList<Vector2Int>> regions = TileGenerationUtils.getRegions(output,true);
        for (Collection<Vector2Int> region : regions) {
            if (region.size() < CaveTilemapCreator.CHASM_MIN_SIZE) {
                output.removeAll(region);
            }
        }
    }
}
