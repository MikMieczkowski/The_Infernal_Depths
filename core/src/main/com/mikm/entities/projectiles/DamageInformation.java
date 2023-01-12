package com.mikm.entities.projectiles;

public class DamageInformation {
    public float knockbackAngle;
    public float knockbackForceMagnitude;
    public float damage;

    public DamageInformation(float knockbackAngle, float knockbackForceMagnitude, float damage) {
        this.knockbackAngle = knockbackAngle;
        this.knockbackForceMagnitude = knockbackForceMagnitude;
        this.damage = damage;
    }

    public DamageInformation(int damage) {
        this.damage = damage;
    }
}
