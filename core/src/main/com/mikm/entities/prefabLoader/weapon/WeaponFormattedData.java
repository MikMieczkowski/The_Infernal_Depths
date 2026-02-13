package com.mikm.entities.prefabLoader.weapon;

import java.util.List;
import java.util.Map;

/**
 * Formatted POJO for weapon data with properly typed fields.
 * COMBO_TREE and AERIAL_COMBO_TREE are transformed from raw nested maps
 * into structured AttackNode trees.
 */
public class WeaponFormattedData {
    public OrbitData ORBIT;
    public List<AttackNode> COMBO_TREE;
    public List<AttackNode> AERIAL_COMBO_TREE;
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
