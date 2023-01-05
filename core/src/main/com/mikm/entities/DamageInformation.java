package com.mikm.entities;

public class DamageInformation {
    public float knockbackAngle;
    public float knockbackForceMagnitude;
    public int damage;

    public DamageInformation(float knockbackAngle, float knockbackForceMagnitude, int damage) {
        this.knockbackAngle = knockbackAngle;
        this.knockbackForceMagnitude = knockbackForceMagnitude;
        this.damage = damage;
    }

    public DamageInformation(int damage) {
        this.damage = damage;
    }
}
