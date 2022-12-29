package com.mikm.rendering.tilemap;

import com.mikm.Vector2Int;

import java.util.ArrayList;
import java.util.List;

class CaveRoom implements Comparable<CaveRoom> {
    public List<Vector2Int> tiles;
    public List<Vector2Int> edgeTiles;
    public List<CaveRoom> connectedRooms;
    public int roomSize;
    public boolean isAccesibleFromMainRoom;
    public boolean isMainRoom;

    public CaveRoom()
    {

    }

    public CaveRoom(List<Vector2Int> roomTiles, boolean[][] ruleCellPositions) {
        tiles = roomTiles;
        roomSize = tiles.size();
        connectedRooms = new ArrayList<>();
        edgeTiles = new ArrayList<>();

        for(Vector2Int tile : tiles)
        {
            for (int y = tile.y - 1; y <= tile.y + 1; y++) {
                for (int x = tile.x - 1; x <= tile.x + 1; x++) {
                    if (x == tile.x || y == tile.y) {
                        if (ruleCellPositions[y][x])
                        {
                            edgeTiles.add(tile);
                        }
                    }
                }
            }
        }
    }

    public void SetAccesibleFromMainRoom() {
        if (!isAccesibleFromMainRoom) {
            isAccesibleFromMainRoom = true;
            for(CaveRoom connectedRoom : connectedRooms)
            {
                connectedRoom.SetAccesibleFromMainRoom();
            }
        }
    }

    public static void ConnectRooms(CaveRoom roomA, CaveRoom roomB) {
        if (roomA.isAccesibleFromMainRoom) {
            roomB.SetAccesibleFromMainRoom();
        } else if (roomB.isAccesibleFromMainRoom) {
            roomA.SetAccesibleFromMainRoom();
        }
        roomA.connectedRooms.add(roomB);
        roomB.connectedRooms.add(roomA);
    }

    public boolean IsConnected(CaveRoom otherRoom) {
        return connectedRooms.contains(otherRoom);
    }

    @Override
    public int compareTo(CaveRoom otherRoom) {
        return Integer.compare(otherRoom.roomSize, roomSize);
    }
}