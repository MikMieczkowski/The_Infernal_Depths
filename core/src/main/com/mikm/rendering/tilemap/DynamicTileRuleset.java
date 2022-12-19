package com.mikm.rendering.tilemap;

public class DynamicTileRuleset {
    public CellPresence[][] array;
    public DynamicTileRuleset(CellPresence[][] tileRuleset) {
        if (tileRuleset.length != 3 || tileRuleset[0].length != 3) {
            throw new ExceptionInInitializerError("Tile Rulesets must be 3x3");
        }
        array = tileRuleset;
    }
}

enum CellPresence {
    Empty, Full, Either
}
