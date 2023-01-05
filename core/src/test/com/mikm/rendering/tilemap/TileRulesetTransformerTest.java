package com.mikm.rendering.tilemap;

import com.mikm.rendering.tilemap.ruleCell.TileRuleset;
import com.mikm.rendering.tilemap.ruleCell.TileRulesetTransformer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.mikm.rendering.tilemap.ruleCell.CellPresence.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TileRulesetTransformerTest {
    TileRuleset original;

    @BeforeEach
    public void setup() {
        original = new TileRuleset(Empty, Either, Empty,
                                Either, Full, Empty,
                                Either, Either, Empty);
    }

    @Test
    public void shouldCreateRotatedRulesets() {
        TileRuleset rotated1 = new TileRuleset(Either, Either, Empty,
                                            Either, Full, Either,
                                            Empty, Empty, Empty);
        TileRuleset rotated2 = new TileRuleset(Empty, Either, Either,
                                            Empty, Full, Either,
                                            Empty, Either, Empty);
        TileRuleset rotated3 = new TileRuleset(Empty, Empty, Empty,
                                            Either, Full, Either,
                                            Empty, Either, Either);
        TileRuleset[] expected = new TileRuleset[]{original, rotated1, rotated2, rotated3};
        TileRuleset[] actual = TileRulesetTransformer.createRotatedRulesetsFrom(original);

        assertTrue(Arrays.deepEquals(expected[0].array, actual[0].array));
        assertTrue(Arrays.deepEquals(expected[1].array, actual[1].array));
        assertTrue(Arrays.deepEquals(expected[2].array, actual[2].array));
        assertTrue(Arrays.deepEquals(expected[3].array, actual[3].array));

        assertEquals(0, actual[0].rotation);
        assertEquals(1, actual[1].rotation);
        assertEquals(2, actual[2].rotation);
        assertEquals(3, actual[3].rotation);
    }

    @Test
    public void shouldCreateHorizontallyFlippedRulesets() {
        TileRuleset flipped = new TileRuleset(Empty, Either, Empty,
                                            Empty, Full, Either,
                                            Empty, Either, Either);
        TileRuleset[] expected = new TileRuleset[]{original, flipped};
        TileRuleset[] actual = TileRulesetTransformer.createHorizontallyFlippedRulesetsFrom(original);

        assertTrue(Arrays.deepEquals(expected[0].array, actual[0].array));
        assertTrue(Arrays.deepEquals(expected[1].array, actual[1].array));
    }

    @Test
    public void shouldCreateVerticallyFlippedRulesets() {
        TileRuleset flipped = new TileRuleset(Either, Either, Empty,
                Either, Full, Empty,
                Empty, Either, Empty);
        TileRuleset[] expected = new TileRuleset[]{original, flipped};
        TileRuleset[] actual = TileRulesetTransformer.createVerticallyFlippedRulesetsFrom(original);

        assertTrue(Arrays.deepEquals(expected[0].array, actual[0].array));
        assertTrue(Arrays.deepEquals(expected[1].array, actual[1].array));
    }


}