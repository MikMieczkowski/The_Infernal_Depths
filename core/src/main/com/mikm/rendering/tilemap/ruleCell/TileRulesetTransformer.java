package com.mikm.rendering.tilemap.ruleCell;

public class TileRulesetTransformer {
    public static TileRuleset[] createRotatedRulesetsFrom(TileRuleset originalRuleset) {
        CellPresence[][] originalArray = originalRuleset.array;
        TileRuleset[] outputRulesets = new TileRuleset[4];
        outputRulesets[0] = originalRuleset;
        CellPresence[][] lastArray = originalArray;
        for (int rotation = 0; rotation < 3; rotation++) {
            CellPresence[][] rotatedArray = TileRulesetTransformer.rotateArray(lastArray);

            TileRuleset outputRuleset = new TileRuleset(rotatedArray);
            outputRuleset.rotation = rotation + 1;
            outputRulesets[rotation + 1] = outputRuleset;
            lastArray = rotatedArray;
        }
        return outputRulesets;
    }

    public static TileRuleset[] createHorizontallyFlippedRulesetsFrom(TileRuleset originalRuleset) {
        TileRuleset[] outputRulesets = new TileRuleset[2];
        CellPresence[][] flippedArray = TileRulesetTransformer.flipArrayHorizontal(originalRuleset.array);
        TileRuleset flippedRuleset = new TileRuleset(flippedArray);
        flippedRuleset.flippedHorizontally = true;

        outputRulesets[0] = originalRuleset;
        outputRulesets[1] = flippedRuleset;
        return outputRulesets;
    }

    public static TileRuleset[] createVerticallyFlippedRulesetsFrom(TileRuleset originalRuleset) {
        TileRuleset[] outputRulesets = new TileRuleset[2];
        CellPresence[][] flippedArray = TileRulesetTransformer.flipArrayVertical(originalRuleset.array);
        TileRuleset flippedRuleset = new TileRuleset(flippedArray);
        flippedRuleset.flippedVertically = true;

        outputRulesets[0] = originalRuleset;
        outputRulesets[1] = flippedRuleset;
        return outputRulesets;
    }

    private static CellPresence[][] rotateArray(CellPresence[][] source) {
        CellPresence[][] rotated = new CellPresence[3][3];
        rotated[2][0] = source[0][0];
        rotated[2][1] = source[1][0];
        rotated[2][2] = source[2][0];

        rotated[1][0] = source[0][1];
        rotated[1][1] = source[1][1];
        rotated[1][2] = source[2][1];

        rotated[0][0] = source[0][2];
        rotated[0][1] = source[1][2];
        rotated[0][2] = source[2][2];
        return rotated;
    }

    private static CellPresence[][] flipArrayHorizontal(CellPresence[][] source) {
        CellPresence[][] flipped = new CellPresence[3][3];
        flipped[2][0] = source[2][2];
        flipped[2][1] = source[2][1];
        flipped[2][2] = source[2][0];

        flipped[1][0] = source[1][2];
        flipped[1][1] = source[1][1];
        flipped[1][2] = source[1][0];

        flipped[0][0] = source[0][2];
        flipped[0][1] = source[0][1];
        flipped[0][2] = source[0][0];
        return flipped;
    }

    private static CellPresence[][] flipArrayVertical(CellPresence[][] source) {
        CellPresence[][] flipped = new CellPresence[3][3];
        flipped[2][0] = source[0][0];
        flipped[2][1] = source[0][1];
        flipped[2][2] = source[0][2];

        flipped[1][0] = source[1][0];
        flipped[1][1] = source[1][1];
        flipped[1][2] = source[1][2];

        flipped[0][0] = source[2][0];
        flipped[0][1] = source[2][1];
        flipped[0][2] = source[2][2];
        return flipped;
    }
}
