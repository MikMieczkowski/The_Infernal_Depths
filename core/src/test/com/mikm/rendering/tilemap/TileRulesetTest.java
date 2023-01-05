package com.mikm.rendering.tilemap;

import com.mikm.rendering.tilemap.ruleCell.TileRuleset;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static com.mikm.rendering.tilemap.ruleCell.CellPresence.*;

class TileRulesetTest {
    @Test
    public void leftShouldFitRight() {
        TileRuleset left = new TileRuleset(Either, Empty, Full,
                                            Either, Either, Empty,
                                            Full, Empty, Empty);
        TileRuleset right = new TileRuleset(Full, Empty, Full,
                                            Full, Either, Empty,
                                            Full, Empty, Empty);
        assertTrue(left.fits(right));
        assertTrue(right.fits(left));
    }

    @Test
    public void leftShouldNotFitRight() {
        TileRuleset left = new TileRuleset(Either, Empty, Full,
                Either, Either, Empty,
                Full, Empty, Empty);
        TileRuleset right = new TileRuleset(Full, Full, Full,
                Full, Either, Empty,
                Full, Empty, Empty);
        assertFalse(left.fits(right));
        assertFalse(right.fits(left));
    }
}