package com.mikm.entities.prefabLoader.weapon;

/**
 * Duration/weight of an attack in a combo.
 * Determines timing and can affect damage multipliers.
 */
public enum AttackDuration {
    LIGHT,
    MEDIUM,
    HEAVY;

    /**
     * Parses a duration from a string (case-insensitive).
     *
     * @param s The string to parse (e.g., "light", "MEDIUM", "Heavy")
     * @return The corresponding AttackDuration
     * @throws IllegalArgumentException if the string doesn't match any duration
     */
    public static AttackDuration fromString(String s) {
        return valueOf(s.toUpperCase());
    }
}
