package com.mikm.rendering.tilemap;

public class TileRulesetTransformer {
    public static TileRuleset[] createRotatedRulesetsFrom(TileRuleset originalRuleset) {
        CellPresence[][] originalArray = originalRuleset.array;
        TileRuleset[] outputRulesets = new TileRuleset[4];
        outputRulesets[0] = originalRuleset;
        CellPresence[][] lastArray = originalArray;
        for (int rotation = 0; rotation < 3; rotation++) {
            CellPresence[][] rotatedArray = TileRulesetTransformer.rotateArray(lastArray);

            TileRuleset outputRuleset = new TileRuleset(rotatedArray);
            outputRulesets[rotation + 1] = outputRuleset;
            lastArray = rotatedArray;
        }
        return outputRulesets;
    }

    public static TileRuleset[] createHorizontallyFlippedRulesetsFrom(TileRuleset originalRuleset) {
        TileRuleset[] outputRulesets = new TileRuleset[2];
        CellPresence[][] flippedArray = TileRulesetTransformer.flipArrayHorizontal(originalRuleset.array);
        TileRuleset flippedRuleset = new TileRuleset(flippedArray);

        outputRulesets[0] = originalRuleset;
        outputRulesets[1] = flippedRuleset;
        return outputRulesets;
    }

    public static TileRuleset[] createVerticallyFlippedRulesetsFrom(TileRuleset originalRuleset) {
        TileRuleset[] outputRulesets = new TileRuleset[2];
        CellPresence[][] flippedArray = TileRulesetTransformer.flipArrayVertical(originalRuleset.array);
        TileRuleset flippedRuleset = new TileRuleset(flippedArray);

        outputRulesets[0] = originalRuleset;
        outputRulesets[1] = flippedRuleset;
        return outputRulesets;
    }

    private static CellPresence[][] rotateArray(CellPresence[][] source) {
        CellPresence[][] destination = new CellPresence[3][3];
        destination[0][0] = source[0][2];
        destination[1][0] = source[0][1];
        destination[2][0] = source[0][0];

        destination[0][1] = source[1][2];
        destination[1][1] = source[1][1];
        destination[2][1] = source[1][0];

        destination[0][2] = source[2][2];
        destination[1][2] = source[2][1];
        destination[2][2] = source[2][0];
        return destination;
    }

    private static CellPresence[][] flipArrayHorizontal(CellPresence[][] source) {
        CellPresence[][] destination = new CellPresence[3][3];
        destination[0][0] = source[2][0];
        destination[1][0] = source[1][0];
        destination[2][0] = source[0][0];

        destination[0][1] = source[2][1];
        destination[1][1] = source[1][1];
        destination[2][1] = source[0][1];

        destination[0][2] = source[2][2];
        destination[1][2] = source[1][2];
        destination[2][2] = source[0][2];
        return destination;
    }

    private static CellPresence[][] flipArrayVertical(CellPresence[][] source) {
        CellPresence[][] destination = new CellPresence[3][3];
        destination[0][0] = source[0][2];
        destination[1][0] = source[1][2];
        destination[2][0] = source[2][2];

        destination[0][1] = source[0][1];
        destination[1][1] = source[1][1];
        destination[2][1] = source[2][1];

        destination[0][2] = source[0][0];
        destination[1][2] = source[1][0];
        destination[2][2] = source[2][0];
        return destination;
    }
}
