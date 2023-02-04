package com.mikm.rendering.cave;

import com.mikm.ExtraMathUtils;
import com.mikm.TileGenerationUtils;
import com.mikm.Vector2Int;

import java.util.*;

import static com.mikm.rendering.cave.CaveTilemap.MAP_WIDTH;
import static com.mikm.rendering.cave.CaveTilemap.MAP_HEIGHT;

//Thanks to Sebastian Lague

class RuleCellPositionGenerator {
    public final int WALL_THRESHOLD_SIZE = 50, ROOM_THRESHOLD_SIZE = 50, PASSAGE_WIDTH = 1;

    private boolean[][] ruleCellPositions;

    public boolean[][] createRuleCellPositions() {
        ruleCellPositions = new boolean[MAP_HEIGHT][MAP_WIDTH];
        fillRuleCellPositionsRandomly();
        for (int i = 0; i < 5; i++) {
            smoothRuleCellPositions();
        }
        processMap();
        return ruleCellPositions;
    }

    private void fillRuleCellPositionsRandomly() {
        for (int y = MAP_HEIGHT - 1; y >= 0; y--) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                if (x == 0 || x == MAP_WIDTH - 1 || y == 0 || y == MAP_HEIGHT - 1) {
                    ruleCellPositions[y][x] = true;
                } else {
                    ruleCellPositions[y][x] = (ExtraMathUtils.randomInt(100) < CaveTilemap.FILL_CELL_PERCENT_CHANCE);
                }
            }
        }
    }

    private void smoothRuleCellPositions() {
        for (int y = MAP_HEIGHT - 1; y >= 0; y--) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                int neighboringWallCount = getNeighboringCellCount(y, x);
                if (neighboringWallCount > 4) {
                    ruleCellPositions[y][x] = true;
                } else if (neighboringWallCount < 4){
                    ruleCellPositions[y][x] = false;
                }
            }
        }
    }

    private int getNeighboringCellCount(int y, int x) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                boolean outOfBounds = (x+j < 0 || x+j > MAP_WIDTH - 1 || y+i < 0 || y+i > MAP_HEIGHT - 1);
                if (outOfBounds) {
                    count++;
                    continue;
                }
                if (ruleCellPositions[y + i][x + j]) {
                    count++;
                }
            }
        }
        return count;
    }

    private void processMap()
    {
        ArrayList<ArrayList<Vector2Int>> wallRegions = TileGenerationUtils.getRegions(true, ruleCellPositions);

        for(ArrayList<Vector2Int> wallRegion : wallRegions)
        {
            if (wallRegion.size() < WALL_THRESHOLD_SIZE)
            {
                for(Vector2Int tile : wallRegion)
                {
                    ruleCellPositions[tile.y][tile.x] = false;
                }
            }
        }

        ArrayList<ArrayList<Vector2Int>> roomRegions = TileGenerationUtils.getRegions(false, ruleCellPositions);
        ArrayList<CaveRoom> survivingRooms = new ArrayList<>();

        for (ArrayList<Vector2Int> roomRegion : roomRegions)
        {
            if (roomRegion.size() < ROOM_THRESHOLD_SIZE)
            {
                for (Vector2Int tile : roomRegion)
                {
                    ruleCellPositions[tile.y][tile.x] = true;
                }
            } else
            {
                survivingRooms.add(new CaveRoom(roomRegion, ruleCellPositions));
            }
        }
        if (survivingRooms.size() == 0) {
            System.out.println("Map was empty");
            return;
        }
        survivingRooms.sort(Comparator.naturalOrder());
        survivingRooms.get(0).isMainRoom = true;
        survivingRooms.get(0).isAccesibleFromMainRoom = true;
        connectClosestRooms(survivingRooms, false);
    }

    private void connectClosestRooms(ArrayList<CaveRoom> allRooms, boolean forceAccessibilityFromMainRoom)
    {
        ArrayList<CaveRoom> roomListA = new ArrayList<>();
        ArrayList<CaveRoom> roomListB = new ArrayList<>();

        if (forceAccessibilityFromMainRoom)
        {
            for (CaveRoom room : allRooms)
            {
                if (room.isAccesibleFromMainRoom)
                {
                    roomListB.add(room);
                } else
                {
                    roomListA.add(room);
                }
            }
        } else
        {
            roomListA = allRooms;
            roomListB = allRooms;
        }

        int bestDistance = 0;
        Vector2Int bestTileA = new Vector2Int();
        Vector2Int bestTileB = new Vector2Int();
        CaveRoom bestRoomA = new CaveRoom();
        CaveRoom bestRoomB = new CaveRoom();
        boolean possibleConnectionFound = false;

        for (CaveRoom roomA : roomListA)
        {
            if (!forceAccessibilityFromMainRoom)
            {
                possibleConnectionFound = false;
                if (roomA.connectedRooms.size() > 0)
                {
                    continue;
                }
            }
            for (CaveRoom roomB : roomListB)
            {
                if (roomA == roomB || roomA.IsConnected(roomB))
                {
                    continue;
                }
                for (int tileIndexA = 0; tileIndexA < roomA.edgeTiles.size(); tileIndexA++)
                {
                    for (int tileIndexB = 0; tileIndexB < roomB.edgeTiles.size(); tileIndexB++)
                    {
                        Vector2Int tileA = roomA.edgeTiles.get(tileIndexA);
                        Vector2Int tileB = roomB.edgeTiles.get(tileIndexB);
                        int distanceBetweenRooms = (int)(Math.pow(tileA.x - tileB.x, 2) + Math.pow(tileA.y - tileB.y, 2));

                        if (distanceBetweenRooms < bestDistance || !possibleConnectionFound)
                        {
                            bestDistance = distanceBetweenRooms;
                            possibleConnectionFound = true;
                            bestTileA = tileA;
                            bestTileB = tileB;
                            bestRoomA = roomA;
                            bestRoomB = roomB;

                        }
                    }
                }
            }
            if (possibleConnectionFound && !forceAccessibilityFromMainRoom)
            {
                createPassage(bestRoomA, bestRoomB, bestTileA, bestTileB);
            }
        }
        if (possibleConnectionFound && forceAccessibilityFromMainRoom)
        {
            createPassage(bestRoomA, bestRoomB, bestTileA, bestTileB);
            connectClosestRooms(allRooms, true);
        }
        if (!forceAccessibilityFromMainRoom)
        {
            connectClosestRooms(allRooms, true);
        }
    }

    private void createPassage(CaveRoom roomA, CaveRoom roomB, Vector2Int tileA, Vector2Int tileB)
    {
        CaveRoom.ConnectRooms(roomA, roomB);

        ArrayList<Vector2Int> passagePositions = TileGenerationUtils.getLinePositions(tileA, tileB, PASSAGE_WIDTH);
        for (Vector2Int c : passagePositions)
        {
            ruleCellPositions[c.y][c.x] = false;
        }

    }



}
