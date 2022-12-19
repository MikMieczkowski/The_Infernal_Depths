package com.mikm.rendering.tilemap;


import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DynamicCellMetadata {
    HashMap<Vector2, DynamicTileRuleset[]> tilesMetadata = new HashMap<>();

    public void addTileRulesetsForPosition(Vector2 position, DynamicTileRuleset[] tileRulesets) {
        tilesMetadata.put(position, tileRulesets);
    }

    public void prettyPrint() {
        for (Map.Entry<Vector2, DynamicTileRuleset[]> entry : tilesMetadata.entrySet()) {
            Vector2 key = entry.getKey();
            DynamicTileRuleset[] value = entry.getValue();
            for (DynamicTileRuleset tileRuleset : value) {
                System.out.println("Position: " + key.x + ", " + key.y + ". TileRuleset: " + Arrays.deepToString(tileRuleset.array));
            }
        }
    }
}

