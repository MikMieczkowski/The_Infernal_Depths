package com.mikm.rendering;

import com.badlogic.gdx.math.MathUtils;
import com.mikm.ExtraMathUtils;
import com.mikm.TileGenerationUtils;
import com.mikm.Vector2Int;
import com.mikm.rendering.cave.CaveTilemap;

import java.util.ArrayList;
import java.util.Collections;

public class HolePositionGenerator {
    private CaveTilemap caveTilemap;

    private ArrayList<Vector2Int> holePositions;
    private ArrayList<Vector2Int> openTiles;
    private ArrayList<Vector2Int> shuffledOpenTiles;

    private boolean[][] ruleCellPositions;
    private boolean[][] nextRuleCellPositions;
    public HolePositionGenerator(CaveTilemap caveTilemap) {
        this.caveTilemap = caveTilemap;
        openTiles = caveTilemap.openTiles;
        ruleCellPositions = caveTilemap.ruleCellPositions;
        nextRuleCellPositions = caveTilemap.nextRuleCellPositions;
    }

    public ArrayList<Vector2Int> createHolePositions() {
        holePositions = new ArrayList<>();
        shuffledOpenTiles = new ArrayList<>(caveTilemap.openTiles);
        Collections.shuffle(shuffledOpenTiles);
        for (int i = 0; i < CaveTilemap.HOLE_AMOUNT; i++) {
            createHole();
        }

        for (int y = 0; y < CaveTilemap.MAP_HEIGHT; y++) {
            for (int x = 0; x < CaveTilemap.MAP_WIDTH; x++) {
                if (!(!ruleCellPositions[y][x] && !nextRuleCellPositions[y][x])) {
                    holePositions.remove(new Vector2Int(x, y));
                }
            }
        }

        deleteSmallHoles();
        return holePositions;
    }

    private void deleteSmallHoles() {
        boolean[][] holePositionsArray = new boolean[CaveTilemap.MAP_HEIGHT][CaveTilemap.MAP_WIDTH];
        for (Vector2Int openTile: holePositions) {
            holePositionsArray[openTile.y][openTile.x] = true;
        }
        ArrayList<ArrayList<Vector2Int>> regions = TileGenerationUtils.getRegions(true, holePositionsArray);
        for(ArrayList<Vector2Int> region : regions) {
            if (region.size() < 5) {
                for (Vector2Int point : region) {
                    holePositions.remove(point);
                    openTiles.add(point);
                    shuffledOpenTiles.add(point);
                }
            }
        }
    }

    private void createHole()  {
        float randomAngle = ExtraMathUtils.randomFloat(0, MathUtils.PI2);
        Vector2Int startingPosition = getNewHoleStartingPosition();

        ArrayList<Vector2Int> line = createLine(randomAngle, startingPosition,CaveTilemap.HOLE_WIDTH);
        ArrayList<Vector2Int> lineWide = createLine(randomAngle, startingPosition, CaveTilemap.HOLE_WIDTH+1);

        if (removeOverlappingPointsAndCheckIfLargeEnough(line)) {
            placeLine(line, lineWide);
        } else {
            ArrayList<Vector2Int> oppositeLine = createLine(randomAngle - MathUtils.PI, startingPosition, CaveTilemap.HOLE_WIDTH);
            ArrayList<Vector2Int> oppositeLineWide = createLine(randomAngle - MathUtils.PI, startingPosition, CaveTilemap.HOLE_WIDTH+1);
            if (removeOverlappingPointsAndCheckIfLargeEnough(oppositeLine)) {
                placeLine(oppositeLine, oppositeLineWide);
            } else {
                shuffledOpenTiles.remove(startingPosition);
                createHole();
            }
        }
    }

    private Vector2Int getNewHoleStartingPosition() {
        if (shuffledOpenTiles.size() == 0) {
            throw new RuntimeException("Map is too small to find area to place holes.");
        }
        return shuffledOpenTiles.remove(0);
    }

    private void placeLine(ArrayList<Vector2Int> line, ArrayList<Vector2Int> widerLine) {
        for (Vector2Int point: line) {
            holePositions.add(point);
            openTiles.remove(point);
        }
        for (Vector2Int point: widerLine) {
            shuffledOpenTiles.remove(point);
        }
    }

    private boolean removeOverlappingPointsAndCheckIfLargeEnough(ArrayList<Vector2Int> line) {
        final int numberOfAllowedOverlappingPoints = (int)(CaveTilemap.HOLE_LENGTH *0.5f);
        ArrayList<Vector2Int> toBeRemovedPoints = new ArrayList<>();
        for (Vector2Int point : line) {
            if (!openTiles.contains(point) || holePositions.contains(point)) {
                toBeRemovedPoints.add(point);
            }
        }
        if (toBeRemovedPoints.size() < numberOfAllowedOverlappingPoints) {
            return false;
        }
        line.removeAll(toBeRemovedPoints);
        return true;
    }

    private ArrayList<Vector2Int> createLine(float angle, Vector2Int startingPoint, int lineWidth) {
        Vector2Int lineEndPoint = new Vector2Int(startingPoint.x + (int)(MathUtils.cos(angle) * CaveTilemap.HOLE_LENGTH),
                startingPoint.y + (int)(MathUtils.sin(angle) * CaveTilemap.HOLE_LENGTH));
        return TileGenerationUtils.getLinePositions(startingPoint, lineEndPoint, lineWidth);
    }
}
