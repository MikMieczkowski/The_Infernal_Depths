package com.mikm.entities.prefabLoader.weapon;

import java.util.List;
import java.util.Map;

/**
 * Raw POJO for weapon YAML data.
 * Fields match the YAML structure directly.
 * COMBO_TREE and AERIAL_COMBO_TREE remain as raw maps for transformation.
 */
public class WeaponRawData {
    public OrbitData ORBIT;
    public Map<String, Object> COMBO_TREE;
    public Map<String, Object> AERIAL_COMBO_TREE;
    public Map<String, AttackConfigData> CONFIG;

    public static class OrbitData {
        public Integer IMAGE_X;
        public Integer IMAGE_Y;
        public String ORBIT_TYPE;
        public Float ORBIT_DISTANCE;
        public Float ORBIT_ITEM_ANIMATION_SPEED;
        public String ORBIT_ITEM_ANIMATION_TYPE;
        public String ANIMATION_PREFIX;
        public Boolean POINTS_TOWARDS_LOCKED;
    }

    public static class AttackConfigData {
        public List<Integer> PROJECTILE_DAMAGE;
    }
}
