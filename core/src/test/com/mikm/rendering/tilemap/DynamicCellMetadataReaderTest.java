package com.mikm.rendering.tilemap;

import com.badlogic.gdx.math.Vector2;
import org.junit.jupiter.api.Test;
import static com.mikm.rendering.tilemap.CellPresence.*;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class DynamicCellMetadataReaderTest {
    @Test
    public void readFileProperly() {
        DynamicCellMetadataReader metadataReader = new DynamicCellMetadataReader();
        DynamicCellMetadata metadata = metadataReader.createMetadataFromFile("images/caveTiles.meta.txt");
        TileRuleset expected = new TileRuleset(Either, Empty, Either, Full, Either, Full, Either, Empty, Either);
        assertEquals(metadata.tilesMetadata.get(new Vector2(1,11))[0].array, expected.array);
    }
}