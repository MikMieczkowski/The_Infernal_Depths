package com.mikm.entities.prefabLoader;

import java.util.ArrayList;
import java.util.List;

public class WeaponYAMLReader {
    class AttackNode {
        public AttackDuration duration;
        public String attackName;
        public List<AttackNode> children = new ArrayList<>();

        public AttackNode(AttackDuration duration, String attackName) {
            this.duration = duration;
            this.attackName = attackName;
        }
    }

    enum AttackDuration {LIGHT, MEDIUM, HEAVY}
}
