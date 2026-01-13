package com.mikm;

import java.util.*;

public class AttackData {
    public int IMAGE_X = 0;
    public int IMAGE_Y = 0;
    public double USAGE_TIME = 0.5;
    public int COOLDOWN_TIME = 0;
    public double COMBO_TIME = 0.6;
    public boolean CAN_BREAK_ROCKS = false;
    public String ENTER_SOUND_EFFECT = "swing";
    public String END_SOUND_EFFECT = null;
    public int ORBIT_DISTANCE = 15;
    public String ORBIT_TYPE = "SWAP";
    public double ORBIT_ITEM_ANIMATION_SPEED = 0.5;
    public String ORBIT_ITEM_ANIMATION_TYPE = "SWING";
    public MOVEMENT_CONFIG MOVEMENT_CONFIG = new MOVEMENT_CONFIG();
    public static class MOVEMENT_CONFIG {
        public int SPEED = 0;
    }
    public String PLAYER_ANIMATION = "PlayerAttacking";
    public List<PROJECTILESItem> PROJECTILES = new ArrayList<>();
    public static class PROJECTILESItem {
        public String CREATE_ON = "PRESS";
        public int CREATE_ON_HOLD_TIME = 1;
        public String ANIMATION_NAME = "swordSlice";
        public double FPS = 0.1;
        public boolean ORBITS = true;
        public int SPEED = 0;
    }
}
