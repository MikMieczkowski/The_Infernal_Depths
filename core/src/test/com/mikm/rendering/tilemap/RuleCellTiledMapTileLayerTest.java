package com.mikm.rendering.tilemap;

import com.mikm.rendering.tilemap.ruleCell.RuleCell;
import com.mikm.rendering.tilemap.ruleCell.RuleCellTiledMapTileLayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RuleCellTiledMapTileLayerTest {
    @Test
    public void metadataShouldGivePointerNotCopy() {
        RuleCellTiledMapTileLayer tiledMapTileLayer = new RuleCellTiledMapTileLayer(16, 16, 16, 16);
        RuleCell metadataMock = mock(RuleCell.class);
        tiledMapTileLayer.ruleCells[0][0] = metadataMock;
        tiledMapTileLayer.ruleCells[0][1] = metadataMock;
        assertEquals(tiledMapTileLayer.ruleCells[0][0], tiledMapTileLayer.ruleCells[0][1]);
    }
}