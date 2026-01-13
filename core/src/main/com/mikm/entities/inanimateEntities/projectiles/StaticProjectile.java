package com.mikm.entities.inanimateEntities.projectiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.entities.DamageInformation;
import com.mikm.rendering.screens.Application;
//TODO projectiles
//public class StaticProjectile extends InanimateEntity {
//    private TextureRegion image;
//    private Hurtbox hurtbox;
//    private boolean visible;
//
//    public StaticProjectile(TextureRegion image, boolean visible, DamageInformation damageInformation, float x, float y) {
//        super(x, y);
//        this.image = image;
//        this.visible = visible;
//        hurtbox = new Hurtbox(10, true);
//        hurtbox.setPosition(x+5, y+5);
//
//        hurtbox.setDamageInformation(damageInformation);
//    }
//
//    @Override
//    public void update() {
//        if (!Application.playerOLD.routineHandler.inAction("Dive")) {
//            hurtbox.checkIfHitPlayer();
//        }
//    }
//
//    @Override
//    public boolean hasShadow() {
//        return false;
//    }
//
//    @Override
//    public void draw() {
//        if (visible) {
//            Application.batch.draw(image, x, y);
//        }
//    }
//}
