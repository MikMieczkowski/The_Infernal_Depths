package com.mikm.rendering.tilemap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;

public class DynamicCellMetadataReader {
    private DynamicCellMetadata metadata;

    public DynamicCellMetadata createMetadataFromFile(String directory) {
        String rawMetadata = getTextFromFile(directory);
        return readMetadataString(rawMetadata);
    }

    private String getTextFromFile(String directory) {
        FileHandle handle = Gdx.files.local(directory);
        String newLineOrSpaceRegex = "[\\r\\n ]+";
        return handle.readString().replaceAll(newLineOrSpaceRegex, "");
    }

    private DynamicCellMetadata readMetadataString(String rawMetadata) {
        metadata = new DynamicCellMetadata();
        Vector2 tilePosition;
        TileRuleset[] tileRulesets;
        for (int i = 0; i < rawMetadata.length(); i++) {
            if (rawMetadata.charAt(i) == '[') {
                tilePosition = getNextTwoNumbers(i, rawMetadata);
                i = iPositionAfterRightBracket(i, rawMetadata);
                tileRulesets = readTileRuleset(i, rawMetadata);
                metadata.addTileRulesetsForPosition(tilePosition, tileRulesets);
            }
        }
        return metadata;
    }

    private Vector2 getNextTwoNumbers(int i, String rawMetadata) {
        String firstNumberString;
        String secondNumberString;

        boolean firstNumberIsOneDigit = (rawMetadata.charAt(i+2) == ',');
        if (firstNumberIsOneDigit) {
            firstNumberString = String.valueOf(rawMetadata.charAt(i+1));
        } else {
            firstNumberString = rawMetadata.substring(i+1, i+2+1);
            System.out.println(firstNumberString);
            //make i act as if number was one digit
            i++;
        }

        boolean secondNumberIsOneDigit = (rawMetadata.charAt(i+4) == ']');
        if (secondNumberIsOneDigit) {
            secondNumberString = String.valueOf(rawMetadata.charAt(i+3));
        } else {
            secondNumberString = rawMetadata.substring(i+3, i+4+1);
        }
        return numberStringToIntegers(firstNumberString, secondNumberString);
    }

    private TileRuleset[] readTileRuleset(int i, String rawMetadata) {
        char charAtI = rawMetadata.charAt(i);
        if (charAtI == 'R' || charAtI == 'H' || charAtI == 'V') {
            return rotatedOrFlippedRulesets(i, rawMetadata);
        }
        TileRuleset[] singleTileRuleset = new TileRuleset[1];
        singleTileRuleset[0] = getTileRulesetFromRawData(i, rawMetadata);
        return singleTileRuleset;
    }

    private int iPositionAfterRightBracket(int i, String rawMetadata) {
        //has side effects but whatever
        try {
            rawMetadata.charAt(i+5);
        } catch (Exception e) {
            throw new RuntimeException("Dynamic Cell Reader tried to read position after \"]\" character but went out of bounds.");
        }
        boolean firstNumberIsOneDigit = (rawMetadata.charAt(i+2) == ',');
        boolean secondNumberIsOneDigit = (rawMetadata.charAt(i+4) == ']');
        int totalDigits = (firstNumberIsOneDigit ? 1 : 2) + (secondNumberIsOneDigit ? 1 : 2);

        return i + 3 + totalDigits;
    }

    private Vector2 numberStringToIntegers(String first, String second) {
        Vector2 output = new Vector2();
        try {
            output.x = Integer.parseInt(first);
            output.y = Integer.parseInt(second);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Couldn't read Dynamic cell tile position");
        }
        return output;
    }


    private TileRuleset[] rotatedOrFlippedRulesets(int i, String rawMetadata) {
        char charAtI = rawMetadata.charAt(i);
        //This only increments i for this method and further, the next characters will be scanned anyway.
        //To go past the R/H/V character to read the raw data
        i += 1;
        TileRuleset originalRuleset = getTileRulesetFromRawData(i, rawMetadata);
        if (charAtI == 'R') {
            return TileRulesetTransformer.createRotatedRulesetsFrom(originalRuleset);
        }
        if (charAtI == 'H') {
            return TileRulesetTransformer.createHorizontallyFlippedRulesetsFrom(originalRuleset);
        }
        if (charAtI == 'V') {
            return TileRulesetTransformer.createVerticallyFlippedRulesetsFrom(originalRuleset);
        }
        throw new RuntimeException("Couldn't rotate or flip dynamic cell ruleset");
    }

    private TileRuleset getTileRulesetFromRawData(int i, String rawMetadata) {
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
        return new TileRuleset(rules);
    }
}
