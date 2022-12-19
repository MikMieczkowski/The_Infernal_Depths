package com.mikm.rendering.tilemap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;

public class DynamicCellMetadataReader {
    private static DynamicCellMetadata metadata;

    public static DynamicCellMetadata createMetadataFromFile(String directory) {
        String rawMetadata = getTextFromFile(directory);
        return readMetadataString(rawMetadata);
    }

    private static String getTextFromFile(String directory) {
        FileHandle handle = Gdx.files.local(directory);
        String newLineOrSpaceRegex = "[\\r\\n ]+";
        return handle.readString().replaceAll(newLineOrSpaceRegex, "");
    }

    private static DynamicCellMetadata readMetadataString(String rawMetadata) {
        metadata = new DynamicCellMetadata();
        Vector2 tilePosition;
        DynamicTileRuleset[] tileRulesets;
        for (int i = 0; i < rawMetadata.length(); i++) {
            if (rawMetadata.charAt(i) == '[') {
                tilePosition = getNextTwoNumbers(i, rawMetadata);
                //Moves i to position after ']'
                i += 5;
                tileRulesets = readTileRuleset(i, rawMetadata);
                metadata.addTileRulesetsForPosition(tilePosition, tileRulesets);
            }
        }
        return metadata;
    }

    private static Vector2 getNextTwoNumbers(int i, String rawMetadata) {
        Vector2 position = new Vector2();
        char nextChar = rawMetadata.charAt(i+1);
        char charAfterComma = rawMetadata.charAt(i+3);
        checkTilePositionCharacters(nextChar, charAfterComma);
        position.x = Character.getNumericValue(nextChar);
        position.y = Character.getNumericValue(charAfterComma);
        return position;
    }

    private static DynamicTileRuleset[] readTileRuleset(int i, String rawMetadata) {
        char charAtI = rawMetadata.charAt(i);
        if (charAtI == 'R' || charAtI == 'H' || charAtI == 'V') {
            return rotatedOrFlippedRulesets(i, rawMetadata);
        }
        DynamicTileRuleset[] singleTileRuleset = new DynamicTileRuleset[1];
        singleTileRuleset[0] = getTileRulesetFromRawData(i, rawMetadata);
        return singleTileRuleset;
    }

    private static void checkTilePositionCharacters(char first, char second) {
        if (!(Character.isDigit(first) && Character.isDigit(second))) {
            throw new RuntimeException("Couldn't read dynamic cell position");
        }
    }

    private static DynamicTileRuleset[] rotatedOrFlippedRulesets(int i, String rawMetadata) {
        char charAtI = rawMetadata.charAt(i);
        //This only increments i for this method and further, the next characters will be scanned anyway.
        //To go past the R/H/V character to read the raw data
        i += 1;
        DynamicTileRuleset originalRuleset = getTileRulesetFromRawData(i, rawMetadata);
        if (charAtI == 'R') {
            return createRotatedRulesetsFrom(originalRuleset);
        }
        if (charAtI == 'H') {
            return createHorizontallyFlippedRulesetsFrom(originalRuleset);
        }
        if (charAtI == 'V') {
            return createVerticallyFlippedRulesetsFrom(originalRuleset);
        }
        throw new RuntimeException("Couldn't rotate or flip dynamic cell ruleset");
    }

    private static DynamicTileRuleset getTileRulesetFromRawData(int i, String rawMetadata) {
        CellPresence[][] rules = new CellPresence[3][3];
        int totalIteration = 0;
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                if (rawMetadata.charAt(i + totalIteration) == 'o') {
                    rules[x][y] = CellPresence.Empty;
                } else if (rawMetadata.charAt(i + totalIteration) == 'x') {
                    rules[x][y] = CellPresence.Full;
                } else if (rawMetadata.charAt(i + totalIteration) == '-') {
                    rules[x][y] = CellPresence.Either;
                } else {
                    metadata.prettyPrint();
                    throw new RuntimeException("Couldn't read dynamic cell ruleset, tried to read \"" + rawMetadata.charAt(i + totalIteration) + "\"");
                }
                totalIteration++;
            }
        }
        return new DynamicTileRuleset(rules);
    }

    private static DynamicTileRuleset[] createRotatedRulesetsFrom(DynamicTileRuleset originalRuleset) {
        CellPresence[][] originalArray = originalRuleset.array;
        DynamicTileRuleset[] outputRulesets = new DynamicTileRuleset[4];
        for (int rotation = 0; rotation < 4; rotation++) {
            CellPresence[][] rotatedArray = rotateArray(originalArray);

            DynamicTileRuleset outputRuleset = new DynamicTileRuleset(rotatedArray);
            outputRulesets[rotation] = outputRuleset;
        }
        return outputRulesets;
    }

    private static DynamicTileRuleset[] createHorizontallyFlippedRulesetsFrom(DynamicTileRuleset originalRuleset) {
        DynamicTileRuleset[] outputRulesets = new DynamicTileRuleset[2];
        CellPresence[][] flippedArray = flipArrayHorizontal(originalRuleset.array);
        DynamicTileRuleset flippedRuleset = new DynamicTileRuleset(flippedArray);

        outputRulesets[0] = originalRuleset;
        outputRulesets[1] = flippedRuleset;
        return outputRulesets;
    }

    private static DynamicTileRuleset[] createVerticallyFlippedRulesetsFrom(DynamicTileRuleset originalRuleset) {
        DynamicTileRuleset[] outputRulesets = new DynamicTileRuleset[2];
        CellPresence[][] flippedArray = flipArrayVertical(originalRuleset.array);
        DynamicTileRuleset flippedRuleset = new DynamicTileRuleset(flippedArray);

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
