package com.mikm;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.mikm.rendering.cave.CaveTilemap.MAP_HEIGHT;
import static com.mikm.rendering.cave.CaveTilemap.MAP_WIDTH;

public class TileGenerationUtils {
    public static ArrayList<ArrayList<Vector2Int>> getRegions(boolean tileType, boolean[][] ruleCellPositions)
    {
        ArrayList<ArrayList<Vector2Int>> regions = new ArrayList<>();
        boolean[][] mapFlags = new boolean[MAP_HEIGHT][MAP_WIDTH];

        for (int y = MAP_HEIGHT - 1; y >= 0; y--)
        {
            for (int x = 0; x < MAP_WIDTH; x++)
            {
                if (!mapFlags[y][x] && ruleCellPositions[y][x] == tileType)
                {
                    ArrayList<Vector2Int> newRegion = getRegionTiles(y, x, ruleCellPositions);
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

    private static ArrayList<Vector2Int> getRegionTiles(int startY, int startX, boolean[][] ruleCellPositions)
    {
        ArrayList<Vector2Int> tiles = new ArrayList<>();
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
                    if (isInMapRange(y, x) && (y == tile.y || x == tile.x))
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

    public static ArrayList<Vector2Int> getLinePositions(Vector2Int from, Vector2Int to, int lineWidth) {
        ArrayList<Vector2Int> output = new ArrayList<>();
        ArrayList<Vector2Int> line = drawLine(from, to);
        for (Vector2Int c : line)
        {
            drawCircle(output, c, lineWidth);
        }
        return output;
    }

    private static ArrayList<Vector2Int> drawLine(Vector2Int from, Vector2Int to)
    {
        ArrayList<Vector2Int> line = new ArrayList<>();

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

    private static void drawCircle(List<Vector2Int> addTo, Vector2Int c, int r)
    {
        for (int x = -r; x <= r; x++)
        {
            for (int y = -r; y <= r; y++)
            {
                if (x*x + y*y <= r*r)
                {
                    int drawX = c.x + x;
                    int drawY = c.y + y;
                    if (isInMapRange(drawY, drawX))
                    {
                        Vector2Int point = new Vector2Int(drawX, drawY);
                        if (!addTo.contains(point)) {
                            addTo.add(point);
                        }
                    }
                }
            }
        }
    }


    public static int sign(int num) {
        return Integer.compare(num, 0);
    }

    private static boolean isInMapRange(int y, int x)
    {
        return x >= 0 && x < MAP_WIDTH && y >= 0 && y < MAP_HEIGHT;
    }
}
