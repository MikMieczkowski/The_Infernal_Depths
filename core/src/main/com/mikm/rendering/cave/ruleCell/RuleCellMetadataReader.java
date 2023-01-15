package com.mikm.rendering.cave.ruleCell;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.mikm.Vector2Int;

public class RuleCellMetadataReader {
    private RuleCellMetadata metadata;

    public RuleCellMetadata createMetadataFromFile(String directory) {
        String rawMetadata = getTextFromFile(directory);
        return readMetadataString(rawMetadata);
    }

    private String getTextFromFile(String directory) {
        FileHandle handle = Gdx.files.local(directory);
        String newLineOrSpaceRegex = "[\\r\\n ]+";
        return handle.readString().replaceAll(newLineOrSpaceRegex, "");
    }

    private RuleCellMetadata readMetadataString(String rawMetadata) {
        metadata = new RuleCellMetadata();
        Vector2Int tilePosition;
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

    private Vector2Int getNextTwoNumbers(int i, String rawMetadata) {
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
        if (i+5 > rawMetadata.length()-1) {
            throw new RuntimeException("Rule Cell Reader tried to read position after \"]\" character but went out of bounds.");
        }
        boolean firstNumberIsOneDigit = (rawMetadata.charAt(i+2) == ',');
        boolean secondNumberIsOneDigit = (rawMetadata.charAt(i+4) == ']');
        int totalDigits = (firstNumberIsOneDigit ? 1 : 2) + (secondNumberIsOneDigit ? 1 : 2);

        return i + 3 + totalDigits;
    }

    private Vector2Int numberStringToIntegers(String first, String second) {
        Vector2Int output = new Vector2Int();
        try {
            output.x = Integer.parseInt(first);
            output.y = Integer.parseInt(second);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Couldn't read Rule cell tile position");
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
        throw new RuntimeException("Couldn't rotate or flip rule cell ruleset");
    }

    private TileRuleset getTileRulesetFromRawData(int i, String rawMetadata) {
        CellPresence[][] rules = new CellPresence[3][3];
        int totalIteration = 0;
        for (int y = 2; y >= 0; y--) {
            for (int x = 0; x < 3; x++) {
                if (rawMetadata.charAt(i + totalIteration) == 'o') {
                    rules[y][x] = CellPresence.Empty;
                } else if (rawMetadata.charAt(i + totalIteration) == 'x') {
                    rules[y][x] = CellPresence.Full;
                } else if (rawMetadata.charAt(i + totalIteration) == '-') {
                    rules[y][x] = CellPresence.Either;
                } else {
                    metadata.prettyPrint();
                    throw new RuntimeException("Couldn't read rule cell ruleset, tried to read \"" + rawMetadata.charAt(i + totalIteration) + "\"");
                }
                totalIteration++;
            }
        }
        return new TileRuleset(rules);
    }
}
