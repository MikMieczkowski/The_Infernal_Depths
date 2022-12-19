package com.mikm.rendering.tilemap;

import com.badlogic.gdx.graphics.Texture;

public class DynamicCell {
    public Texture spritesheet;
    public DynamicCellMetadata metadata;
    public DynamicCell(Texture spritesheet, DynamicCellMetadata metadata) {
        this.spritesheet = spritesheet;
        this.metadata = metadata;
    }
}
