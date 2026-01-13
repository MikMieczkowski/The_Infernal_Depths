package com.mikm.rendering.cave;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessFiles;
import com.badlogic.gdx.backends.headless.HeadlessNativesLoader;
import com.badlogic.gdx.files.FileHandle;
import com.mikm.YamlCopyResolver;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class YamlCopyResolverTest {

    public static class WeaponConfig {
        public float IMAGE_X;
        public float IMAGE_Y;
        public float USAGE_TIME;
        public float COOLDOWN_TIME;
        public int DAMAGE;
        public boolean CAN_BREAK_ROCKS;
        public List<Projectile> PROJECTILES;

        public static class Projectile {
            public String CREATE_ON;
            public String ANIMATION_NAME;
            public float SPEED;
            public Float FPS;
        }
    }

    public static void main(String[] args) throws IOException {
        HeadlessNativesLoader.load();
        Gdx.files = new HeadlessFiles();

        // write temporary YAML test files
        writeFile("copperSword1.yaml",
                "IMAGE_X: 0\n" +
                        "IMAGE_Y: 0\n" +
                        "USAGE_TIME: 0.4\n" +
                        "COOLDOWN_TIME: 0\n" +
                        "DAMAGE: 1\n" +
                        "CAN_BREAK_ROCKS: false\n" +
                        "PROJECTILES:\n" +
                        "  - CREATE_ON: PRESS\n" +
                        "    ANIMATION_NAME: \"swordSlice\"\n" +
                        "    SPEED: 0\n");

        writeFile("copperSword2.yaml",
                "COPY: copperSword1.yaml\n" +
                        "COOLDOWN_TIME: 1\n");

        writeFile("weaponList.yaml",
                "IMAGE_X: 0\n" +
                        "PROJECTILES:\n" +
                        "  - CREATE_ON: PRESS\n" +
                        "    ANIMATION_NAME: \"slice\"\n" +
                        "    SPEED: 0\n" +
                        "  - COPY: 0\n" +
                        "    SPEED: 1\n");

        writeFile("weaponMap.yaml",
                "map:\n" +
                        "  elem1:\n" +
                        "    data1: 1\n" +
                        "    data2: 2\n" +
                        "    data3:\n" +
                        "      data4: 4\n" +
                        "    CONFIG:\n" +
                        "      data1: 1\n" +
                        "      data2: 2\n" +
                        "      data3: 3\n" +
                        "  elem2:\n" +
                        "    data1: 1\n" +
                        "    data3:\n" +
                        "      data4: 4\n" +
                        "    COPY_CONFIG: elem1\n" +
                        "    CONFIG:\n" +
                        "      data2: 7\n");

        testSimpleCopy();
        testOverride();
        testListCopy();
        testMapSubfieldCopy();

        System.out.println("✅ All tests passed.");
    }

    private static void writeFile(String name, String content) throws IOException {
        FileHandle file = Gdx.files.local(name);
        try (FileWriter writer = new FileWriter(file.file())) {
            writer.write(content);
        }
    }

    private static void testSimpleCopy() {
        WeaponConfig config = YamlCopyResolver.loadAndResolve("copperSword2.yaml", WeaponConfig.class);
        assert config.DAMAGE == 1;
        assert config.COOLDOWN_TIME == 1.0f;
        assert config.PROJECTILES.size() == 1;
        System.out.println("✓ testSimpleCopy passed");
    }

    private static void testOverride() {
        WeaponConfig config = YamlCopyResolver.loadAndResolve("copperSword2.yaml", WeaponConfig.class);
        assert config.COOLDOWN_TIME == 1.0f;
        System.out.println("✓ testOverride passed");
    }

    private static void testListCopy() {
        WeaponConfig config = YamlCopyResolver.loadAndResolve("weaponList.yaml", WeaponConfig.class);
        assert config.PROJECTILES.size() == 2;
        assert config.PROJECTILES.get(1).SPEED == 1;
        System.out.println("✓ testListCopy passed");
    }

    private static void testMapSubfieldCopy() {
        Map resolved = YamlCopyResolver.loadAndResolve("weaponMap.yaml", Map.class);
        Map map = (Map) resolved.get("map");
        Map elem2 = (Map) map.get("elem2");
        Map config = (Map) elem2.get("CONFIG");
        assert config.get("data2").equals(7);
        assert config.get("data3").equals(3);
        System.out.println("✓ testMapSubfieldCopy passed");
    }
}
