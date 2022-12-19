package com.mikm.rendering.tilemap;


import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DynamicCellMetadata {
    HashMap<Vector2, TileRuleset[]> tilesMetadata = new HashMap<>();

    public void addTileRulesetsForPosition(Vector2 position, TileRuleset[] tileRulesets) {
        tilesMetadata.put(position, tileRulesets);
    }

    public void prettyPrint() {
        for (Map.Entry<Vector2, TileRuleset[]> entry : tilesMetadata.entrySet()) {
            Vector2 key = entry.getKey();
            TileRuleset[] value = entry.getValue();
            for (TileRuleset tileRuleset : value) {
                System.out.println("Position: " + key.x + ", " + key.y + ". TileRuleset: " + Arrays.deepToString(tileRuleset.array));
            }
        }
    }
}

