package com.mikm.rendering.cave;

import com.mikm.ExtraMathUtils;
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
        ProcessMap();
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

    private void ProcessMap()
    {

        List<List<Vector2Int>> wallRegions = GetRegions(true);

        for(List<Vector2Int> wallRegion : wallRegions)
        {
            if (wallRegion.size() < WALL_THRESHOLD_SIZE)
            {
                for(Vector2Int tile : wallRegion)
                {
                    ruleCellPositions[tile.y][tile.x] = false;
                }
            }
        }

        List<List<Vector2Int>> roomRegions = GetRegions(false);
        ArrayList<CaveRoom> survivingRooms = new ArrayList<>();

        for (List<Vector2Int> roomRegion : roomRegions)
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
        ConnectClosestRooms(survivingRooms, false);
    }

    private void ConnectClosestRooms(List<CaveRoom> allRooms, boolean forceAccessibilityFromMainRoom)
    {
        List<CaveRoom> roomListA = new ArrayList<>();
        List<CaveRoom> roomListB = new ArrayList<>();

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
                CreatePassage(bestRoomA, bestRoomB, bestTileA, bestTileB);
            }
        }
        if (possibleConnectionFound && forceAccessibilityFromMainRoom)
        {
            CreatePassage(bestRoomA, bestRoomB, bestTileA, bestTileB);
            ConnectClosestRooms(allRooms, true);
        }
        if (!forceAccessibilityFromMainRoom)
        {
            ConnectClosestRooms(allRooms, true);
        }
    }

    private void CreatePassage(CaveRoom roomA, CaveRoom roomB, Vector2Int tileA, Vector2Int tileB)
    {
        CaveRoom.ConnectRooms(roomA, roomB);

        List<Vector2Int> line = GetLine(tileA, tileB);
        for (Vector2Int c : line)
        {
            DrawCircle(c, PASSAGE_WIDTH);
        }

    }

    private void DrawCircle(Vector2Int c, int r)
    {
        for (int x = -r; x <= r; x++)
        {
            for (int y = -r; y <= r; y++)
            {
                if (x*x + y*y <= r*r)
                {
                    int drawX = c.x + x;
                    int drawY = c.y + y;
                    if (IsInMapRange(drawY, drawX))
                    {
                        ruleCellPositions[drawY][drawX] = false;
                    }
                }
            }
        }
    }

    private List<Vector2Int> GetLine(Vector2Int from, Vector2Int to)
    {
        List<Vector2Int> line = new ArrayList<>();

        int x = from.x;
        int y = from.y;

        int dx = to.x - from.x;
        int dy = to.y - from.y;

        boolean inverted = false;
        int step = sign(dx);
        int gradientStep = sign(dy);
        int longest = Math.abs(dx);
        int shortest = Math.abs(dy);

        if (longest < shortest)
        {
            inverted = true;
            longest = Math.abs(dy);
            shortest = Math.abs(dx);

            step = sign(dy);
            gradientStep = sign(dx);
        }

        int gradientAccumulation = longest / 2;
        for (int i = 0; i < longest; i++)
        {
            line.add(new Vector2Int(x, y));
            if (inverted)
            {
                y += step;
            }
            else
            {
                x += step;
            }
            gradientAccumulation += shortest;
            if (gradientAccumulation >= longest)
            {
                if (inverted)
                {
                    x += gradientStep;
                } else
                {
                    y += gradientStep;
                }
                gradientAccumulation -= longest;
            }
        }
        return line;
    }

    private List<List<Vector2Int>> GetRegions(boolean tileType)
    {
        List<List<Vector2Int>> regions = new ArrayList<>();
        boolean[][] mapFlags = new boolean[MAP_HEIGHT][MAP_WIDTH];

        for (int y = MAP_HEIGHT - 1; y >= 0; y--)
        {
            for (int x = 0; x < MAP_WIDTH; x++)
            {
                if (!mapFlags[y][x] && ruleCellPositions[y][x] == tileType)
                {
                    List<Vector2Int> newRegion = getRegionTiles(y, x);
                    regions.add(newRegion);

                    for (Vector2Int tile : newRegion)
                    {
                        mapFlags[tile.y][tile.x] = true;
                    }
                }
            }
        }
        return regions;
    }

    private List<Vector2Int> getRegionTiles(int startY, int startX)
    {
        List<Vector2Int> tiles = new ArrayList<>();
        boolean[][] mapFlags = new boolean[MAP_HEIGHT][MAP_WIDTH];
        boolean tileType = ruleCellPositions[startY][startX];

        LinkedList<Vector2Int> queue = new LinkedList<>();
        queue.add(new Vector2Int(startX, startY));
        mapFlags[startY][startX] = true;

        while (queue.size() > 0)
        {
            Vector2Int tile = queue.remove();
            tiles.add(tile);

            for (int y = tile.y - 1; y <= tile.y + 1; y++)
            {
                for(int x = tile.x - 1; x <= tile.x + 1; x++)
                {
                    if (IsInMapRange(y, x) && (y == tile.y || x == tile.x))
                    {
                        if (!mapFlags[y][x] && ruleCellPositions[y][x] == tileType)
                        {
                            mapFlags[y][x] = true;
                            queue.add(new Vector2Int(x, y));
                        }
                    }
                }
            }
        }
        return tiles;
    }

    private boolean IsInMapRange(int y, int x)
    {
        return x >= 0 && x < MAP_WIDTH && y >= 0 && y < MAP_HEIGHT;
    }

    private int sign(int n) {
        return Integer.compare(n, 0);
    }

}
