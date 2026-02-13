package com.mikm.entities;

public class DamageInformation {
    public float knockbackAngle;
    public float knockbackForceMagnitude;
    public int damage;
    public int hitstunFrames;

    public DamageInformation(float knockbackAngle, float knockbackForceMagnitude, int damage, int hitstunFrames) {
        this.knockbackAngle = knockbackAngle;
        this.knockbackForceMagnitude = knockbackForceMagnitude;
        this.damage = damage;
        this.hitstunFrames = hitstunFrames;
    }

    public DamageInformation(float knockbackAngle, float knockbackForceMagnitude, int damage) {
        this(knockbackAngle, knockbackForceMagnitude, damage, 0);
    }

    public DamageInformation(int damage) {
        this.damage = damage;
    }
}
