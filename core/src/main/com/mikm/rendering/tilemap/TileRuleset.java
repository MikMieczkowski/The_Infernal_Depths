package com.mikm.rendering.tilemap;

public class TileRuleset {
    public CellPresence[][] array;
    public TileRuleset(CellPresence[][] tileRuleset) {
        if (tileRuleset.length != 3 || tileRuleset[0].length != 3) {
            throw new ExceptionInInitializerError("Tile Rulesets must be 3x3");
        }
        array = tileRuleset;
    }

    public TileRuleset(CellPresence topLeft, CellPresence top, CellPresence topRight,
                       CellPresence left, CellPresence middle, CellPresence right,
                       CellPresence bottomLeft, CellPresence bottom, CellPresence bottomRight) {
        array = new CellPresence[3][3];
        array[0][0] = topLeft;
        array[1][0] = top;
        array[2][0] = topRight;

        array[0][1] = left;
        array[1][1] = middle;
        array[2][1] = right;

        array[0][2] = bottomLeft;
        array[1][2] = bottom;
        array[2][2] = bottomRight;
    }
}

enum CellPresence {
    Empty, Full, Either
}
